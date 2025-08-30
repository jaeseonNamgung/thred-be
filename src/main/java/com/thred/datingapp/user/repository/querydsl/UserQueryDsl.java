package com.thred.datingapp.user.repository.querydsl;

import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.entity.user.field.Gender;
import com.thred.datingapp.common.entity.user.field.LoginType;
import com.thred.datingapp.common.entity.user.field.Smoke;
import com.thred.datingapp.user.api.request.BlockInfoRequest;

import java.util.List;
import java.util.Optional;

public interface UserQueryDsl {
  Optional<User> findMatchBySmokePreference(Long userId, List<String> blocks, Gender gender, List<Long> ids,
                                            String city, Smoke smoke);

  List<User> findBlockedUsersByPhoneNumberAndName(List<BlockInfoRequest> blockInfoRequests);
}
