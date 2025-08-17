package com.thred.datingapp.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.BaseErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
@Component
@RequiredArgsConstructor
public class RedisUtils {

  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper                  objectMapper;

  public void saveConnected(final String key, final Long value) {
    redisTemplate.opsForSet().add(key, value);
  }

  public Object get(final String key) {
    return redisTemplate.opsForValue().get(key);
  }

  //  접속 중인 유저 목록을 반환하는 메서드
  public <T> Set<T> getConnected(final String key, final Class<T> classType) {
    Set<Object> operations = redisTemplate.opsForSet().members(key);
    if (operations == null || operations.isEmpty()) {
      return Collections.emptySet();
    }
    return operations.stream().map(value -> {
      try {
        return objectMapper.readValue(value.toString(), classType);
      } catch (JsonProcessingException e) {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String errorMessage = "Error processing JSON for key: " + key + " - " + e.getMessage();
        errorLog(methodName, errorMessage);
        throw new CustomException(BaseErrorCode.INTERNAL_SERVER_ERROR);
      }
    }).collect(Collectors.toSet());
  }

  public void removeConnected(final String key, final Object value) {
    // 접속된 유저 삭제
    redisTemplate.opsForSet().remove(key, value);
    // Set의 남은 요소 수 확인
    Long size = redisTemplate.opsForSet().size(key);
    // 남은 유저가 없으면 key를 삭제
    if (size != null && size == 0) {
      redisTemplate.delete(key);
    }
  }

  public void saveWithTTL(final String key, final Object value, final long ttlSeconds, final TimeUnit timeUnit) {
    ValueOperations<String, Object> operations = redisTemplate.opsForValue();
    operations.set(key, value, ttlSeconds, timeUnit);
  }

  public void deleteValue(String key) {
    redisTemplate.delete(key);
  }

  public Object getValue(String key) {
    ValueOperations<String, Object> operations = redisTemplate.opsForValue();
    return operations.get(key);
  }

  private void errorLog(final String methodName, final String errorMessage) {
    String className = "RedisUtils";
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    // 통일된 메시지 형식
    String logMessage =
        String.format("Timestamp: [%s], Class: [%s], Method: [%s], ErrorMessage: [%s]", timestamp, className, methodName, errorMessage);

    log.error(logMessage);
  }

}
