package com.thred.datingapp.chat.repository;

import com.thred.datingapp.chat.repository.queryDsl.FcmTokenQueryDsl;
import com.thred.datingapp.common.entity.chat.FcmToken;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long>, FcmTokenQueryDsl {

  @Modifying
  @Query("delete from FcmToken f where f.user.id in :userIds")
  void deleteFcmTokensByUserIds(@Param("userIds") List<Long> userIds);

  @Modifying
  @Query("delete from FcmToken f where f.user.id = :userId")
  void deleteByUserId(@Param("userId") Long userId);
}
