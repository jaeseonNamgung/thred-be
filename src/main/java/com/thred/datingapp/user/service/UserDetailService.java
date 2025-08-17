package com.thred.datingapp.user.service;

import com.thred.datingapp.common.entity.user.UserDetail;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.user.repository.UserDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserDetailService {
  private final UserDetailRepository userDetailRepository;

  public UserDetail getByUserId(final Long userId) {
    return userDetailRepository.findByUserId(userId)
                               .orElseThrow(() -> {
                                 log.error("[getByUserId] 해당 유저가 존재하지 않습니다. ===> userId: {}", userId);
                                 return new CustomException(UserErrorCode.USER_NOT_FOUND);
                               });
  }

  public UserDetail getByUserIdFetchUserInfo(final Long userId) {
    return userDetailRepository.findByUserIdFetchUserInfo(userId)
                               .orElseThrow(() -> {
                                 log.error("[getByUserIdFetchUserInfo] 해당 유저가 존재하지 않습니다. ===> userId: {}", userId);
                                 return new CustomException(UserErrorCode.USER_NOT_FOUND);
                               });
  }

  @Transactional
  public void deleteByUserId(final Long userId) {
    if(userId == null) {
      log.error("[deleteByUserId] userId is Null");
      throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    }
    userDetailRepository.deleteByUserId(userId);
  }

  @Transactional
  public void save(final UserDetail userDetail) {
    userDetailRepository.save(userDetail);
  }
}
