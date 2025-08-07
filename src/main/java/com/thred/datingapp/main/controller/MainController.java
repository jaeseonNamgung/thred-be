package com.thred.datingapp.main.controller;

import com.thred.datingapp.common.api.response.ApiDataResponse;
import com.thred.datingapp.common.api.response.ApiStatusResponse;
import com.thred.datingapp.common.api.response.PageResponse;
import com.thred.datingapp.main.dto.request.AnswerRequest;
import com.thred.datingapp.main.dto.response.AnswerAllResponse;
import com.thred.datingapp.main.dto.response.CardOpenResponse;
import com.thred.datingapp.main.service.AnswerService;
import com.thred.datingapp.main.service.CardService;
import com.thred.datingapp.user.api.response.CardProfileResponse;
import com.thred.datingapp.user.api.response.ProcessingResultResponse;
import com.thred.datingapp.user.argumentResolver.Login;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class MainController {

  private final AnswerService answerService;
  private final CardService   cardService;

  @GetMapping("/answer/{receiverId}")
  public ApiDataResponse<AnswerAllResponse> getSubmittedAnswer(@PathVariable("receiverId") Long receiverId, @Login Long senderId) {
    log.info("[API CALL] /api/answer/{receiverId} - 답변 조회 요청");
    log.debug("[getAnswer] senderId = {}", senderId);
    log.debug("[getAnswer] receiverId = {}", receiverId);
    AnswerAllResponse answerAllResponse = answerService.getSubmittedAnswer(senderId, receiverId);
    return ApiDataResponse.ok(answerAllResponse);
  }

  @PostMapping("/answer/{receiverId}")
  public ApiDataResponse<ProcessingResultResponse> saveAnswer(@RequestBody @Valid AnswerRequest answerRequest,
                                                              @PathVariable("receiverId") Long receiverId,
                                                              @Login Long senderId) {
    log.info("[API CALL] /api/answer/{receiverId} - 답변 저장 요청");
    log.debug("[saveAnswer] senderId = {}", senderId);
    log.debug("[saveAnswer] receiverId = {}", receiverId);
    log.debug("[saveAnswer] answerRequest = {}", answerRequest);
    answerService.saveAnswer(senderId, receiverId, answerRequest);
    return ApiDataResponse.ok(ProcessingResultResponse.from(true));
  }

  @GetMapping("/card/open")
  public ApiDataResponse<PageResponse<CardOpenResponse>> getOpenCard(@Login Long userId,
                                                             @RequestParam(name = "pageLastId") long pageLastId,
                                                             @RequestParam("pageSize") int pageSize) {
    log.info("[API CALL] /api/card/open - 오픈 카드 조회 요청");
    log.debug("[getTodayCard] userId = {}", userId);
    return ApiDataResponse.ok(cardService.getOpenCards(userId, pageLastId, pageSize));
  }

  @GetMapping("/card/today")
  public ApiDataResponse<PageResponse<CardProfileResponse>> getTodayProfiles(@Login Long userId,
                                                                             @RequestParam(name = "city") String city,
                                                                             @RequestParam(name = "pageLastId") long pageLastId,
                                                                             @RequestParam("pageSize") int pageSize) {
    log.info("[API CALL] /api/card/today - 오늘의 카드 조회 요청");
    log.debug("[getTodayProfiles] loginId = {}", userId);
    log.debug("[getTodayProfiles] city = {}", city);
    log.debug("[getTodayProfiles] pageLastId = {}", pageLastId);
    log.debug("[getTodayProfiles] pageSize = {}", pageSize);
    return ApiDataResponse.ok(cardService.getTodayRandomCard(userId, city, pageLastId, pageSize));
  }

  @PostMapping("/card/open/{profileUserId}")
  public ApiDataResponse<ApiStatusResponse> saveCardOpen(@Login Long openerId, @PathVariable("profileUserId") Long profileUserId) {
    log.info("[API CALL] /api/card/open/{profileUserId} - 오픈 카드 저장 요청");
    log.debug("[saveCardOpen] openerId = {}", openerId);
    log.debug("[saveCardOpen] profileUserId = {}", profileUserId);
    cardService.saveCardOpen(openerId, profileUserId);
    return ApiDataResponse.ok(ApiStatusResponse.of(true));
  }

  /**
   * @Author NamgungJaeseon
   * @Date 2025.06.18
   * @Description 오늘의 카드로 채팅방 생성 시 오픈 카드 제거
   */
  @DeleteMapping("/card/open/{profileUserId}")
  public ApiDataResponse<ApiStatusResponse> deleteCardOpen(@Login Long openerId, @PathVariable("profileUserId") Long profileUserId) {
    log.info("[API CALL] /api/card/open/{profileUserId} - 오픈 카드 삭제 요청");
    log.debug("[deleteCardOpen] openerId = {}", openerId);
    log.debug("[deleteCardOpen] profileUserId = {}", profileUserId);
    cardService.deleteCardOpen(openerId, profileUserId);
    return ApiDataResponse.ok(ApiStatusResponse.of(true));
  }

}
