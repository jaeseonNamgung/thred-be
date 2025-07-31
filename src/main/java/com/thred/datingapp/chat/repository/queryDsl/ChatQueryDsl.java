package com.thred.datingapp.chat.repository.queryDsl;

import com.thred.datingapp.common.entity.chat.Chat;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ChatQueryDsl {

  Page<Chat> findChatsByChatRoomIdWithPaging(Long chatRoomId, Long pageLastId, int pageSize);
  List<Chat> findChatsByRoomId(Long chatRoomId, Long senderId);
  Long countUnReadChatMessageByReceiverId(Long receiverId);
}
