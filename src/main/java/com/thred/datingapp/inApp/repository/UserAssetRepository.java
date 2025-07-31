package com.thred.datingapp.inApp.repository;

import com.thred.datingapp.common.entity.inApp.UserAsset;
import com.thred.datingapp.inApp.repository.queryDsl.UserAssetQueryDsl;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAssetRepository extends JpaRepository<UserAsset, Long>, UserAssetQueryDsl {

  @Query("select ua from UserAsset ua where ua.user.id = :userId")
  Optional<UserAsset> findUserAssetByUserId(@Param("userId") Long userId);

  @Query("select ua.totalThread from UserAsset ua where ua.user.id = :userId")
  Optional<Integer> findTotalThreadByUserId(@Param("userId") Long userId);

  @Modifying(clearAutomatically = true)
  @Query("delete from UserAsset ur where ur.user.id = :userId")
  void deleteByUserId(@Param("userId") Long userId);
}
