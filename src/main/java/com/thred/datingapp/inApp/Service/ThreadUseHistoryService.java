package com.thred.datingapp.inApp.Service;

import com.thred.datingapp.common.entity.inApp.ThreadUseHistory;
import com.thred.datingapp.common.entity.inApp.type.PurchaseType;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.inApp.dto.response.ThreadUseHistoryResponse;
import com.thred.datingapp.inApp.repository.ThreadUseHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ThreadUseHistoryService {

  private final ThreadUseHistoryRepository threadUseHistoryRepository;

  @Transactional
  public void save(final ThreadUseHistory threadUseHistory) {
    threadUseHistoryRepository.save(threadUseHistory);
    log.debug("[save] threadUseHistory 저장 완료 ===> threadUseHistoryId: {}", threadUseHistory.getId());
  }

  public Page<ThreadUseHistoryResponse> getHistoryAllByUserIdWithPaging(final Long userId, final Long pageLastId, final int pageSize) {
    return threadUseHistoryRepository.findHistoryAllByUserIdWithPaging(userId, pageLastId, pageSize);
  }

  public boolean existsByUserIdAndTargetUserIdAndTargetItemId(final Long userId, final Long purchaseTargetUserId, final PurchaseType purchaseType) {
    return threadUseHistoryRepository.existsByUserIdAndTargetUserIdAndTargetItemId(userId, purchaseTargetUserId, purchaseType);
  }

  @Transactional
  public void deleteByUserId(final Long userId) {
    if(userId == null) {
      log.error("[deleteByUserId] userId is Null");
      throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    }
    threadUseHistoryRepository.deleteByUserId(userId);
    log.debug("[deleteByUserId] threadUseHistory 삭제 완료 ===> userId: {}", userId);
  }
}
