package com.thred.datingapp.main.repository;

import com.thred.datingapp.common.entity.card.Card;
import com.thred.datingapp.main.repository.querydsl.CardQueryDsl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long>, CardQueryDsl {

  @Query("select c from Card c where c.profileUser.id = :profileUserId")
  Optional<Card> findByProfileUserId(@Param("profileUserId") Long profileUserId);

  @Modifying(clearAutomatically = true)
  @Query("delete from Card c where c.id = :cardId")
  void deleteById(@Param("cardId") Long cardId);
}
