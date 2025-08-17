package com.thred.datingapp.user.service;

import com.thred.datingapp.chat.repository.FcmTokenRepository;
import com.thred.datingapp.common.entity.chat.FcmToken;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class FcmTokenService {

  private final FcmTokenRepository fcmTokenRepository;

  @Transactional
  public void save(final FcmToken fcmToken) {
    FcmToken savedFcmToken = fcmTokenRepository.save(fcmToken);
    log.debug("[fcmTokenService - save] fcmToken 저장 완료 ===> fcmTokenId: {}", savedFcmToken.getId());
  }

  @Transactional
  public void deleteByUserId(final Long userId) {
    if (userId == null) {
      log.debug("[deleteByUserId] userId is Null");
      throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    }
    fcmTokenRepository.deleteByUserId(userId);
    log.debug("[deleteByUserId] fcmToken 삭제 완료 ===> userId: {}", userId);
  }
}
