package com.thred.datingapp.main.repository.querydsl;

import com.thred.datingapp.main.dto.response.CardOpenResponse;
import org.springframework.data.domain.Page;

public interface CardOpenQueryDsl {
  Page<CardOpenResponse> findCardOpenResponseByOpenerIdWithPaging(Long openerId, long pageLastId, int pageSize);
  boolean existsByOpenerIdAndCardId(Long openerId, Long cardId);
}
