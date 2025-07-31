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
import com.thred.datingapp.inApp.repository.ReceiptRepository;
import com.thred.datingapp.inApp.repository.ThreadUseHistoryRepository;
import com.thred.datingapp.inApp.repository.UserAssetRepository;
import com.thred.datingapp.user.repository.UserRepository;
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

  private final UserRepository    userRepository;
  private final ProductRepository productRepository;
  private final ReceiptRepository          receiptRepository;
  private final UserAssetRepository        assetRepository;
  private final ThreadUseHistoryRepository threadUseHistoryRepository;
  private final UserAssetRepository        userAssetRepository;

  @Transactional
  public boolean processInAppPurchase(final Long userId, final ReceiptRequest receiptRequest) {
    User user = userRepository.findById(userId)
                              .orElseThrow(() -> {
                                   log.error("[savePurchaseHistory] 존재하지 않은 회원입니다.(Not exist user) ===> userId: {}", userId);
                                   return new CustomException(UserErrorCode.USER_NOT_FOUND);
                                 });

    // 1. 인앱 영수증 검증
    VerifiedProductDto verifiedProductDto;
    if (receiptRequest.inAppType()
                      .equals(InAppType.APPLE)) {
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
    Product product = productRepository.findByInAppProductId(verifiedProductDto.productId())
                                       .orElseThrow(() -> {
                                         log.error("[savePurchaseHistory] 존재하지 않은 상품입니다.(Not exist product) ===> productId: {}",
                                                   verifiedProductDto.productId());
                                         return new CustomException(InAppErrorCode.PURCHASE_ERROR);
                                       });
    receiptRepository.save(verifiedProductDto.toReceiptEntity(user, product));
    log.debug("[savePurchaseHistory] 영수증 저장 성공 ===> inAppType: {}, productId: {}", receiptRequest.inAppType(), verifiedProductDto.productId());

    // 3. 유저 실타래 수량 업데이트
    UserAsset userAsset = assetRepository.findUserAssetByUserId(userId)
                                         .orElseGet(() -> UserAsset.builder()
                                                                   .totalThread(0)
                                                                   .user(user)
                                                                   .build()); // Default UserAsset 생성
    userAsset.quantityAddThread(product.getQuantityThread());
    assetRepository.save(userAsset);
    log.info("[savePurchaseHistory] 영수증 검증 및 영수증 저장, 실타래 수량 업데이트 성공 ===> inAppType: {}, productId: {}, transactionId: {}, totalThread:{}",
             receiptRequest.inAppType(), verifiedProductDto.productId(), verifiedProductDto.transactionId(), userAsset.getTotalThread());
    return true;
  }

  @Transactional
  public boolean useThread(final Long userId, final ThreadRequest threadRequest) {

    // 1. 실타래 수량 차감
    UserAsset userAsset = userAssetRepository.findUserAssetByUserId(userId)
                                             .orElseThrow(() -> {
                                               log.error("[useThread] UserAsset가 존재하지 않습니다. (Not exist userAsset) ===> userId: {}", userId);
                                               return new CustomException(InAppErrorCode.NOT_EXIST_THREAD);
                                             });

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
    threadUseHistoryRepository.save(threadUseHistory);
    log.info("[useThread] 실타래 이력 저장(Successful save thread use history) ===>  purchaseTartId: {}, purchaseType: {}",
             threadRequest.purchaseTargetUserId(), threadRequest.purchaseType());
    return true;
  }

  public PageResponse<ThreadUseHistoryResponse> getAllThreadUseHistories(final Long userId, final Long pageLastId, final int pageSize) {
    Page<ThreadUseHistoryResponse> page = threadUseHistoryRepository.findHistoryAllByUserIdWithPaging(userId, pageLastId, pageSize);
    return PageResponse.of(page.getSize(), page.isLast(), page.getContent());
  }

  public List<ProductResponse> getAllProducts() {
    return productRepository.findAll()
                            .stream()
                            .map(ProductResponse::toEntity)
                            .toList();
  }

  public int getTotalThread(final Long userId) {
    return userAssetRepository.findTotalThreadByUserId(userId)
                              .orElse(0);
  }

  @Transactional
  public void updateThreadQuantityByReferralCode(final User referredUser, final User referrerUser) {
    UserAsset referredUserAsset = UserAsset.builder()
                                           .totalThread(PurchaseType.REFERRAL_CODE.getAmount())
                                           .user(referredUser)
                                           .build();
    userAssetRepository.save(referredUserAsset);
    log.debug("[updateThreadQuantityByReferralCode] ReferredUserAsset 생성 ===> referredUserId: {}, totalThread: {} ", referredUserAsset.getId(),
              referredUserAsset.getTotalThread());
    UserAsset referrerAsset = userAssetRepository.findUserAssetByUserId(referrerUser.getId())
                                                 .orElse(UserAsset.builder()
                                                                  .totalThread(0)
                                                                  .user(referrerUser)
                                                                  .build());
    referrerAsset.quantityAddThread(PurchaseType.REFERRAL_CODE.getAmount());
    userAssetRepository.save(referrerAsset);
    log.debug("[updateThreadQuantityByReferralCode] ReferrerUserAsset 생성 ===> referrerUserId: {}, totalThread: {} ", referrerAsset.getId(),
              referrerAsset.getTotalThread());
    ThreadUseHistory referredUserUseHistory = ThreadUseHistory.builder()
                                                              .purchaseType(PurchaseType.REFERRAL_CODE)
                                                              .user(referredUser)
                                                              .build();
    ThreadUseHistory referrerUserUseHistory = ThreadUseHistory.builder()
                                                              .purchaseType(PurchaseType.REFERRAL_CODE)
                                                              .user(referrerUser)
                                                              .build();

    threadUseHistoryRepository.save(referredUserUseHistory);
    threadUseHistoryRepository.save(referrerUserUseHistory);
    log.debug("[updateThreadQuantityByReferralCode] ThreadUseHistory 생성 ===> referredUserUseHistoryId: {}, referrerUserUseHistoryId: {} ",
              referredUserUseHistory.getId(), referrerUserUseHistory.getId());
    log.info("[updateThreadQuantityByReferralCode] 추천인 코드 실타래 수량 업데이트 로직 성공 ===> referredUserId: {}, referrerUserId: {} ", referredUser.getId(),
             referrerUser.getId());
  }

  public boolean existsThreadUseHistory(final Long userId,
                                        final Long purchaseTargetUserId,
                                        final String purchaseTypeStr) {
    PurchaseType purchaseType = PurchaseType.findType(purchaseTypeStr);
    return threadUseHistoryRepository.existsByUserIdAndTargetUserIdAndTargetItemId(userId, purchaseTargetUserId, purchaseType);
  }

  @Transactional
  public void deleteAllPurchaseHistoriesForWithdrawnUser(final Long userId){
    receiptRepository.deleteByUserId(userId);
    threadUseHistoryRepository.deleteByUserId(userId);
    userAssetRepository.deleteByUserId(userId);
    log.info("[deleteAllPurchaseHistory] 구매 정보 전체 삭제 완료 ===> userId: {} ", userId);
  }
}
