package com.thred.datingapp.user.repository;

import com.thred.datingapp.common.entity.user.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserDetailRepository extends JpaRepository<UserDetail, Long>{

  @Query("select ud from UserDetail ud where ud.user.id = :userId")
  Optional<UserDetail> findByUserId(@Param("userId") Long userId);

  @Query("select ud from UserDetail ud join fetch ud.user u join fetch u.profiles where ud.user.id=:userId")
  Optional<UserDetail> findByUserIdFetchUserInfo(@Param("userId") Long userId);

  @Modifying(clearAutomatically = true)
  @Query("delete from UserDetail d where d.user.id in :userIds")
  void deleteAllByUserId(@Param("userIds") List<Long> userIds);

  @Modifying(clearAutomatically = true)
  @Query("delete from UserDetail ud where ud.user.id = :userId")
  void deleteByUserId(@Param("userId")Long userId);
}
