package com.thred.datingapp.user.repository;

import com.thred.datingapp.common.entity.user.RefreshToken;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Modifying(clearAutomatically = true)
    @Query("delete from RefreshToken r where r.user.id=:userId")
    void deleteByUserId(@Param("userId") Long userId);

    boolean existsByRefreshToken(@Param("refreshToken") String refreshToken);

    @Query("select r from RefreshToken r where r.user.id = :userId")
    Optional<RefreshToken> findByUserId(@Param("userId") Long userId);

}
