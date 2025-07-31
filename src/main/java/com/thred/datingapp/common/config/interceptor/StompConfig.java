package com.thred.datingapp.common.config.interceptor;

import com.thred.datingapp.chat.dto.ChatEventDto;
import com.thred.datingapp.chat.service.ChatRoomService;
import com.thred.datingapp.chat.service.ChatService;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.BaseErrorCode;
import com.thred.datingapp.common.error.errorCode.ChatErrorCode;
import com.thred.datingapp.common.utils.JwtUtils;
import io.jsonwebtoken.lang.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpHeaders;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

import java.util.Optional;

/**
 * @Author NamgungJaeseon
 * @Date 2024.10.8
 * @Description WebSocket 연결 시 `ChannelInterceptor`를 통해 `preSend` 메서드가 호출됨
 */
@Log4j2
@RequiredArgsConstructor
@Configuration
public class StompConfig implements ChannelInterceptor {

    private final JwtUtils jwtUtil;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        handleCommand(accessor);
        return message;
    }

    private void handleCommand(final StompHeaderAccessor accessor) {
        if(accessor.getCommand() == null) {
            log.error("[handleCommand] StompHeaderAccessor command is null ===> accessor: {}", accessor);
            throw new CustomException(ChatErrorCode.CHAT_INTERCEPTOR_ERROR);
        }
        switch (accessor.getCommand()) {
            case CONNECT:
                handleConnect(accessor);
                break;

            case DISCONNECT:
                handleDisconnect(accessor);
                break;

            default:
                break;
        }
    }

    private void handleConnect(final StompHeaderAccessor accessor) {
        String token = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        String accessToken = jwtParse(token);

        Long userId = jwtUtil.getUserId(accessToken);
        Long chatRoomId = getChatRoomId(accessor);
        if (userId == null) {
            log.error("[handleConnect] JWT 파싱 실패: userId is null ===> accessToken: {}", accessToken);
            throw new CustomException(BaseErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 테스트 용
        // Long userId = Long.valueOf(accessor.getFirstNativeHeader("userId"));

        log.debug("[handleConnect] userId: {}", userId);
        log.debug("[handleConnect] chatRoomId: {}", chatRoomId);

        accessor.getSessionAttributes().put("chatRoomId", chatRoomId);
        accessor.getSessionAttributes().put("userId", userId);

        eventPublisher.publishEvent(ChatEventDto.chatConnectEvent(userId, chatRoomId));
    }

    private void handleDisconnect(final StompHeaderAccessor accessor) {

        Long chatRoomId = (Long) accessor.getSessionAttributes().get("chatRoomId");
        Long userId = (Long) accessor.getSessionAttributes().get("userId");

        if (chatRoomId == null || userId == null) {
            log.error("[handleDisconnect] ===> chatRoomId 또는 userId가 null입니다. (chatRoomId: {}, userId: {})", chatRoomId, userId);
            return;
        }

        eventPublisher.publishEvent(ChatEventDto.chatDisconnectEvent(userId, chatRoomId));
        accessor.getSessionAttributes().remove("chatRoomId");
        accessor.getSessionAttributes().remove("userId");
        log.debug("[handleDisconnect] Websocket disconnect process 완료");
    }

    private Long getChatRoomId(final StompHeaderAccessor accessor) {
        String chatRoomIdStr =
                Optional.ofNullable(accessor.getFirstNativeHeader("chatRoomId"))
                        .orElseThrow(() -> {
                            log.error("[handleDisconnect] chatRoomId is null");
                            return new CustomException(ChatErrorCode.CHAT_INTERCEPTOR_ERROR);
                        });
        try {
            return Long.valueOf(chatRoomIdStr);
        } catch (NumberFormatException e) {
            log.error("[getChatRoomId] Long 타입 변환 중 오류 ===> error: {}", e.getMessage());
            throw new CustomException(BaseErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    private static String jwtParse(String token) {
        if (Strings.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

}
