package com.pfnd.BusinessLogicService.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pfnd.BusinessLogicService.model.messages.FactCheckCommand;
import com.pfnd.BusinessLogicService.utils.Messages;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueueMenager {

    private final ConnectionFactory connectionFactory;

    public String createReplyQueue(String correlationId) {
        String queueName = buildReplyQueueName(correlationId);

        try (Connection conn = connectionFactory.createConnection();
             Channel channel = conn.createChannel(false)) {
            channel.queueDeclare(queueName, false, true, true, Map.of("x-expires", 180000));
            return queueName;
        } catch (IOException | TimeoutException e) {
            log.error(e.getMessage());
            throw new RuntimeException(Messages.MSG_QUEUE_ERROR, e);
        }
    }

    public void deleteQueue(String queueName) {
        try (Connection conn = connectionFactory.createConnection();
             Channel ch = conn.createChannel(false)) {
            ch.queueDelete(queueName);
            log.info("Deleted reply queue: {}", queueName);
        } catch (IOException | TimeoutException e) {
            log.error("Failed to delete queue: {}", queueName, e);
            throw new RuntimeException(Messages.MSG_QUEUE_ERROR, e);
        }
    }

    public void listenForReply(String queueName, String correlationId,
                                                         BiConsumer<String, SimpleMessageListenerContainer> callback) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(msg -> {
            if (correlationId.equals(msg.getMessageProperties().getCorrelationId())) {
                callback.accept(new String(msg.getBody()), container);
            }
        });
        container.start();
    }


    private String buildReplyQueueName(String correlationId) {
        return "reply_" + correlationId;
    }
}
