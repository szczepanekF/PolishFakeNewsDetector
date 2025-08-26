package com.pfnd.BusinessLogicService.service.impl;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfnd.BusinessLogicService.Messages;
import com.pfnd.BusinessLogicService.model.dto.AnalyzeResult;
import com.pfnd.BusinessLogicService.model.dto.FactCheckResultDto;
import com.pfnd.BusinessLogicService.model.messages.FactCheckCommand;
import com.pfnd.BusinessLogicService.model.postgresql.AnalyzeResultRecord;
import com.pfnd.BusinessLogicService.model.postgresql.EvaluationHistoryRecord;
import com.pfnd.BusinessLogicService.model.postgresql.ReferenceRecord;
import com.pfnd.BusinessLogicService.model.postgresql.ResultRecord;
import com.pfnd.BusinessLogicService.repository.AnalyzeResultRepository;
import com.pfnd.BusinessLogicService.repository.EvaluationHistoryRepository;
import com.pfnd.BusinessLogicService.service.FactCheckRequestHandler;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactCheckRequestHandlerImpl implements FactCheckRequestHandler {

    private final EvaluationHistoryRepository evaluationHistoryRepository;
    private final AnalyzeResultRepository analyzeResultRepository;

    private final RabbitTemplate rabbitTemplate;
    private final ConnectionFactory connectionFactory;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void requestEvaluation(FactCheckCommand request) {
        String correlationId = String.valueOf(request.historyId());
        String replyQueueName = "reply_" + correlationId;

        try (Connection conn = connectionFactory.createConnection();
             Channel channel = conn.createChannel(false)) {
            channel.queueDeclare(replyQueueName, false, true, true, Map.of("x-expires", 180000));
        } catch (IOException | TimeoutException e) {
            log.error(e.getMessage());
            throw new RuntimeException(Messages.MSG_QUEUE_ERROR, e);
        }

        populateInterimnQueue(request.historyId());
        defineReplyQueueListener(replyQueueName, correlationId, request);
        MessageProperties props = new MessageProperties();
        props.setReplyTo(replyQueueName);
        props.setCorrelationId(correlationId);
        props.setContentType("application/json");
        try {
            Message requestMessage = new Message(objectMapper.writeValueAsBytes(request), props);
            rabbitTemplate.send("", "analyze_tasks", requestMessage);
            log.info("Waiting for reply...");
        } catch (JsonProcessingException e) {
            log.error(Messages.SERIALIZATION_ERROR, e);
            throw new RuntimeException(e);
        }
    }

    private void populateInterimnQueue(int historyId) {
        FactCheckResultDto dto = new FactCheckResultDto();
        dto.setMessage("Kolejkowanie");
        dto.setId(String.valueOf(historyId));
        dto.setCurrentStep(0);
        dto.setAllSteps(10000);
        storeInterimResultInRedis(dto, historyId);
    }

    private void defineReplyQueueListener(String replyQueueName, String correlationId, FactCheckCommand request) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(replyQueueName);
        container.setMessageListener(message -> {
            String corrId = message.getMessageProperties().getCorrelationId();
            String body = new String(message.getBody());
            if (correlationId.equals(corrId)) {
                log.info("Received evaluation response for historyId: {} - {}", request.historyId(), body);
                try {
                    FactCheckResultDto result = objectMapper.readValue(body, FactCheckResultDto.class);
                    if (isFinalStep(result)) {
                        log.debug("Received final step {} {}", result.getCurrentStep(), result.getAllSteps());
                        updateHistoryRecord(request, result);
                        deleteInterimResult(request.historyId());
                        tryToDeleteTheQueue(replyQueueName);
                        container.stop();
                    } else {
                        storeInterimResultInRedis(result, request.historyId());
                    }
                } catch (JacksonException e) {
                    tryToDeleteTheQueue(replyQueueName);
                    log.error(Messages.SERIALIZATION_ERROR, e);
                    container.stop();
                }
            }
        });
        container.start();
    }

    private boolean isFinalStep(FactCheckResultDto result) {
        return result.getCurrentStep() == result.getAllSteps();
    }

    private void updateHistoryRecord(FactCheckCommand request, FactCheckResultDto result) {
        EvaluationHistoryRecord record =
                evaluationHistoryRepository.findById(request.historyId())
                                            .orElseThrow(
                                                    () -> new RuntimeException(Messages.DOES_NOT_EXIST));
        Optional<AnalyzeResultRecord> analyzeResultRecord = analyzeResultRepository.findByHistoryRecord_Id(request.historyId());
        if (analyzeResultRecord.isPresent()) {
            log.error("{}{}", Messages.ENTITY_EXISTS, analyzeResultRecord.get());
            throw new RuntimeException(Messages.ENTITY_EXISTS + analyzeResultRecord.get());
        }
        AnalyzeResult receivedResult = result.getResult();
        List<ReferenceRecord> referenceRecords = receivedResult.getReferences().stream().map(ReferenceRecord::new)
                                                               .toList();
        List<ResultRecord> resultValues = receivedResult.getResults().entrySet().stream()
                                                        .map(entry -> new ResultRecord(entry.getKey(),
                                                                entry.getValue())).toList();

        AnalyzeResultRecord resultRecord = AnalyzeResultRecord.builder()
                                                              .historyRecord(record)
                                                              .finalScore(receivedResult.getFinalScore())
                                                              .label(receivedResult.getLabel())
                                                              .explanation(receivedResult.getExplanation())
                                                              .results(resultValues)
                                                              .references(referenceRecords)
                                                              .build();
        for (ResultRecord rr : resultValues) {
            rr.setAnalyzeResult(resultRecord);
        }
        for (ReferenceRecord ref : referenceRecords) {
            ref.setAnalyzeResult(resultRecord);
        }
        record.setSteps(result.getAllSteps());
        record.setStatus(result.getMessage());
        try {
            analyzeResultRepository.save(resultRecord);
            evaluationHistoryRepository.save(record);
            analyzeResultRepository.flush();
        } catch (Exception e) {
            log.error("{}{}", Messages.SAVE_ERROR, resultRecord, e);
            throw new RuntimeException(Messages.SAVE_ERROR + resultRecord);
        }
    }

    private void storeInterimResultInRedis(FactCheckResultDto result, long historyId) {
        try {
            String key = "interim_result:" + historyId;
            String value = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(10));
            log.info("Stored interim result in Redis for historyId: {}", historyId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize interim result", e);
        }
    }

    private void deleteInterimResult(long historyId) {
        String key = "interim_result:" + historyId;
        redisTemplate.delete(key);
        log.debug("Deleting interim result in Redis for historyId {}", historyId);
    }

    private void tryToDeleteTheQueue(String queueName) {
        try (Connection conn = connectionFactory.createConnection();
             Channel ch = conn.createChannel(false)) {
            ch.queueDelete(queueName);
            log.info("Deleted reply queue: {}", queueName);
        } catch (IOException | TimeoutException e) {
            log.error("Failed to delete queue: {}", queueName, e);
            throw new RuntimeException(Messages.MSG_QUEUE_ERROR, e);
        }

    }

    @Override
    public Optional<FactCheckResultDto> getInterimResult(long historyId) {
        String key = "interim_result:" + historyId;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) return Optional.empty();
        try {
            FactCheckResultDto dto = objectMapper.readValue(value, FactCheckResultDto.class);
            return Optional.of(dto);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize interim result", e);
            return Optional.empty();
        }
    }

}
