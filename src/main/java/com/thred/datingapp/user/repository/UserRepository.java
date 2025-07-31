package com.thred.datingapp.user.repository;

import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.entity.user.field.Role;
import com.thred.datingapp.user.repository.querydsl.UserQueryDsl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserQueryDsl {

  Optional<User> findByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByCode(String code);

  boolean existsByEmail(String email);

  Optional<User> findAdminByEmailAndRole(String email, Role role);

  @Query("select u from User u where u.email = :email and u.certification = false")
  Optional<User> findByEmailAndCertificationFalse(String email);

  Optional<User> findByCode(String code);

  @Query("select u.id from User u where u.userState = 'WITHDRAW_REQUESTED' and u.withdrawRequestDate <= :beforeDate")
  List<Long> findIdsByWithdrawRequestedBeforeDate(@Param("beforeDate") LocalDate beforeDate);

  @Modifying(clearAutomatically = true)
  @Query("delete from User u where u.id = :userId")
  void deleteByUserId(@Param("userId") Long userId);

  @Query("select u.username from User u where u.id = :userId")
  String getUsernameById(@Param("userId") Long userId);

  @Query("select u from User u where u.socialId = :socialId")
  Optional<User> findBySocialId(@Param("socialId") Long socialId);
}
