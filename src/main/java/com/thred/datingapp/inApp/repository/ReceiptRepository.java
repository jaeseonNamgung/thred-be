package com.thred.datingapp.inApp.repository;

import com.thred.datingapp.common.entity.inApp.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

  @Query("select r from Receipt r join fetch r.userAsset join fetch r.product where r.transactionId = :transactionId")
  Optional<Receipt> findByTransactionId(@Param("transactionId") String transactionId);

  @Modifying(clearAutomatically = true)
  @Query("delete from Receipt r where r.transactionId = :transactionId")
  void deleteByTransactionId(@Param("transactionId") String transactionId);

  @Modifying(clearAutomatically = true)
  @Query("delete from Receipt r where r.user.id = :userId")
  void deleteByUserId(@Param("userId") Long userId);
}
