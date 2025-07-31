package com.thred.datingapp.chat.repository;

import com.thred.datingapp.chat.repository.queryDsl.ChatPartQueryDsl;
import com.thred.datingapp.common.entity.chat.ChatPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatPartRepository extends JpaRepository<ChatPart, Long>, ChatPartQueryDsl {

  @Query("select cp.chatRoom.id from ChatPart cp left join cp.chatRoom where cp.user.id = :userId")
  List<Long> findChatRoomIdAllByUserId(@Param("userId") Long userId);

  @Modifying(clearAutomatically = true)
  @Query("delete from ChatPart cp where cp.chatRoom.id in :chatRoomIds")
  void deleteAllByChatRoomIds(@Param("chatRoomIds")List<Long> chatRoomIds);

  @Modifying(clearAutomatically = true)
  @Query("delete from ChatPart cp where cp.chatRoom.id = :chatRoomId")
  void deleteAllByChatRoomId(@Param("chatRoomId") Long chatRoomId);

  @Modifying(clearAutomatically = true)
  @Query("delete from ChatPart cp where cp.chatRoom.id = :chatRoomId and cp.user.id = :userId")
  void deleteByChatRoomIdAndUserId(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);
}
