package com.thred.datingapp.user.service;

import com.thred.datingapp.chat.service.ChatRoomService;
import com.thred.datingapp.community.service.CommunityService;
import com.thred.datingapp.inApp.Service.PurchaseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class QuitServiceTest {

  @InjectMocks
  private QuitService    sut;
  @Mock
  private UserService      userService;
  @Mock
  private CommunityService communityService;
  @Mock
  private ChatRoomService  chatRoomService;
  @Mock
  private PurchaseService purchaseService;

  @Test
  @DisplayName("회원 상태가 WITHDRAW_REQUES이고 30일이 지난 회원이 없을 경우 return")
  void deleteExpiredWithdrawUsers_whenNoUsersPast30Days_thenDoNothing() {
    // given
    given(userService.getAllWithdrawUser()).willReturn(List.of());
    // when
    sut.deleteExpiredWithdrawUsers();
    // then
    then(userService).should().getAllWithdrawUser();

  }

  @DisplayName("30일이 지난 회원이 있을 경우 회원 정보 관련 데이터(커뮤니티 → 채팅 → 구매 내역 → 카드 → 신고내역 → 회원 정보)를 순차적으로 삭제")
  void deleteExpiredWithdrawUsers_whenUserWithdrawPassed30Days_thenDeleteUserDataInOrder() {
    // given
    given(userService.getAllWithdrawUser()).willReturn(List.of(1L, 2L, 3L));
    // when
    sut.deleteExpiredWithdrawUsers();
    // then
    then(userService).should().getAllWithdrawUser();
    then(communityService).should().deleteAllCommunitiesForWithdrawnUser(anyLong());
    then(chatRoomService).should().deleteAllChatHistory(anyLong(), anyLong());
    then(purchaseService).should().deleteAllPurchaseHistoriesForWithdrawnUser(anyLong());
    //then(userService).should().deleteAllUserInfo(anyLong());
  }

}
