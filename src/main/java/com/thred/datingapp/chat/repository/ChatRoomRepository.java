package com.thred.datingapp.chat.repository;

import com.thred.datingapp.chat.repository.queryDsl.ChatRoomQueryDsl;
import com.thred.datingapp.common.entity.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomQueryDsl {

  @Modifying(clearAutomatically = true)
  @Query("delete from ChatRoom cr where cr.id in :chatRoomIds")
  void deleteAllByChatRoomId(@Param("chatRoomIds") List<Long> chatRoomIds);

  @Modifying(clearAutomatically = true)
  @Query("delete from ChatRoom cr where cr.id = :chatRoomId")
  void deleteByChatRoomId(@Param("chatRoomId")Long chatRoomId);
}

