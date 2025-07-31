package com.thred.datingapp.chat.service;

import com.thred.datingapp.chat.dto.response.SseChatMessageResponse;
import com.thred.datingapp.chat.repository.ChatRoomRepository;
import com.thred.datingapp.chat.repository.queryDsl.ChatRepository;
import com.thred.datingapp.common.entity.chat.Chat;
import com.thred.datingapp.common.entity.chat.ChatRoom;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.ChatErrorCode;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.common.utils.SseEmitterUtil;
import com.thred.datingapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Service
public class SseEmitterService {

    private final SseEmitterUtil sseEmitterUtil;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository     userRepository;
    private final ChatRepository     chatRepository;

    public SseEmitter createEmitter(final Long userId) {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        sseEmitterUtil.save(userId, sseEmitter);
        sseEmitter.onTimeout(() -> {
            log.error("[createEmitter] SSE 서버 타임아웃 발생(SSE server sent event timed out) ===> userId: {}", userId);
            removeEmitter(userId);
            sseEmitter.complete();
        });
        sseEmitter.onError(error -> {
            log.error("[createEmitter] SSE 서버 에러 발생(SSE server sent event error occurred) ===> userId: {}", userId);
            log.error(error.toString());
            removeEmitter(userId);
            sseEmitter.completeWithError(error);
        });
        sseEmitter.onCompletion(() -> {
            log.info("[createEmitter] SSE emitter 캐시 제거 완료(sse server sent event removed in emitter cache) ===> userId: {}", userId);
            removeEmitter(userId);
        });
        return sseEmitter;
    }

    public void sendMessageByClient(final Long receiverId, final Long chatRoomId, final Chat chat) {
        SseEmitter sseEmitters = sseEmitterUtil.findById(receiverId);
        if (sseEmitters != null) {
            log.info("[sendMessageByClient] SSE 접속(Connected SSE) ===>  sseEmitters: {}", sseEmitters);
            User receiver = userRepository.findById(receiverId)
                                          .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
            ChatRoom chatRoom = chatRoomRepository.findChatRoomById(chatRoomId)
                    .orElseThrow(() -> new CustomException(ChatErrorCode.NOT_FOUND_CHATROOM));
            // 읽지 않은 메지시 수
            Long unReadCount = chatRepository.countUnReadChatMessageByReceiverId(receiverId);
            log.info("[sendMessageByClient] 읽지 않은 메지시 수 조회(Selected unread message count) ===>  unReadCount: {}", unReadCount);

            try {
                SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event()
                        .id(receiverId.toString())
                        .comment("update chat room message")
                        .data(SseChatMessageResponse.fromResponse(chatRoom, chat, receiver, unReadCount));
                sseEmitters.send(eventBuilder);
                log.info("[sendMessageByClient] SSE 메시지 전송 완료(Successfully sent SSE message) ===>  eventBuilder: {}", eventBuilder);
            } catch (IOException e) {
                log.error("[sendMessageByClient] SSE 메시지 전송 실패(Failed to send message to client) ===> receiverId: {}", receiverId);
                log.error(e.toString());
                removeEmitter(receiverId);
                throw new CustomException(ChatErrorCode.SSE_ERROR);
            }
        }
    }

    public void removeEmitter(final Long userId) {
        SseEmitter emitter = sseEmitterUtil.findById(userId);
        if (emitter != null) {
            sseEmitterUtil.deleteById(userId);
            log.info("[removeEmitter] SSE 접속 제거 완료(SSE connection successfully removed) ===>  userId: {}", userId);
        }
    }

    public boolean disconnect(final Long userId) {
        SseEmitter sseEmitter = sseEmitterUtil.findById(userId);
        if (sseEmitter == null) {
            log.warn("[disconnect] SSE emitter 조회 실패(SSE emitter not found for userId) ===> userId: {}", userId);
            return false;
        }
        sseEmitter.complete();
        removeEmitter(userId);
        return true;

    }
}
