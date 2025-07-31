package com.thred.datingapp.chat.repository.queryDsl;

import com.thred.datingapp.common.entity.chat.ChatRoom;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface ChatRoomQueryDsl {
    boolean existChatRoomByChatRoomId(Long id);
    Page<ChatRoom> findChatRoomsByUserIdWithPagination(Long userId, Long pageLastId, Integer pageSize);
    Optional<ChatRoom> findChatRoomById(Long id);
    Optional<ChatRoom> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    Optional<ChatRoom> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);
}
