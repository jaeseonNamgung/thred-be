package com.thred.datingapp.inApp.repository;

import com.thred.datingapp.common.entity.inApp.ThreadUseHistory;
import com.thred.datingapp.inApp.repository.queryDsl.ThreadUseHistoryQueryDsl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThreadUseHistoryRepository extends JpaRepository<ThreadUseHistory, Long>, ThreadUseHistoryQueryDsl {

  @Modifying(clearAutomatically = true)
  @Query("delete from ThreadUseHistory t where t.user.id = :userId")
  void deleteByUserId(@Param("userId") Long userId);
}
