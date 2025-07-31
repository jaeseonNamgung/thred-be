package com.thred.datingapp.community.controller;

import com.thred.datingapp.common.api.response.ApiDataResponse;
import com.thred.datingapp.common.api.response.PageResponse;
import com.thred.datingapp.common.error.errorCode.CommunityErrorCode;
import com.thred.datingapp.community.dto.request.CommunityRequest;
import com.thred.datingapp.community.dto.response.CommunityAllResponse;
import com.thred.datingapp.community.dto.response.CommunityResponse;
import com.thred.datingapp.community.service.CommunityService;
import com.thred.datingapp.user.argumentResolver.Login;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RequestMapping("/api/community")
@RequiredArgsConstructor
@RestController
public class CommunityApiController {

  private final CommunityService communityService;


  @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiDataResponse<CommunityResponse> createCommunity(
      @Login Long userId,
      @RequestPart("communityRequest") CommunityRequest communityRequest,
      @RequestPart(name = "images", required = false) List<MultipartFile> multipartFiles
  ){
    log.debug("[createCommunity] userId: {}", userId);
    log.debug("[createCommunity] communityRequest: {}", communityRequest);
    log.debug("[createCommunity] multipartFiles: {}", multipartFiles);
    return ApiDataResponse.ok(communityService.createCommunity(userId, communityRequest, multipartFiles));
  }

  @GetMapping("/all")
  public ApiDataResponse<PageResponse<CommunityAllResponse>> getAllCommunities(
      @RequestParam(name = "pageLastId") Long pageLastId,
      @RequestParam(defaultValue = "실시간", name = "communityType") String CommunityTypeValue,
      @RequestParam("pageSize") int pageSize) {

    log.debug("[getAllCommunity] CommunityTypeValue: {}", CommunityTypeValue);
    log.debug("[getAllCommunity] pageLastId: {}", pageLastId);
    log.debug("[getAllCommunity] pageSize: {}", pageSize);
    return ApiDataResponse.ok(communityService.getAllCommunities(CommunityTypeValue, pageLastId, pageSize));
  }

  @GetMapping("/get/user")
  public ApiDataResponse<PageResponse<CommunityAllResponse>> getAllUserCommunities(
          @Login Long userId,
          @RequestParam(name = "pageLastId") Long pageLastId,
          @RequestParam("pageSize") int pageSize) {

    log.debug("[getAllUserCommunity] userId: {}", userId);
    log.debug("[getAllUserCommunity] pageLastId: {}", pageLastId);
    log.debug("[getAllUserCommunity] pageSize: {}", pageSize);
    return ApiDataResponse.ok(communityService.getAllUserCommunities(userId, pageLastId, pageSize));
  }

  @GetMapping("/get/{communityId}")
  public ApiDataResponse<CommunityResponse> getCommunity(
          @PathVariable("communityId") Long communityId,
          @Login Long userId
          ) {

    log.debug("[getCommunity] userId: {}", userId);
    log.debug("[getCommunity] communityId: {}", communityId);
    return ApiDataResponse.ok(communityService.getCommunity(communityId, userId));
  }

  @PatchMapping(value = "/update/{communityId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiDataResponse<Boolean> updateCommunity(
          @PathVariable(name="communityId") Long communityId,
          @RequestPart(name = "communityRequest") CommunityRequest communityUpdateRequest,
          @RequestPart(name = "images", required = false) List<MultipartFile> multipartFiles
  ){
    log.debug("[updateCommunity] communityId: {}", communityId);
    log.debug("[updateCommunity] communityUpdateRequest: {}", communityUpdateRequest);
    log.debug("[updateCommunity] multipartFiles: {}", multipartFiles);
    return ApiDataResponse.ok(communityService.updateCommunity(communityId, communityUpdateRequest, multipartFiles));
  }

  @DeleteMapping("/delete/{communityId}")
  public ApiDataResponse<Boolean> deleteCommunity(
          @PathVariable("communityId") Long communityId,
          @Login Long userId) {
    log.debug("[deleteCommunity] communityId: {}", communityId);
    log.debug("[deleteCommunity] userId: {}", userId);
    return ApiDataResponse.ok(communityService.deleteCommunity(communityId, userId));
  }

  @PostMapping("/add/like/{communityId}")
  public ApiDataResponse<Boolean> addLike(
          @PathVariable("communityId") Long communityId,
          @Login Long userId) {

    log.debug("[addLike] communityId: {}", communityId);
    log.debug("[addLike] userId: {}", userId);
    return communityService.addCommunityLike(communityId, userId) ?
            ApiDataResponse.ok(true):
            ApiDataResponse.ok(false, CommunityErrorCode.ALREADY_ADDED_LIKED.getMessage());
  }
  @DeleteMapping("/delete/like/{communityId}")
  public ApiDataResponse<Boolean> deleteLike(
          @PathVariable("communityId") Long communityId,
          @Login Long userId) {

    log.debug("[deleteLike] communityId: {}", communityId);
    log.debug("[deleteLike] userId: {}", userId);
    return communityService.deleteCommunityLike(communityId, userId) ?
            ApiDataResponse.ok(true):
            ApiDataResponse.ok(false, CommunityErrorCode.ALREADY_REMOVED_LIKE.getMessage());
  }

}
