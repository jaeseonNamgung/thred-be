package com.thred.datingapp.user.service;

import com.thred.datingapp.chat.service.ChatRoomService;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.community.service.CommunityService;
import com.thred.datingapp.inApp.Service.PurchaseService;
import com.thred.datingapp.main.service.CardService;
import com.thred.datingapp.report.service.ReportService;
import com.thred.datingapp.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuitService {

  private final UserService            userService;
  private final ChatRoomService        chatRoomService;
  private final CommunityService       communityService;
  private final PurchaseService        purchaseService;
  private final ReportService          reportService;
  private final CardService            cardService;
  private final RefreshTokenRepository refreshTokenRepository;

  @Transactional
  public void updateQuitStatus(Long userId) {
    User user = userService.getUserById(userId);
    user.requestWithdraw();
    refreshTokenRepository.deleteByUserId(userId);
    log.info("[setUserQuitStatus] 유저 탈퇴 처리 완료 ===> userId: {}, state: {}, withdrawRequestDate: {}", userId, user.getUserState(),
             user.getWithdrawRequestDate());
  }

  /*
   * 1. 회원 탈퇴 스케줄링
   * 2. 채팅 관련 기록 삭제
   * 3. 게시글 관련 삭제
   * 4. 구매 관련 삭제
   * 5. 신고 내역 삭제
   * 6. 카드, 오픈 코드 삭제
   * 7. 회원 정보 삭제
   * */
  @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul") // 매일 새벽 4시 메서드 실행
  @Transactional
  public void deleteExpiredWithdrawUsers() {
    // 30일이 지난 회원 탈퇴 유저 조회
    List<Long> withdrawUserIds = userService.getAllWithdrawUser();

    if (withdrawUserIds.isEmpty()) {
      ;
      return;
    }
    withdrawUserIds.forEach(userId -> {
      chatRoomService.deleteAllChatsForWithdrawnUser(userId);
      // 게시글, 댓글은 삭제가 아니라 회원 정보를 null 변경
      communityService.deleteAllCommunitiesForWithdrawnUser(userId);
      purchaseService.deleteAllPurchaseHistoriesForWithdrawnUser(userId);
      reportService.deleteAllReportsForWithdrawnUser(userId);
      cardService.deleteAllCardsForWithdrawnUser(userId);
      userService.withdrawUser(userId);
    });
    log.info("[deleteExpiredWithdrawUsers] 회원 탈퇴 삭제 완료");
  }

}
