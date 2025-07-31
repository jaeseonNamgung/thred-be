package com.thred.datingapp.main.repository;

import com.thred.datingapp.common.entity.card.CardOpen;
import com.thred.datingapp.main.repository.querydsl.CardOpenQueryDsl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CardOpenRepository extends JpaRepository<CardOpen, Long>, CardOpenQueryDsl {

  @Modifying(clearAutomatically = true)
  @Query("delete from CardOpen c where c.opener.id = :openerId and c.card.id = :cardId")
  void deleteByOpenerIdAndCardId(@Param("openerId") Long openerId, @Param("cardId") Long cardId);

  @Query("select co from CardOpen co where co.card.id = :cardId")
  Optional<CardOpen> findByCardId(@Param("cardId") Long cardId);

  @Modifying(clearAutomatically = true)
  @Query("delete from CardOpen c where c.card.id = :cardId or c.opener.id = :openerId")
  void deleteAllByCardIdOrOpenerId(@Param("cardId") Long cardId, @Param("openerId") Long openerId);
}
