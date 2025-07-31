package com.thred.datingapp.common.entity.community.type;

import com.google.common.base.Functions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum CommunityType {
  REAL_TIME("realTime","실시간"),
  HOT("hot","핫");

  public final String name;
  public final String description;

  private static final Map<String, CommunityType> descriptions =
          Collections.unmodifiableMap(Stream.of(values())
                  .collect(Collectors.toMap(CommunityType::getName, Functions.identity())));

  public static CommunityType findType(String name) {
    return Optional.ofNullable(descriptions.get(name)).orElse(REAL_TIME);
  }
}
