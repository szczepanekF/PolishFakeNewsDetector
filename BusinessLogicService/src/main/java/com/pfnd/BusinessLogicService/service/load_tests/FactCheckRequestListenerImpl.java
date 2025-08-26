package com.pfnd.BusinessLogicService.service.load_tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfnd.BusinessLogicService.model.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Profile("load-test")
public class FactCheckRequestListenerImpl {

    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;


    @RabbitListener(queues = "analyze_tasks")
    public void handleAnalyzeTask(Message message) throws IOException {
        asyncProcessMessage(message);
    }

    @Async("factCheckExecutor")
    public void asyncProcessMessage(Message message) throws IOException {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        Map<String, Object> request = objectMapper.readValue(body, new TypeReference<>() {
        });
        String inputText = (String) request.get("text");

        MessageProperties props = message.getMessageProperties();
        String replyTo = props.getReplyTo();
        String correlationId = props.getCorrelationId();

        if (replyTo != null && correlationId != null) {
            int totalSteps = 9;

            for (int i = 1; i <= totalSteps; i++) {
                FactCheckResultDto result = prepareMockResult(inputText, correlationId, i, totalSteps);
                byte[] replyBytes = objectMapper.writeValueAsBytes(result);

                MessageProperties replyProps = new MessageProperties();
                replyProps.setCorrelationId(correlationId);
                replyProps.setContentType("application/json");

                Message replyMessage = new Message(replyBytes, replyProps);

                rabbitTemplate.send("", replyTo, replyMessage);
                log.info("Sent step {}/{} to reply queue {}", i, totalSteps, replyTo);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Interrupted while simulating processing delay", e);
                    break;
                }
            }
        }
    }

    private static List<String> messages = List.of("Started", "Text preparation", "Processing", "Sentiment analysis",
            "NLP", "Looking for reference sources", "Preparing response", "Success");

    private FactCheckResultDto prepareMockResult(String inputText, String correlationId, int currentStep, int allSteps) {
        Reference ref = new Reference(55,"test.source", new Date(), "url");
        ScoredValue sample = new ScoredValue("sentiment", 0.5f);
        AnalyzeResult analyzeRes =
                new AnalyzeResult(inputText, 0.9f, ClassificationLabel.UNCLASSIFIED, "explanation",
                        Map.of("test_metric", sample), List.of(ref));

        return new FactCheckResultDto(correlationId, messages.get((currentStep - 1) % messages.size()), currentStep,
                allSteps,
                analyzeRes);
    }
}
