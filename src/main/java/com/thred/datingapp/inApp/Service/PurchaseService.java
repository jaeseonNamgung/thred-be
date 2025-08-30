package com.thred.datingapp.inApp.Service;

import com.thred.datingapp.common.api.response.PageResponse;
import com.thred.datingapp.common.entity.inApp.Product;
import com.thred.datingapp.common.entity.inApp.ThreadUseHistory;
import com.thred.datingapp.common.entity.inApp.UserAsset;
import com.thred.datingapp.common.entity.inApp.type.InAppType;
import com.thred.datingapp.common.entity.inApp.type.PurchaseType;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.InAppErrorCode;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.inApp.dto.VerifiedProductDto;
import com.thred.datingapp.inApp.dto.request.ReceiptRequest;
import com.thred.datingapp.inApp.dto.request.ThreadRequest;
import com.thred.datingapp.inApp.dto.response.ProductResponse;
import com.thred.datingapp.inApp.dto.response.ThreadUseHistoryResponse;
import com.thred.datingapp.inApp.repository.ProductRepository;
import com.thred.datingapp.user.service.UserAssetService;
import com.thred.datingapp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PurchaseService {

  private final AppleInAppService  appleInAppService;
  private final GoogleInAppService googleInAppService;

  private final UserService             userService;
  private final ProductService          productService;
  private final ReceiptService          receiptService;
  private final UserAssetService        userAssetService;
  private final ThreadUseHistoryService threadUseHistoryService;

  @Transactional
  public boolean processInAppPurchase(final Long userId, final ReceiptRequest receiptRequest) {
    User user = userService.getUserById(userId);

    // 1. 인앱 영수증 검증
    VerifiedProductDto verifiedProductDto;
    if (receiptRequest.inAppType().equals(InAppType.APPLE)) {
      verifiedProductDto = appleInAppService.verifyReceipt(receiptRequest.receiptData());
    } else {
      verifiedProductDto = googleInAppService.verifyReceipt(receiptRequest.productId(), receiptRequest.receiptData());
    }

    if (verifiedProductDto == null) {
      log.error("[savePurchaseHistory] verifiedProductDto is null ===> inAppType: {}", receiptRequest.inAppType());
      throw new CustomException(InAppErrorCode.RECEIPT_SIGNATURE_VERIFICATION_ERROR);
    }

    log.debug("[savePurchaseHistory] 인앱 영수증 검증 성공 ===> inAppType: {}, productId: {}, transactionId: {}", receiptRequest.inAppType(),
              verifiedProductDto.productId(), verifiedProductDto.transactionId());

    // 2. 영수증 저장
    Product product = productService.getByProductId(verifiedProductDto.productId());
    receiptService.save(verifiedProductDto.toReceiptEntity(user, product));
    log.debug("[savePurchaseHistory] 영수증 저장 성공 ===> inAppType: {}, productId: {}", receiptRequest.inAppType(), verifiedProductDto.productId());

    // 3. 유저 실타래 수량 업데이트
    UserAsset userAsset = userAssetService.getUserAsset(user);
    userAsset.quantityAddThread(product.getQuantityThread());
    userAssetService.save(userAsset);

    // 4. 실타래 충전 기록 저장
    ThreadUseHistory threadUseHistory = ThreadUseHistory.builder()
        .purchaseTargetUserId(0L)
        .purchaseType(PurchaseType.THREAD_TOP_UP)
        .amount(product.getQuantityThread())
        .user(user)
        .build();
    threadUseHistoryService.save(threadUseHistory);
    log.info("[savePurchaseHistory] 영수증 검증 및 영수증 저장, 실타래 수량 업데이트 성공 ===> inAppType: {}, productId: {}, transactionId: {}, totalThread:{}",
             receiptRequest.inAppType(), verifiedProductDto.productId(), verifiedProductDto.transactionId(), userAsset.getTotalThread());
    return true;
  }

  @Transactional
  public boolean useThread(final Long userId, final ThreadRequest threadRequest) {

    // 1. 실타래 수량 차감
    UserAsset userAsset = userAssetService.getUserAsset(userId);

    PurchaseType purchaseType = PurchaseType.findType(threadRequest.purchaseType());
    // 2. 실타래 수량이 부족한 경우
    if (userAsset.getTotalThread() < purchaseType.getAmount()) {
      log.error("[useThread] 실타래가 부족합니다.(Insufficient thread count.) ===> productThreadCount: {}, userTotalThread: {}", purchaseType.getAmount(),
                userAsset.getTotalThread());
      throw new CustomException(InAppErrorCode.INSUFFICIENT_THREAD_COUNT);
    }

    // 3. 실타래 수량 업데이트 (실타래 차감)
    userAsset.quantityRemoveThread(purchaseType.getAmount());
    log.info("[useThread] 실타래 차감(Successful remove thread) ===>  userId: {}, amountThread: {}, userTotalThread: {}", userId, purchaseType.getAmount(),
             userAsset.getTotalThread());

    // 4. 실타래 기록 저장
    ThreadUseHistory threadUseHistory = threadRequest.toEntity(userAsset.getUser());
    threadUseHistoryService.save(threadUseHistory);
    log.info("[useThread] 실타래 이력 저장(Successful save thread use history) ===>  purchaseTartId: {}, purchaseType: {}",
             threadRequest.purchaseTargetUserId(), threadRequest.purchaseType());
    return true;
  }

  public PageResponse<ThreadUseHistoryResponse> getAllThreadUseHistories(final Long userId, final Long pageLastId, final int pageSize) {
    Page<ThreadUseHistoryResponse> page = threadUseHistoryService.getHistoryAllByUserIdWithPaging(userId, pageLastId, pageSize);
    return PageResponse.of(page.getSize(), page.isLast(), page.getContent());
  }

  @Transactional
  public void updateThreadQuantityByReferralCode(final User referredUser, final User referrerUser) {
    UserAsset referredUserAsset = UserAsset.builder().totalThread(PurchaseType.REFERRAL_CODE.getAmount()).user(referredUser).build();
    userAssetService.save(referredUserAsset);
    log.debug("[updateThreadQuantityByReferralCode] ReferredUserAsset 생성 ===> referredUserId: {}, totalThread: {} ", referredUserAsset.getId(),
              referredUserAsset.getTotalThread());

    UserAsset referrerAsset = userAssetService.getUserAsset(referrerUser);
    referrerAsset.quantityAddThread(PurchaseType.REFERRAL_CODE.getAmount());
    userAssetService.save(referrerAsset);
    log.debug("[updateThreadQuantityByReferralCode] ReferrerUserAsset 생성 ===> referrerUserId: {}, totalThread: {} ", referrerAsset.getId(),
              referrerAsset.getTotalThread());

    ThreadUseHistory referredUserUseHistory = ThreadUseHistory.builder().purchaseType(PurchaseType.REFERRAL_CODE).user(referredUser).build();
    ThreadUseHistory referrerUserUseHistory = ThreadUseHistory.builder().purchaseType(PurchaseType.REFERRAL_CODE).user(referrerUser).build();

    threadUseHistoryService.save(referredUserUseHistory);
    threadUseHistoryService.save(referrerUserUseHistory);
    log.debug("[updateThreadQuantityByReferralCode] ThreadUseHistory 생성 ===> referredUserUseHistoryId: {}, referrerUserUseHistoryId: {} ",
              referredUserUseHistory.getId(), referrerUserUseHistory.getId());
    log.info("[updateThreadQuantityByReferralCode] 추천인 코드 실타래 수량 업데이트 로직 성공 ===> referredUserId: {}, referrerUserId: {} ", referredUser.getId(),
             referrerUser.getId());
  }

  public boolean existsThreadUseHistory(final Long userId, final Long purchaseTargetUserId, final String purchaseTypeStr) {
    PurchaseType purchaseType = PurchaseType.findType(purchaseTypeStr);
    return threadUseHistoryService.existsByUserIdAndTargetUserIdAndTargetItemId(userId, purchaseTargetUserId, purchaseType);
  }

  @Transactional
  public void deleteAllPurchaseHistoriesForWithdrawnUser(final Long userId) {
    receiptService.deleteByUserId(userId);
    threadUseHistoryService.deleteByUserId(userId);
    userAssetService.deleteByUserId(userId);
    log.info("[deleteAllPurchaseHistory] 구매 정보 전체 삭제 완료 ===> userId: {} ", userId);
  }

  // 추천인 코드
  @Transactional
  public void joinCodeEvent(final Long referredUserId, final String code) {
    User referrerUser = userService.getByCode(code);
    User referredUser = userService.getUserById(referredUserId);
    updateThreadQuantityByReferralCode(referredUser, referrerUser);
  }
}
