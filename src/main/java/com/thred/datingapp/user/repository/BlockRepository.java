package com.thred.datingapp.user.repository;

import com.thred.datingapp.common.entity.user.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BlockRepository extends JpaRepository<Block, Long> {

  @Query("select b from Block b join fetch b.blockedUser where b.blocker.id = :blockerId")
  List<Block> findByBlockerId(@Param("blockerId") Long blockerId);

  @Modifying(clearAutomatically = true)
  @Query("delete from Block b where b.blocker.id = :blockerId")
  void deleteByBlockerId(@Param("blockerId") Long blockerId);

  @Modifying(clearAutomatically = true)
  @Query("delete from Block b where b.blocker.id in :userIds")
  void deleteByUserIds(List<Long> userIds);

  @Modifying(clearAutomatically = true)
  @Query("delete from Block b where b.blocker.id = :userId")
  void deleteByUserId(@Param("userId")Long userId);
}
