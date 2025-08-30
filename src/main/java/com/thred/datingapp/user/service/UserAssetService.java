package com.thred.datingapp.user.service;

import com.thred.datingapp.common.entity.inApp.UserAsset;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.InAppErrorCode;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.inApp.repository.UserAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserAssetService {

  private final UserAssetRepository userAssetRepository;

  public int getTotalThreadByUserId(final Long userId) {
    if (userId == null) {
      log.error("[getTotalThreadByUserId] userId is Null");
      throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    }
    return userAssetRepository.findTotalThreadByUserId(userId).orElse(0);
  }

  public UserAsset getUserAsset(final User user) {
    return userAssetRepository.findUserAssetByUserId(user.getId())
                              .orElseGet(() -> UserAsset.builder().totalThread(0).user(user).build()); // Default UserAsset 생성

  }
  public UserAsset getUserAsset(final Long userId) {
    return userAssetRepository.findUserAssetByUserId(userId).orElseThrow(() -> {
      log.error("[getUserAssetByUserId] UserAsset가 존재하지 않습니다. (Not exist userAsset) ===> userId: {}", userId);
      return new CustomException(InAppErrorCode.NOT_EXIST_THREAD);
    });
  }

  @Transactional
  public void save(final UserAsset userAsset) {
    userAssetRepository.save(userAsset);
    log.debug("[save] UserAsset 저장 완료 ===> userAssetId: {}", userAsset.getId());
  }

  @Transactional
  public void deleteByUserId(final Long userId) {
    if (userId == null) {
      log.error("[deleteByUserId] userId is Null");
      throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    }
    userAssetRepository.deleteByUserId(userId);
    log.debug("[deleteByUserId] UserAsset 삭제 완료 ===> userId: {}", userId);
  }
}
