package com.thred.datingapp.user.controller;

import static com.thred.datingapp.common.error.errorCode.UserErrorCode.MAIN_PROFILE_REQUIRED;
import static com.thred.datingapp.common.error.errorCode.UserErrorCode.INSUFFICIENT_PROFILE_PHOTOS;

import com.thred.datingapp.admin.service.ReviewService;
import com.thred.datingapp.common.api.response.ApiDataResponse;
import com.thred.datingapp.common.entity.admin.Review;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.user.api.request.BlockNumbersRequest;
import com.thred.datingapp.user.api.request.ChangeAddressRequest;
import com.thred.datingapp.user.api.request.ChangePhoneNumberRequest;
import com.thred.datingapp.user.api.request.EditMainDetailsRequest;
import com.thred.datingapp.user.api.request.JoinDetailsRequest;
import com.thred.datingapp.user.api.request.JoinUserRequest;
import com.thred.datingapp.user.api.request.RejoinUserRequest;
import com.thred.datingapp.user.api.response.BlockNumbersResponse;
import com.thred.datingapp.user.api.response.DuplicateResponse;
import com.thred.datingapp.user.api.response.FirstJoinResponse;
import com.thred.datingapp.user.api.response.JoinTotalDetails;
import com.thred.datingapp.user.api.response.JudgmentResponse;
import com.thred.datingapp.user.api.response.ProcessingResultResponse;
import com.thred.datingapp.user.api.response.ProfileResponse;
import com.thred.datingapp.user.service.QuitService;
import com.thred.datingapp.user.service.UserService;
import com.thred.datingapp.user.argumentResolver.Login;
import com.thred.datingapp.main.dto.response.ProfilesResponse;
import com.thred.datingapp.main.dto.response.UserDetailsResponse;
import com.thred.datingapp.main.service.AnswerService;
import jakarta.validation.Valid;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class MemberController {

    private final UserService userService;
    private final AnswerService answerService;
    private final ReviewService reviewService;
    private final QuitService   quitService;

    @GetMapping("/email")
    public ApiDataResponse<DuplicateResponse> checkEmail(@RequestParam("email") String email) {
        log.info("[API CALL] /api/user/email - 이메일 중복검사 요청");
        log.debug("[checkEmail] email = {}", email);
        return ApiDataResponse.ok(DuplicateResponse.of(userService.checkDuplicateEmail(email)));
    }

    @GetMapping("/username")
    public ApiDataResponse<DuplicateResponse> checkName(@RequestParam("username") String username) {
        log.info("[API CALL] /api/user/username - 닉네임 중복 검사 요청");
        log.debug("[checkName] username = {}", username);
        return ApiDataResponse.ok(DuplicateResponse.of(userService.checkDuplicateName(username)));
    }

    @GetMapping("/code")
    public ApiDataResponse<DuplicateResponse> checkCode(@RequestParam("code") String code) {
        log.info("[API CALL] /api/user/code - 인증 코드 중복 검사 요청");
        log.debug("[checkCode] code = {}", code);
        return ApiDataResponse.ok(DuplicateResponse.of(userService.checkCode(code)));
    }

    @PostMapping("/join")
    public ApiDataResponse<FirstJoinResponse> join(@Valid @RequestPart("user") JoinUserRequest user,
                                                   @Valid @RequestPart("details") JoinDetailsRequest details,
                                                   @RequestPart("mainProfile") MultipartFile mainProfile,
                                                   @RequestPart("files") List<MultipartFile> files) {
        log.info("[API CALL] /api/user/join - 회원가입 요청");
        log.debug("[join] user = {}", user);
        log.debug("[join] details = {}", details);
        log.debug("[join] mainProfile = {}", mainProfile);
        log.debug("[join] files = {}", files);
        if (mainProfile == null || mainProfile.isEmpty()) {
            throw new CustomException(MAIN_PROFILE_REQUIRED);
        }
        if (files == null || files.size() < 3) {
            throw new CustomException(INSUFFICIENT_PROFILE_PHOTOS);
        }
        Long userId = userService.join(user, details, mainProfile, files);
        return ApiDataResponse.ok(FirstJoinResponse.of(userId, true));
    }

    @PostMapping("/rejoin")
    public ApiDataResponse<ProcessingResultResponse> rejoin(@Valid @RequestPart("user") RejoinUserRequest user,
                                                            @Valid @RequestPart("details") JoinDetailsRequest details,
                                                            @RequestParam(name="mainChange") boolean mainChange,
                                                            @RequestPart(required = false,name = "mainProfile") MultipartFile mainProfile,
                                                            @RequestParam(required = false,name = "deleteFileIds") List<Long> deleteFileIds,
                                                            @RequestPart(required = false, name = "extraChangedProfiles") List<MultipartFile> extraChangedProfiles) {
        log.info("[API CALL] /api/user/rejoin - 회원 재가입 요청");
        log.debug("[rejoin] user = {}", user);
        log.debug("[rejoin] details = {}", details);
        log.debug("[rejoin] mainChange = {}", mainChange);
        log.debug("[rejoin] mainProfile = {}", mainProfile);
        log.debug("[rejoin] deleteFileIds = {}", deleteFileIds);
        log.debug("[rejoin] extraChangedProfiles = {}", extraChangedProfiles);
        userService.rejoin(user, details, mainChange, mainProfile, deleteFileIds, extraChangedProfiles);
        return ApiDataResponse.ok(ProcessingResultResponse.from(true));
    }

    @PostMapping("/block")
    public ApiDataResponse<ProcessingResultResponse> setBlockNumber(@RequestBody @Valid BlockNumbersRequest request,
                                                                    @Login Long userId) {
        log.info("[API CALL] /api/user/block - 전화번호 차단 요청");
        log.debug("[setBlockNumber] userId = {}", userId);
        log.debug("[setBlockNumber] blockNumbersRequest = {}", request);
        userService.setBlockNumber(userId, request.numbers());
        return ApiDataResponse.ok(ProcessingResultResponse.from(true));
    }

    @GetMapping("/block")
    public ApiDataResponse<BlockNumbersResponse> getBlockNumber(@Login Long userId) {
        log.info("[API CALL] /api/user/block - 전화번호 차단 목록 조회 요청");
        log.debug("[getBlockNumber] userId = {}", userId);
        return ApiDataResponse.ok(userService.getBlockNumber(userId));
    }

    @GetMapping("/judgments")
    public ApiDataResponse<JudgmentResponse> getJoinReviewResponse(@Login Long userId) {
        log.info("[API CALL] /api/user/judgments - 회원 심사 조회 요청");
        log.debug("[getJoinJudgmentResponse] userId = {}", userId);
        Review review = reviewService.getJoinReviewByUserId(userId);
        JoinTotalDetails joinDetails = userService.getJoinDetails(userId);
        return ApiDataResponse.ok(
                JudgmentResponse.of(review.getReviewStatus().name(), review.getReason(), joinDetails));
    }

    @GetMapping("/{userId}/details")
    public ApiDataResponse<UserDetailsResponse> getUserProfile(@PathVariable("userId") Long userId,
                                                               @Login Long loginId) {
        log.info("[API CALL] /api/user/{userId}/details - 회원 상세 조회 요청");
        log.debug("[getUserProfile] loginId = {}", loginId);
        log.debug("[getUserProfile] userId = {}", userId);
        UserDetailsResponse allDetails = userService.getAllDetails(userId);
        allDetails.setAnswer(answerService.isSend(loginId, userId));
        return ApiDataResponse.ok(allDetails);
    }

    @GetMapping("/details")
    public ApiDataResponse<JoinTotalDetails> getLoginUserDetails(@Login Long userId) {
        log.info("[API CALL] /api/user/details - 로그인 회원 상세 조회 요청");
        log.debug("[getLoginUserDetails] userId = {}", userId);
        return ApiDataResponse.ok(userService.getJoinDetails(userId));
    }

    @PostMapping("/details")
    public ApiDataResponse<JoinTotalDetails> editMainDetails(@RequestBody @Valid EditMainDetailsRequest request,
                                                             @Login Long loginId) {
        log.info("[API CALL] /api/user/details - 회원 상세 정보 수정 요청");
        log.debug("[editMainDetails] userId = {}", loginId);
        log.debug("[editMainDetails] EditMainDetailsRequest = {}", request);
        userService.updateUserAndDetails(loginId, request.questionChange(), request.introduceChange(), request.user(),
                request.details());
        return ApiDataResponse.ok(userService.getJoinDetails(loginId));
    }

    @GetMapping("/profile")
    public ApiDataResponse<ProfilesResponse> getProfiles(@Login Long loginId) {
        log.info("[API CALL] /api/user/profile - 프로필 조회 요청");
        log.debug("[getProfiles] userId = {}", loginId);
        User user = userService.getUserById(loginId);
        List<ProfileResponse> profiles = userService.getProfiles(loginId);
        return ApiDataResponse.ok(ProfilesResponse.of(user.getMainProfile(), profiles));
    }

    @PostMapping("/profile")
    public ApiDataResponse<ProcessingResultResponse> editProfiles(@Login Long userId,
                                                                  @RequestParam boolean mainChange,
                                                                  @RequestPart(required = false) MultipartFile mainProfile,
                                                                  @RequestParam(required = false) List<Long> changedProfileIds,
                                                                  @RequestPart(required = false) List<MultipartFile> changedExtraProfiles) {
        log.info("[API CALL] /api/user/profile - 프로필 수정 요청");
        log.debug("[editProfiles] userId = {}", userId);
        log.debug("[editProfiles] mainChange = {}", mainChange);
        log.debug("[editProfiles] mainProfile = {}", mainProfile);
        log.debug("[editProfiles] changedProfileIds = {}", changedProfileIds);
        log.debug("[editProfiles] changedExtraProfiles = {}", changedExtraProfiles);
        userService.sendEditProfilesRequest(userId, mainChange, mainProfile, changedProfileIds, changedExtraProfiles);
        return ApiDataResponse.ok(ProcessingResultResponse.from(true));
    }

    @PostMapping("/number")
    public ApiDataResponse<ProcessingResultResponse> editUserNumber(@Login Long loginId, @RequestBody @Valid
    ChangePhoneNumberRequest request) {
        log.info("[API CALL] /api/user/number - 전화번호 수정 요청");
        log.debug("[editUserNumber] userId = {}", loginId);
        log.debug("[editUserNumber] ChangePhoneNumberRequest = {}", request);
        userService.changePhoneNumber(loginId, request.number());
        return ApiDataResponse.ok(ProcessingResultResponse.from(true));
    }

    @PostMapping("/address")
    public ApiDataResponse<ProcessingResultResponse> editAddress(@Login Long loginId, @RequestBody @Valid
    ChangeAddressRequest request) {
        log.info("[API CALL] /api/user/address - 주소 수정 요청");
        log.debug("[editAddress] userId = {}", loginId);
        log.debug("[editAddress] ChangeAddressRequest = {}", request);
        userService.changeAddress(loginId, request.city(), request.province());
        return ApiDataResponse.ok(ProcessingResultResponse.from(true));
    }

    @PostMapping("/withdraw")
    public ApiDataResponse<ProcessingResultResponse> withdrawUser(@Login Long userId) {
        log.info("[API CALL] /api/user/withdraw - 회원 탈퇴 요청");
        log.debug("[deleteUser] userId = {}", userId);
        quitService.updateQuitStatus(userId);
        return ApiDataResponse.ok(ProcessingResultResponse.from(true));
    }
}
