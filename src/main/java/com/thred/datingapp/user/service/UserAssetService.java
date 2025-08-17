package com.thred.datingapp.user.service;

import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.inApp.repository.UserAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserAssetService {

  private final UserAssetRepository userAssetRepository;

  public int getTotalThreadByUserId(final Long userId) {
    if (userId == null) {
      log.debug("[getTotalThreadByUserId] userId is Null");
      throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    }
    return userAssetRepository.findTotalThreadByUserId(userId).orElse(0);
  }
}
