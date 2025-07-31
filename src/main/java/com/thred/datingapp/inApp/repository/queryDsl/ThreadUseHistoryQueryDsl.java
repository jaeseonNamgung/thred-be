package com.thred.datingapp.inApp.repository.queryDsl;

import com.thred.datingapp.common.entity.inApp.type.PurchaseType;
import com.thred.datingapp.inApp.dto.response.ThreadUseHistoryResponse;
import org.springframework.data.domain.Page;

public interface ThreadUseHistoryQueryDsl {
  Page<ThreadUseHistoryResponse> findHistoryAllByUserIdWithPaging(Long userId, Long pageLastId, int pageSize);
  boolean existsByUserIdAndTargetUserIdAndTargetItemId(Long userId, Long purchaseTargetUserId, PurchaseType purchaseType);
}
