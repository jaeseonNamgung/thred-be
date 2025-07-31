package com.thred.datingapp.inApp.Service;

import com.thred.datingapp.common.entity.inApp.type.InAppType;
import com.thred.datingapp.common.entity.inApp.type.RevocationReason;
import com.thred.datingapp.common.entity.inApp.type.TransactionType;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.entity.inApp.Product;
import com.thred.datingapp.common.entity.inApp.UserAsset;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.InAppErrorCode;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.inApp.dto.request.ThreadRequest;
import com.thred.datingapp.inApp.dto.request.ReceiptRequest;
import com.thred.datingapp.inApp.dto.VerifiedProductDto;
import com.thred.datingapp.inApp.repository.ProductRepository;
import com.thred.datingapp.inApp.repository.ReceiptRepository;
import com.thred.datingapp.inApp.repository.ThreadUseHistoryRepository;
import com.thred.datingapp.inApp.repository.UserAssetRepository;
import com.thred.datingapp.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

  @Mock
  private UserRepository             userRepository;
  @Mock
  private UserAssetRepository        assetRepository;
  @Mock
  private ProductRepository          productRepository;
  @Mock
  private ReceiptRepository          receiptRepository;
  @Mock
  private AppleInAppService          inAppService;
  @Mock
  private ThreadUseHistoryRepository threadUseHistoryRepository;

  @InjectMocks
  private PurchaseService sut;

  @Test
  @DisplayName("모든 로직을 성공적으로 수행할 경우 true를 리턴- 이전에 구매한 상품")
  void processInAppPurchase() throws Exception {
    //given
    ReceiptRequest receiptRequest = ReceiptRequest.of("receiptData", InAppType.APPLE);
    User user = createUser();
    ReflectionTestUtils.setField(user, "id", 1L);
    Product product = createProduct();
    UserAsset asset = createAsset(10);
    VerifiedProductDto verifiedProductDto = createSaveProductDto();

    given(userRepository.findById(any())).willReturn(Optional.of(user));
    given(inAppService.verifyReceipt(any())).willReturn(verifiedProductDto);
    given(productRepository.findByInAppProductId(any())).willReturn(Optional.of(product));
    given(assetRepository.findUserAssetByUserId(any())).willReturn(Optional.of(asset));

    //when
    boolean expectedBool = sut.processInAppPurchase(1L, receiptRequest);

    //then
    assertTrue(expectedBool);

    then(userRepository).should().findById(any());
    then(inAppService).should().verifyReceipt(any());
    then(productRepository).should().findByInAppProductId(any());
    then(receiptRepository).should().save(any());
    then(assetRepository).should().findUserAssetByUserId(any());
    then(threadUseHistoryRepository).should().save(any());
  }

  @Test
  @DisplayName("모든 로직을 성공적으로 수행할 경우 true를 리턴- 처음 구매한 상품")
  void processInAppPurchase2() throws Exception {
    //given
    ReceiptRequest receiptRequest = ReceiptRequest.of("receiptData", InAppType.APPLE);
    User user = createUser();
    ReflectionTestUtils.setField(user, "id", 1L);
    Product product = createProduct();
    VerifiedProductDto verifiedProductDto = createSaveProductDto();

    given(userRepository.findById(any())).willReturn(Optional.of(user));
    given(inAppService.verifyReceipt(any())).willReturn(verifiedProductDto);
    given(productRepository.findByInAppProductId(any())).willReturn(Optional.of(product));
    given(assetRepository.findUserAssetByUserId(any())).willReturn(Optional.empty());

    //when
    boolean expectedBool = sut.processInAppPurchase(1L, receiptRequest);
    //then
    assertTrue(expectedBool);

    then(userRepository).should().findById(any());
    then(inAppService).should().verifyReceipt(any());
    then(productRepository).should().findByInAppProductId(any());
    then(receiptRepository).should().save(any());
    then(assetRepository).should().findUserAssetByUserId(any());
    then(assetRepository).should().save(any());
    then(threadUseHistoryRepository).should().save(any());
  }

  @Test
  @DisplayName("회원이 존재하지 않을 때 에러 발생")
  void processInAppPurchase4() throws Exception {
    //given
    ReceiptRequest receiptRequest = ReceiptRequest.of("receiptData", InAppType.APPLE);

    given(userRepository.findById(any())).willReturn(Optional.empty());

    //when & then
    CustomException expectedException = assertThrows(CustomException.class, () -> sut.processInAppPurchase(1L, receiptRequest));

    assertThat(expectedException.getErrorCode().getHttpStatus()).isEqualTo(UserErrorCode.USER_NOT_FOUND.getHttpStatus());
    assertThat(expectedException.getMessage()).isEqualTo(UserErrorCode.USER_NOT_FOUND.getMessage());

    then(userRepository).should().findById(any());
  }

  @Test
  @DisplayName("saveProductDto가 null 값이면 에러 발생")
  void processInAppPurchase5() throws Exception {
    //given
    ReceiptRequest receiptRequest = ReceiptRequest.of("receiptData", InAppType.APPLE);
    User user = createUser();
    ReflectionTestUtils.setField(user, "id", 1L);

    given(userRepository.findById(any())).willReturn(Optional.of(user));
    given(inAppService.verifyReceipt(any())).willReturn(null);

    //when & then
    CustomException expectedException = assertThrows(CustomException.class, () -> sut.processInAppPurchase(1L, receiptRequest));

    assertThat(expectedException.getErrorCode().getHttpStatus()).isEqualTo(InAppErrorCode.RECEIPT_SIGNATURE_VERIFICATION_ERROR.getHttpStatus());
    assertThat(expectedException.getMessage()).isEqualTo(InAppErrorCode.RECEIPT_SIGNATURE_VERIFICATION_ERROR.getMessage());

    then(userRepository).should().findById(any());
    then(inAppService).should().verifyReceipt(any());
  }

  @Test
  @DisplayName("상품이 없을 때 에러 발생")
  void processInAppPurchase6() throws Exception {
    //given
    ReceiptRequest receiptRequest = ReceiptRequest.of("receiptData", InAppType.APPLE);
    User user = createUser();
    ReflectionTestUtils.setField(user, "id", 1L);
    VerifiedProductDto verifiedProductDto = createSaveProductDto();

    given(userRepository.findById(any())).willReturn(Optional.of(user));
    given(inAppService.verifyReceipt(any())).willReturn(verifiedProductDto);
    given(productRepository.findByInAppProductId(any())).willReturn(Optional.empty());

    //when & then
    CustomException expectedException = assertThrows(CustomException.class, () -> sut.processInAppPurchase(1L, receiptRequest));

    assertThat(expectedException.getErrorCode().getHttpStatus()).isEqualTo(InAppErrorCode.PURCHASE_ERROR.getHttpStatus());
    assertThat(expectedException.getMessage()).isEqualTo(InAppErrorCode.PURCHASE_ERROR.getMessage());

    then(userRepository).should().findById(any());
    then(inAppService).should().verifyReceipt(any());
    then(productRepository).should().findByInAppProductId(any());
  }

  /*
   * 1. 회원 실타래 조회 (UserAsset)
   *   1-1. 존재한다면 기존 실타래에서 구매 타입에 맞게 차감
   *   2-1. 존재하지 않는다면 실타래 부족 관련 예외 발생
   * 2. 실타래 사용 히스토리 저장
   *
   * */
  @Test
  @DisplayName("사용자 실타래 사용 이력 저장 테스트 - 성공 시 ture를 리턴")
  void useThreadTest() {
    // given
    UserAsset asset = createAsset(10);
    ThreadRequest threadRequest = new ThreadRequest(1L, "viewProfile");
    given(assetRepository.findUserAssetByUserId(any())).willReturn(Optional.of(asset));

    // when
    boolean expectedBool = sut.useThread(1L, threadRequest);
    // then
    assertTrue(expectedBool);

    then(assetRepository).should().findUserAssetByUserId(any());
    then(threadUseHistoryRepository).should().save(any());
  }

  @Test
  @DisplayName("사용자 실타래 사용 이력 저장 테스트 - userAsset가 존재하지 않을 경우 예외 발생")
  void useThreadTest2() {
    // given
    ThreadRequest threadRequest = new ThreadRequest(1L, "allProfileView");
    given(assetRepository.findUserAssetByUserId(any())).willReturn(Optional.empty());
    // when
    CustomException expectedException = (CustomException) catchException(() -> sut.useThread(1L, threadRequest));
    // then
    assertThat(expectedException.getErrorCode()).hasFieldOrPropertyWithValue("httpStatus", InAppErrorCode.NOT_EXIST_THREAD.getHttpStatus())
                                                .hasFieldOrPropertyWithValue("message", InAppErrorCode.NOT_EXIST_THREAD.getMessage());

    then(assetRepository).should().findUserAssetByUserId(any());
  }

  @Test
  @DisplayName("사용자 실타래 사용 이력 저장 테스트 - 실타래 수량이 부족한 경우 예외 발생")
  void useThreadTest3() {
    // given
    UserAsset asset = createAsset(1);
    ThreadRequest threadRequest = new ThreadRequest(1L, "viewProfile");
    given(assetRepository.findUserAssetByUserId(any())).willReturn(Optional.of(asset));

    // when
    CustomException expectedException = (CustomException) catchException(() -> sut.useThread(1L, threadRequest));
    // then
    assertThat(expectedException.getErrorCode()).hasFieldOrPropertyWithValue("httpStatus", InAppErrorCode.INSUFFICIENT_THREAD_COUNT.getHttpStatus())
                                                .hasFieldOrPropertyWithValue("message", InAppErrorCode.INSUFFICIENT_THREAD_COUNT.getMessage());

    then(assetRepository).should().findUserAssetByUserId(any());
  }

  @Test
  @DisplayName("실타래 수량 조회 테스트 - 구매한 실 있음")
  void getTotalThreadTest() {
    // given
    given(assetRepository.findTotalThreadByUserId(any())).willReturn(Optional.of(10));
    // when
    int expectedValue = sut.getTotalThread(1L);
    // then
    assertThat(expectedValue).isEqualTo(10);

    then(assetRepository).should().findTotalThreadByUserId(any());

  }

  @Test
  @DisplayName("실타래 수량 조회 테스트 - 구매한 실 없음 -> 0을 리턴")
  void getTotalThreadTest2() {
    // given

    given(assetRepository.findTotalThreadByUserId(any())).willReturn(Optional.empty());
    // when
    int expectedValue = sut.getTotalThread(1L);
    // then
    assertThat(expectedValue).isEqualTo(0);

    then(assetRepository).should().findTotalThreadByUserId(any());
  }

  /*
   *
   * 1. 회원 실 수량 10개 추가
   * 2. 실 이력 저장
   * */

  @Test
  @DisplayName("추천인 코드 실타래 수량 업데이트 테스트 - 추천한 사람이 구매한 실이 없을 경우 UserAsset 생성 후 10개 저장")
  void updateThreadQuantityByPromoCodeTest() {
    // given
    User referredUserId = createUser();
    User referrerUserId = createUser();
    ReflectionTestUtils.setField(referrerUserId, "id", 2L);

    given(assetRepository.findUserAssetByUserId(any())).willReturn(Optional.empty());
    // when
    sut.updateThreadQuantityByReferralCode(referredUserId, referrerUserId);
    // then
    then(assetRepository).should(times(2)).save(any());
    then(threadUseHistoryRepository).should(times(2)).save(any());
  }

  @Test
  @DisplayName("추천인 코드 실타래 수량 업데이트 테스트 - 추천한 사람이 구매한 실이 있을 경우 UserAsset 10개 업데이트 후 저장")
  void updateThreadQuantityByPromoCodeTest2() {
    // given
    User referredUserId = createUser();
    User referrerUserId = createUser();
    ReflectionTestUtils.setField(referrerUserId, "id", 2L);

    UserAsset asset = createAsset(10);

    given(assetRepository.findUserAssetByUserId(any())).willReturn(Optional.of(asset));
    // when
    sut.updateThreadQuantityByReferralCode(referredUserId, referrerUserId);
    // then
    then(assetRepository).should(times(2)).save(any());
    then(threadUseHistoryRepository).should(times(2)).save(any());
  }

  /*
   * 1. 유저 아이디와 채팅 또는 프로필 대상 유저 아이디로 ThreadUseHistory 조회
   * 2. 존재할 경우 true를 리턴 존재하지 않을 경우 false를 리턴
   * */
  @Test
  @DisplayName("채팅 또는 프로필을 구매한 이력이 있는지 확인 - 프로필을 구매한 이력이 있으면 true를 리턴")
  void existsThreadUseHistoryTest() {
    // given
    Long userId = 1L;
    Long targetUserId = 2L;
    Long targetItemId = 1L;
    String purchaseTypeValue = "viewProfile";

    given(threadUseHistoryRepository.existsByUserIdAndTargetUserIdAndTargetItemId(any(), any(), any())).willReturn(true);
    // when
    boolean expectedBool = sut.existsThreadUseHistory(userId, targetUserId, purchaseTypeValue);
    // then
    assertThat(expectedBool).isTrue();
    then(threadUseHistoryRepository).should().existsByUserIdAndTargetUserIdAndTargetItemId(any(), any(), any());
  }

  @Test
  @DisplayName("채팅 또는 프로필을 구매한 이력이 있는지 확인 - 답변을 구매한 이력이 있으면 true를 리턴")
  void existsThreadUseHistoryTest2() {
    // given
    Long userId = 1L;
    Long targetUserId = 2L;
    Long targetItemId = 1L;
    String purchaseTypeValue = "answerQuestion";

    given(threadUseHistoryRepository.existsByUserIdAndTargetUserIdAndTargetItemId(any(), any(), any())).willReturn(true);
    // when
    boolean expectedBool = sut.existsThreadUseHistory(userId, targetUserId, purchaseTypeValue);
    // then
    assertThat(expectedBool).isTrue();
    then(threadUseHistoryRepository).should().existsByUserIdAndTargetUserIdAndTargetItemId(any(), any(), any());
  }

  @Test
  @DisplayName("프로필을 구매한 이력이 있는지 확인 - 구매한 이력이 없으면 false를 리턴")
  void existsThreadUseHistoryTest4() {
    // given
    Long userId = 1L;
    Long targetUserId = 2L;
    Long targetItemId = 1L;
    String purchaseTypeValue = "viewProfile";
    given(threadUseHistoryRepository.existsByUserIdAndTargetUserIdAndTargetItemId(any(), any(), any())).willReturn(false);
    // when
    boolean expectedBool = sut.existsThreadUseHistory(userId, targetUserId, purchaseTypeValue);
    // then
    assertThat(expectedBool).isFalse();
    then(threadUseHistoryRepository).should().existsByUserIdAndTargetUserIdAndTargetItemId(any(), any(), any());
  }

  private static VerifiedProductDto createSaveProductDto() {
    return VerifiedProductDto.of("productId", "transactionId", "originalTransactionId", InAppType.APPLE, TransactionType.PURCHASE,
                                 LocalDateTime.now(), RevocationReason.REFUNDED_FOR_OTHER_REASON, LocalDateTime.now());
  }

  private static UserAsset createAsset(int thread) {
    return UserAsset.builder().totalThread(thread).user(createUser()).build();
  }

  private static User createUser() {
    User user = User.builder().email("test@email.com").username("nickName").build();
    ReflectionTestUtils.setField(user, "id", 1L);
    return user;
  }

  private static Product createProduct() {
    return Product.builder().title("product title").price(3000).inAppProductId("in-app-product-id").quantityThread(10).build();
  }

}
