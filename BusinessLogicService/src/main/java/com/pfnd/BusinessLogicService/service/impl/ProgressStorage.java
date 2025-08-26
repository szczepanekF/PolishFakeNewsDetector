package com.pfnd.BusinessLogicService.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfnd.BusinessLogicService.model.dto.FactCheckProgressDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProgressStorage {

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    public void initialize(int historyId) {
        FactCheckProgressDto dto = new FactCheckProgressDto("Kolejkowanie", String.valueOf(historyId), 0, 10000, null);
        store(dto, historyId);
    }

    public void store(FactCheckProgressDto result, long historyId) {
        try {
            String key = buildKey(historyId);
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result), Duration.ofMinutes(10));
            log.info("Stored interim result in Redis for historyId: {}", historyId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize interim result", e);
        }
    }

    public void delete(long historyId) {
        String key = "interim_result:" + historyId;
        redisTemplate.delete(key);
        log.debug("Deleting interim result in Redis for historyId {}", historyId);
    }

    public Optional<FactCheckProgressDto> get(long historyId) {
        String key = "interim_result:" + historyId;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        try {
            FactCheckProgressDto dto = objectMapper.readValue(value, FactCheckProgressDto.class);
            return Optional.of(dto);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize interim result", e);
            return Optional.empty();
        }
    }

    private String buildKey(long historyId) {
        return "interim_result:" + historyId;
    }
}
