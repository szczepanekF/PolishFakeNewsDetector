package com.pfnd.BusinessLogicService.service.impl;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfnd.BusinessLogicService.utils.Messages;
import com.pfnd.BusinessLogicService.model.dto.FactCheckProgressDto;
import com.pfnd.BusinessLogicService.model.messages.FactCheckCommand;
import com.pfnd.BusinessLogicService.service.FactCheckRequestHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactCheckRequestHandlerImpl implements FactCheckRequestHandler {

    private final EvaluationResultService evaluationResultService;
    private final ProgressStorage progressStorage;
    private final QueueMenager queueMenager;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void requestEvaluation(FactCheckCommand request) {
        String correlationId = String.valueOf(request.historyId());
        String replyQueueName = queueMenager.createReplyQueue(correlationId);

        progressStorage.initialize(request.historyId());
        queueMenager.listenForReply(replyQueueName, correlationId, (message, container) -> {
            handleReplyMessage(request, replyQueueName, message, container);
        });

        sendRequest(request, replyQueueName, correlationId);
    }


    public void sendRequest(FactCheckCommand request, String queueName, String correlationId) {
        MessageProperties props = new MessageProperties();
        props.setReplyTo(queueName);
        props.setCorrelationId(correlationId);
        props.setContentType("application/json");
        try {
            Message requestMessage = new Message(objectMapper.writeValueAsBytes(request), props);
            rabbitTemplate.send("", "analyze_tasks", requestMessage);
            log.info("Fact check request sent for historyId={}, waiting for reply...", request.historyId());
        } catch (JsonProcessingException e) {
            log.error(Messages.SERIALIZATION_ERROR, e);
            throw new RuntimeException(e);
        }
    }

    private void handleReplyMessage(FactCheckCommand request, String queueName, String body,
                                    SimpleMessageListenerContainer container) {
        log.info("Received evaluation response for historyId: {} - {}", request.historyId(), body);
        try {
            FactCheckProgressDto result = objectMapper.readValue(body, FactCheckProgressDto.class);
            if (result.isFinalStep()) {
                log.debug("Received final step {} {}", result.getCurrentStep(), result.getAllSteps());
                evaluationResultService.updateHistoryRecord(request, result);
                progressStorage.delete(request.historyId());
                queueMenager.deleteQueue(queueName);
                container.stop();
            } else {
                progressStorage.store(result, request.historyId());
            }
        } catch (JacksonException e) {
            queueMenager.deleteQueue(queueName);
            log.error(Messages.SERIALIZATION_ERROR, e);
            container.stop();
        }
    }

    @Override
    public Optional<FactCheckProgressDto> getInterimResult(long historyId) {
        return progressStorage.get(historyId);
    }
}
