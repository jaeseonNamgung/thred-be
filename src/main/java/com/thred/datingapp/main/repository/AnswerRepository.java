package com.thred.datingapp.main.repository;

import com.thred.datingapp.common.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

  @Query("select a from Answer a where a.sender.id = :senderId and a.receiver.id = :receiverId")
  Optional<Answer> findBySenderIdAndReceiverId(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

}
