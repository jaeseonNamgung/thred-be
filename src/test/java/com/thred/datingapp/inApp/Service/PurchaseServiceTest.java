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
import com.thred.datingapp.user.service.UserAssetService;
import com.thred.datingapp.user.service.UserService;
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
  private UserService             userService;
  @Mock
  private UserAssetService        userAssetService;
  @Mock
  private ProductService          productService;
  @Mock
  private ReceiptService          receiptService;
  @Mock
  private AppleInAppService       inAppService;
  @Mock
  private ThreadUseHistoryService threadUseHistoryService;

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

    given(userService.getUserById(any())).willReturn(user);
    given(inAppService.verifyReceipt(any())).willReturn(verifiedProductDto);
    given(productService.getByProductId(any())).willReturn(product);
    given(userAssetService.getUserAsset(any(User.class))).willReturn(asset);

    //when
    boolean expectedBool = sut.processInAppPurchase(1L, receiptRequest);

    //then
    assertTrue(expectedBool);

    then(userService).should().getUserById(any());
    then(inAppService).should().verifyReceipt(any());
    then(productService).should().getByProductId(any());
    then(receiptService).should().save(any());
    then(userAssetService).should().getUserAsset(any(User.class));
    then(threadUseHistoryService).should().save(any());
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
    UserAsset asset = createAsset(0);

    given(userService.getUserById(any())).willReturn(user);
    given(inAppService.verifyReceipt(any())).willReturn(verifiedProductDto);
    given(productService.getByProductId(any())).willReturn(product);
    given(userAssetService.getUserAsset(any(User.class))).willReturn(asset);

    //when
    boolean expectedBool = sut.processInAppPurchase(1L, receiptRequest);
    //then
    assertTrue(expectedBool);

    then(userService).should().getUserById(any());
    then(inAppService).should().verifyReceipt(any());
    then(productService).should().getByProductId(any());
    then(receiptService).should().save(any());
    then(userAssetService).should().getUserAsset(any(User.class));
    then(userAssetService).should().save(any());
    then(threadUseHistoryService).should().save(any());
  }

  @Test
  @DisplayName("saveProductDto가 null 값이면 에러 발생")
  void processInAppPurchase5() throws Exception {
    //given
    ReceiptRequest receiptRequest = ReceiptRequest.of("receiptData", InAppType.APPLE);
    User user = createUser();
    ReflectionTestUtils.setField(user, "id", 1L);

    given(userService.getUserById(any())).willReturn(user);
    given(inAppService.verifyReceipt(any())).willReturn(null);

    //when & then
    CustomException expectedException = assertThrows(CustomException.class, () -> sut.processInAppPurchase(1L, receiptRequest));

    assertThat(expectedException.getErrorCode().getHttpStatus()).isEqualTo(InAppErrorCode.RECEIPT_SIGNATURE_VERIFICATION_ERROR.getHttpStatus());
    assertThat(expectedException.getMessage()).isEqualTo(InAppErrorCode.RECEIPT_SIGNATURE_VERIFICATION_ERROR.getMessage());

    then(userService).should().getUserById(any());
    then(inAppService).should().verifyReceipt(any());
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
    ThreadRequest threadRequest = new ThreadRequest(1L, "VIEW_PROFILE");
    given(userAssetService.getUserAsset(any(Long.class))).willReturn(asset);

    // when
    boolean expectedBool = sut.useThread(1L, threadRequest);
    // then
    assertTrue(expectedBool);

    then(userAssetService).should().getUserAsset(any(Long.class));
    then(threadUseHistoryService).should().save(any());
  }

  @Test
  @DisplayName("사용자 실타래 사용 이력 저장 테스트 - 실타래 수량이 부족한 경우 예외 발생")
  void useThreadTest3() {
    // given
    UserAsset asset = createAsset(1);
    ThreadRequest threadRequest = new ThreadRequest(1L, "VIEW_PROFILE");
    given(userAssetService.getUserAsset(any(Long.class))).willReturn(asset);

    // when
    CustomException expectedException = (CustomException) catchException(() -> sut.useThread(1L, threadRequest));
    // then
    assertThat(expectedException.getErrorCode()).hasFieldOrPropertyWithValue("httpStatus", InAppErrorCode.INSUFFICIENT_THREAD_COUNT.getHttpStatus())
                                                .hasFieldOrPropertyWithValue("message", InAppErrorCode.INSUFFICIENT_THREAD_COUNT.getMessage());

    then(userAssetService).should().getUserAsset(any(Long.class));
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
    UserAsset asset = createAsset(0);
    given(userAssetService.getUserAsset(any(User.class))).willReturn(asset);
    // when
    sut.updateThreadQuantityByReferralCode(referredUserId, referrerUserId);
    // then
    then(userAssetService).should(times(2)).save(any());
    then(threadUseHistoryService).should(times(2)).save(any());
    then(userAssetService).should().getUserAsset(any(User.class));
  }

  @Test
  @DisplayName("추천인 코드 실타래 수량 업데이트 테스트 - 추천한 사람이 구매한 실이 있을 경우 UserAsset 10개 업데이트 후 저장")
  void updateThreadQuantityByPromoCodeTest2() {
    // given
    User referredUserId = createUser();
    User referrerUserId = createUser();
    ReflectionTestUtils.setField(referrerUserId, "id", 2L);

    UserAsset asset = createAsset(10);

    given(userAssetService.getUserAsset(any(User.class))).willReturn(asset);
    // when
    sut.updateThreadQuantityByReferralCode(referredUserId, referrerUserId);
    // then
    then(userAssetService).should(times(2)).save(any());
    then(threadUseHistoryService).should(times(2)).save(any());
    then(userAssetService).should().getUserAsset(any(User.class));
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
    String purchaseTypeValue = "VIEW_PROFILE";

    given(threadUseHistoryService.existsByUserIdAndTargetUserIdAndTargetItemId(any(), any(), any())).willReturn(true);
    // when
    boolean expectedBool = sut.existsThreadUseHistory(userId, targetUserId, purchaseTypeValue);
    // then
    assertThat(expectedBool).isTrue();
    then(threadUseHistoryService).should().existsByUserIdAndTargetUserIdAndTargetItemId(any(), any(), any());
  }

  @Test
  @DisplayName("채팅 또는 프로필을 구매한 이력이 있는지 확인 - 답변을 구매한 이력이 있으면 true를 리턴")
  void existsThreadUseHistoryTest2() {
    // given
    Long userId = 1L;
    Long targetUserId = 2L;
    Long targetItemId = 1L;
    String purchaseTypeValue = "VIEW_PROFILE";

    given(threadUseHistoryService.existsByUserIdAndTargetUserIdAndTargetItemId(any(), any(), any())).willReturn(true);
    // when
    boolean expectedBool = sut.existsThreadUseHistory(userId, targetUserId, purchaseTypeValue);
    // then
    assertThat(expectedBool).isTrue();
    then(threadUseHistoryService).should().existsByUserIdAndTargetUserIdAndTargetItemId(any(), any(), any());
  }

  @Test
  @DisplayName("프로필을 구매한 이력이 있는지 확인 - 구매한 이력이 없으면 false를 리턴")
  void existsThreadUseHistoryTest4() {
    // given
    Long userId = 1L;
    Long targetUserId = 2L;
    Long targetItemId = 1L;
    String purchaseTypeValue = "VIEW_PROFILE";
    given(threadUseHistoryService.existsByUserIdAndTargetUserIdAndTargetItemId(any(), any(), any())).willReturn(false);
    // when
    boolean expectedBool = sut.existsThreadUseHistory(userId, targetUserId, purchaseTypeValue);
    // then
    assertThat(expectedBool).isFalse();
    then(threadUseHistoryService).should().existsByUserIdAndTargetUserIdAndTargetItemId(any(), any(), any());
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
