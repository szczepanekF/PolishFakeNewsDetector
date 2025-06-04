package com.pfnd.BusinessLogicService.service.impl;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfnd.BusinessLogicService.Messages;
import com.pfnd.BusinessLogicService.model.dto.FactCheckResultDto;
import com.pfnd.BusinessLogicService.model.messages.FactCheckCommand;
import com.pfnd.BusinessLogicService.model.postgresql.EvaluationHistoryRecord;
import com.pfnd.BusinessLogicService.repository.EvalutationHistoryRepository;
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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactCheckRequestHandlerImpl implements FactCheckRequestHandler {

    // TODO add email to taskId map and map taskIds to Status
    // TODO CREATE status and implement responses with status, or make the websocket impl
    private final EvalutationHistoryRepository evalutationHistoryRepository; //TODO implement user service responsible for handling edge cases


    private final RabbitTemplate rabbitTemplate;
    private final ConnectionFactory connectionFactory;
    private final Set<String> ongoingTasks = new HashSet<>();

    @Override
    public void requestEvaluation(FactCheckCommand request) {
        String correlationId = String.valueOf(request.historyId());
        ongoingTasks.add(correlationId);
        String replyQueueName = "reply_" + correlationId;

        try (Connection conn = connectionFactory.createConnection();
             Channel channel = conn.createChannel(false)) {
            channel.queueDeclare(replyQueueName, false, true, false, Map.of("x-expires", 180000));
        } catch (IOException | TimeoutException e) {
            log.error(e.getMessage());
            throw new RuntimeException(Messages.MSG_QUEUE_ERROR);
        }

        defineReplyQueueListener(replyQueueName, correlationId, request);
        // Send request
        MessageProperties props = new MessageProperties();
        props.setReplyTo(replyQueueName);
        props.setCorrelationId(correlationId);
        props.setContentType("application/json");
        try {
            Message requestMessage = new Message(new ObjectMapper().writeValueAsBytes(request), props);
            rabbitTemplate.send("", "analyze_tasks", requestMessage);
            log.info("Waiting for reply...");
        } catch (JsonProcessingException e) {
            log.error(Messages.SERIALIZATION_ERROR, e);
            throw new RuntimeException(e);
        }
    }

    private void defineReplyQueueListener(String replyQueueName, String correlationId, FactCheckCommand request) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(replyQueueName);
        container.setMessageListener(message -> {
            String corrId = message.getMessageProperties().getCorrelationId();
            String body = new String(message.getBody());
            if (correlationId.equals(corrId)) {
                log.info("Received evaluation response for historyid: {} - {}", request.historyId(), body);
                try {
                    FactCheckResultDto result = new ObjectMapper().readValue(body, FactCheckResultDto.class);
                    EvaluationHistoryRecord record =
                            evalutationHistoryRepository.findById(request.historyId())
                                                        .orElseThrow(
                                                                () -> new RuntimeException(Messages.DOES_NOT_EXIST));
                    record.setScore(result.getSentiment().getFinalScore());

                    if (result.getStatus().equals("SUCCESS") || result.getStatus().equals("ERROR")) {
                        tryToDeleteTheQueue(replyQueueName);
                    }
                } catch (JacksonException e) {
                    tryToDeleteTheQueue(replyQueueName);
                    log.error(Messages.SERIALIZATION_ERROR, e);
                }
                ongoingTasks.remove(correlationId);
            }
            container.stop();
        });
        container.start();

    }


    private void tryToDeleteTheQueue(String queueName) {
        try (Connection conn = connectionFactory.createConnection();
             Channel ch = conn.createChannel(false)) {
            ch.queueDelete(queueName);
            log.info("Deleted reply queue: {}", queueName);
        } catch (IOException | TimeoutException e) {
            log.error("Failed to delete queue: {}", queueName, e);
            throw new RuntimeException(Messages.MSG_QUEUE_ERROR);
        }

    }
    //MANUAL TEST QUEUE LISTENER METHOD
//    @RabbitListener(queues = "analyze_tasks")
//    public void handleAnalyzeTask(Message message) throws IOException {
//        String body = new String(message.getBody(), StandardCharsets.UTF_8);
//        Map<String, Object> request = new ObjectMapper().readValue(body, new TypeReference<>() {
//        });
//
//        // Example logic: "processing"
//        String inputText = (String) request.get("text");
//        FactCheckResultDto.Reference ref = new FactCheckResultDto.Reference(42, "title", "url");
//        FactCheckResultDto.ScoredValue sample = new FactCheckResultDto.ScoredValue("value", 0.5f);
//        FactCheckResultDto.AnalyzeResult analyzeRes =
//                new FactCheckResultDto.AnalyzeResult(0.9f, ClassificationLabel.UNCLASSIFIED, "explanation",
//                                                     Map.of("chuj", sample), List.of(ref));
//        FactCheckResultDto result = new FactCheckResultDto("id", inputText, "status", analyzeRes);
//
//        // Get replyTo and correlationId
//        MessageProperties props = message.getMessageProperties();
//        String replyTo = props.getReplyTo();
//        String correlationId = props.getCorrelationId();
//
//        if (replyTo != null && correlationId != null) {
//            // Build reply message
//            byte[] replyBytes = new ObjectMapper().writeValueAsBytes(result);
//
//            MessageProperties replyProps = new MessageProperties();
//            replyProps.setCorrelationId(correlationId);
//            replyProps.setContentType("application/json");
//
//            Message replyMessage = new Message(replyBytes, replyProps);
//
//            // Send reply
//            rabbitTemplate.send("", replyTo, replyMessage);
//        }
//    }
}
