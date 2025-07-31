package com.thred.datingapp.chat.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SseEmitterServiceTest {

  @InjectMocks
  private SseEmitterService sseEmitterService;

  @Test
  @DisplayName("SSE createEmitter 테스트")
  void createEmitterTest() {
    // given

    // when
    SseEmitter emitter = sseEmitterService.createEmitter(1L);
    // then
    assertNotNull(emitter);
  }

}
