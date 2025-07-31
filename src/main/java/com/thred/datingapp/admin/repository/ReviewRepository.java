package com.thred.datingapp.admin.repository;

import com.thred.datingapp.admin.repository.queryDsl.ReviewQueryDsl;
import com.thred.datingapp.common.entity.admin.Review;
import com.thred.datingapp.common.entity.admin.ReviewStatus;
import com.thred.datingapp.common.entity.admin.ReviewType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewQueryDsl {

  @Query("select j from Review j where j.user.id=:userId and j.reviewType=:reviewType")
  Optional<Review> findByUserIdAndReviewType(@Param("userId")Long userId, @Param("reviewType") ReviewType reviewType);

  @Modifying(clearAutomatically = true)
  @Query("delete from Review j where j.user.id = :userId and j.reviewType = :reviewType")
  void deleteByUserIdAndReviewType(@Param("userId")Long userId, @Param("reviewType") ReviewType reviewType);

  @Query("select j from Review j join fetch j.user where j.id=:id")
  Optional<Review> findByIdFetchUser(@Param("id") Long id);

  @Modifying(clearAutomatically = true)
  @Query("delete from Review r where r.id = :reviewId")
  void deleteById(@Param("reviewId") Long reviewId);

}
