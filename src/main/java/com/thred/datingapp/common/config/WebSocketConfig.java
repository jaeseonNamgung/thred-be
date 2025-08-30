package com.thred.datingapp.common.config;

import com.thred.datingapp.common.config.interceptor.JwtPrincipalHandshakeHandler;
import com.thred.datingapp.common.config.interceptor.StompConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Configuration
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final StompConfig                  stompConfig;
  private final JwtPrincipalHandshakeHandler handshakeHandler;

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // Stomp 엔드포인트 설정
    registry.addEndpoint("/ws/chat").setHandshakeHandler(handshakeHandler).setAllowedOrigins("*");
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // Spring 자체의 내장 브로커(SimpleBroker)를 활성화
    //해당 주소를 구독하고 있는 클라이언트들에게 메세지 전달
    registry.enableSimpleBroker("/sub");
    //클라이언트에서 보낸 메세지를 받을 prefix
    registry.setApplicationDestinationPrefixes("/pub");
    // rabbit MQ 설정
    // registry.setPathMatcher(new AntPathMatcher("."));

    // registry.enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue")
    // .setRelayHost("localhost")
    // .setRelayPort(61613)
    // .setSystemLogin("guest")
    // .setSystemPasscode("guest")
    // .setClientLogin("guest")
    // .setClientPasscode("guest");
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(stompConfig);
  }

}
