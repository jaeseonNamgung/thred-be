package com.thred.datingapp.common.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterUtil {
  private final Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

  public SseEmitter save(Long userId, SseEmitter sseEmitter) {
    sseEmitters.put(userId, sseEmitter);
    return sseEmitter;
  }

  public void deleteById(Long userId) {
    sseEmitters.remove(userId);
  }

  public SseEmitter findById(Long userId) {
    return sseEmitters.get(userId);
  }
}
