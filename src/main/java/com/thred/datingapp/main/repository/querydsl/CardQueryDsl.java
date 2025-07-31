package com.thred.datingapp.main.repository.querydsl;

import com.thred.datingapp.common.entity.user.field.Gender;
import com.thred.datingapp.user.api.response.CardProfileResponse;

import java.util.List;

public interface CardQueryDsl {
  List<CardProfileResponse> findTodayRandomCardByViewerIdGenderCity(Long viewerId, Gender gender, String city);
  boolean existsByProfileUserId(Long profileUserId);
}
