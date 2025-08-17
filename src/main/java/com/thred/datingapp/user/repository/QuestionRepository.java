package com.thred.datingapp.user.repository;

import com.thred.datingapp.common.entity.user.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

  @Modifying(clearAutomatically = true)
  @Query("delete from Question q where q.user.id = :userId")
  void deleteByUserId(@Param("userId") Long userId);

  @Query("select q from Question q where q.user.id = :userId")
  Optional<Question> findByUserId(@Param("userId") Long userId);

}
