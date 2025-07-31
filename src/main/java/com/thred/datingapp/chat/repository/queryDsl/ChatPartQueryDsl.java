package com.thred.datingapp.chat.repository.queryDsl;

import com.thred.datingapp.common.entity.chat.ChatPart;

import java.util.List;
import java.util.Optional;

public interface ChatPartQueryDsl {

    List<ChatPart> findByRoomId(Long roomId);
    Optional<ChatPart> findByChatRoomIdAndOtherConnectorId(Long chatRoomId, Long otherConnectorId);
    Optional<ChatPart> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    List<ChatPart> findByChatRoomId(Long chatRoomId);
}
