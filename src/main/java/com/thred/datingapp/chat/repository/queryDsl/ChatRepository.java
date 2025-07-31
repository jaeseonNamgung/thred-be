package com.thred.datingapp.chat.repository.queryDsl;

import com.thred.datingapp.common.entity.chat.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long>, ChatQueryDsl {
  @Modifying(clearAutomatically = true)
  @Query("delete from Chat c where c.chatPart.chatRoom.id in :chatRoomIds")
  void deleteAllByChatRoomIds(@Param("chatRoomIds") List<Long> chatRoomIds);

  @Modifying(clearAutomatically = true)
  @Query("update Chat c set c.readStatus = true where c.chatPart.id = :chatPartId")
  void updateReadStatusByChatPartId(@Param("chatPartId")Long chatPartId);

  @Modifying(clearAutomatically = true)
  @Query("delete from Chat c where c.chatPart.chatRoom.id = :chatRoomId")
  void deleteAllByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}
