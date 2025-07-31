package com.thred.datingapp.community.service;

import com.thred.datingapp.common.api.response.PageResponse;
import com.thred.datingapp.common.entity.community.Comment;
import com.thred.datingapp.common.entity.community.Community;
import com.thred.datingapp.common.entity.community.CommunityImage;
import com.thred.datingapp.common.entity.community.type.CommunityType;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.CommunityErrorCode;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.common.utils.S3Utils;
import com.thred.datingapp.community.dto.request.CommunityRequest;
import com.thred.datingapp.community.dto.response.CommentResponse;
import com.thred.datingapp.community.dto.response.CommunityAllResponse;
import com.thred.datingapp.community.dto.response.CommunityResponse;
import com.thred.datingapp.community.repository.*;
import com.thred.datingapp.user.repository.PictureRepository;
import com.thred.datingapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CommunityService {
  private final CommunityRepository      communityRepository;
  private final CommunityImageRepository communityImageRepository;
  private final CommunityLikeRepository  communityLikeRepository;
  private final UserRepository           userRepository;
  private final CommentRepository        commentRepository;
  private final CommentLikeRepository    commentLikeRepository;
  private final S3Utils                  s3Utils;
  private final PictureRepository        pictureRepository;

  @Transactional
  public CommunityResponse createCommunity(final Long userId, final CommunityRequest communityRequest, final List<MultipartFile> images) {

    User user = userRepository.findById(userId).orElseThrow(() -> {
      log.error("[createCommunity] 존재하지 않은 회원(Not exist user) ===> userId: {}", userId);
      return new CustomException(UserErrorCode.USER_NOT_FOUND);
    });

    Community savedCommunity = communityRepository.save(communityRequest.toEntity(user));
    log.info("[createCommunity] 게시글 저장 완료(Successfully saved communityImage) ===> community: {}", savedCommunity);
    // 게시글에 이미지를 업로드 했을 때 로직 처리
    if (images != null && !images.isEmpty()) {

      List<CommunityImage> communityImages = images.stream()
                                                   .map(image -> CommunityImage.builder()
                                                                               .community(savedCommunity)
                                                                               .s3Path(s3Utils.saveImage(image))
                                                                               .originalFileName(image.getOriginalFilename())
                                                                               .build())
                                                   .toList();
      log.info("[createCommunity] 이미지 저장 완료(Successfully saved communityImage) ===> communityImage: {}", communityImages);
      communityImageRepository.saveAll(communityImages);
    }
    Community community = communityRepository.findByCommunityId(savedCommunity.getId()).orElseThrow(() -> {
      log.error("[createCommunity] 존재하지 않은 게시글 (Not found board) ===> communityId: {}", savedCommunity.getId());
      return new CustomException(CommunityErrorCode.NOT_FOUND_BOARD);
    });
    List<String> userProfiles = pictureRepository.findS3PathAllByUserId(community.getUser().getId());
    int randomIndex = getUserProfileRandomIndex(community.getId(), userProfiles.size());
    return CommunityResponse.fromResponse(community, List.of(), userProfiles.get(randomIndex), false, userId);
  }

  public CommunityResponse getCommunity(final Long communityId, final Long userId) {
    Community community = communityRepository.findByCommunityId(communityId).orElseThrow(() -> {
      log.error("[getCommunity] 존재하지 않은 게시글 (Not found board) ===> communityId: {}", communityId);
      return new CustomException(CommunityErrorCode.NOT_FOUND_BOARD);
    });

    List<Comment> comments = commentRepository.findByCommunityId(communityId);
    List<String> userProfiles = pictureRepository.findS3PathAllByUserId(community.getUser().getId());
    int randomIndex = getUserProfileRandomIndex(communityId, userProfiles.size());
    return convertToCommentResponse(communityId, userId, community, comments, userProfiles.get(randomIndex));
  }

  public PageResponse<CommunityAllResponse> getAllUserCommunities(final Long userId, final Long pageLastId, final int pageSize) {
    Page<CommunityAllResponse> page = communityRepository.findCommunitiesByUseIdAndPageLastIdWithPaging(userId, pageLastId, pageSize);
    return PageResponse.of(page.getSize(), page.isLast(), page.getContent());
  }

  public PageResponse<CommunityAllResponse> getAllCommunities(final String communityTypeValue, final Long pageLastId, final int pageSize) {
    Page<CommunityAllResponse> page =
        communityRepository.findCommunitiesByCommunityTypeAndPageLastIdWithPaging(CommunityType.findType(communityTypeValue), pageLastId, pageSize);
    page.getContent().forEach(content -> {
      List<String> userProfiles = pictureRepository.findS3PathAllByUserId(content.getUserId());
      int randomIndex = getUserProfileRandomIndex(content.getCommunityId(), userProfiles.size());
      content.setProfile(userProfiles.get(randomIndex));
    });
    return PageResponse.of(page.getSize(), page.isLast(), page.getContent());
  }

  @Transactional
  public boolean deleteCommunity(final Long communityId, final Long userId) {
    boolean existsCommunity = communityRepository.existsByCommunityIdAndUserId(communityId, userId);
    if (!existsCommunity) {
      log.error("[deleteCommunity] 존재하지 않은 게시글(Not found board) ===> communityId: {}, userId: {}", communityId, userId);
      throw new CustomException(CommunityErrorCode.NOT_FOUND_BOARD);
    }
    // 1. 게시글/댓글 좋아요 삭제
    communityLikeRepository.deleteLikeByCommunityId(communityId);
    log.info("[deleteCommunity] 게시글 좋아요 삭제 완료(Successfully deleted board like) ===> communityId: {}", communityId);
    commentLikeRepository.deleteLikeByCommunityId(communityId);
    log.info("[deleteCommunity] 댓글 좋아요 삭제 완료(Successfully deleted comment like) ===> communityId: {}", communityId);
    // 2. 이미지 삭제
    List<CommunityImage> s3PathList = communityImageRepository.findByCommunityId(communityId);
    deleteCommunityImage(communityId, s3PathList);
    log.info("[deleteCommunity] 이미지 삭제 완료(Successfully deleted image) ===> communityId: {}", communityId);
    // 3. 댓글 삭제
    commentRepository.deleteAllByCommunityId(communityId);
    log.info("[deleteCommunity] 댓글 삭제 완료(Successfully deleted comment) ===> communityId: {}", communityId);
    // 4. 게시글 삭제
    communityRepository.deleteByCommunityId(communityId);
    log.info("[deleteCommunity] 게시글 삭제 완료(Successfully deleted community board) ===> communityId: {}", communityId);
    return true;
  }

  @Transactional
  public boolean addCommunityLike(final Long communityId, final Long userId) {
    return processCommunityLike(communityId, userId, true);
  }

  @Transactional
  public boolean deleteCommunityLike(final Long communityId, final Long userId) {
    return processCommunityLike(communityId, userId, false);
  }

  @Transactional
  public Boolean deleteAllCommunitiesForWithdrawnUser(final Long userId) {
    boolean existUser = userRepository.existsById(userId);
    if (!existUser) {
      log.error("[deleteUserCommunityHistory] 존재하지 않은 회원 (Not exist user) ===> userId: {}", userId);
      throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    }
    communityRepository.detachUserFromCommunities(userId);
    log.info("[deleteUserCommunityHistory] 회원 관련 게시글 삭제 완료(Successfully deleted user community boards) ===> userId: {}", userId);
    commentRepository.detachUserFromComments(userId);
    log.info("[deleteUserCommunityHistory] 회원 관련 댓글 삭제 완료(Successfully deleted user comment) ===> userId: {}", userId);
    return true;
  }

  @Transactional
  public Boolean updateCommunity(final Long communityId, final CommunityRequest communityUpdateRequest, final List<MultipartFile> images) {
    Community community = communityRepository.findByCommunityId(communityId).orElseThrow(() -> {
      log.error("[updateCommunity] 존재하지 않은 게시글(Not found board) ===> communityId: {}", communityId);
      return new CustomException(CommunityErrorCode.NOT_FOUND_BOARD);
    });

    // 1. 이미 존재하는 S3 이미지 조회
    List<CommunityImage> existingImages = communityImageRepository.findByCommunityId(communityId);
    // 2. 이미지 삭제 처리
    deleteCommunityImage(communityId, existingImages);
    log.info("[updateCommunity] 기존 이미지 삭제 완료(Successfully deleted existing images) ===> images: {}", existingImages);
    // 3. 새 이미지 저장 (기존에 없는 이미지들만 추가)
    if (images != null && !images.isEmpty()) {
      saveCommunityImage(images, community);
      log.info("[updateCommunity] 새 이미지 저장 완료(Successfully created new images) ===> images: {}", images);
    }
    community.updateBoard(communityUpdateRequest.title(), communityUpdateRequest.content(), communityUpdateRequest.isPublicProfile());

    Long resultCode = communityRepository.updateByCommunityId(community);
    if (resultCode <= 0) {
      log.error("[updateCommunity] 업데이트 중 오류(Update error) ===> updateResultCode: {}", resultCode);
      throw new CustomException(CommunityErrorCode.UPDATE_FAILED);
    }
    log.info("[updateCommunity] 게시글 수정 완료(Successfully updated board) ===> title: {}, content: {}, isPublicProfile: {}", community.getTitle(),
             community.getContent(), community.getIsPublicProfile());
    return true;

  }

  private boolean validateCommunityLike(final Long communityId, final Long userId) {
    return communityId != null && userId != null;
  }

  private boolean processCommunityLike(final Long communityId, final Long userId, final boolean isAddOperation) {
    if (!validateCommunityLike(communityId, userId)) {
      log.error("[processCommunityLike] Community ID or User ID is Null ===> communityId: {}, userId: {}", communityId, userId);
      return false;
    }
    boolean isExistLike = communityLikeRepository.existsLikesByCommunityIdAndUserId(communityId, userId);
    log.info("[processCommunityLike] 좋아요 존재(Exist like) ===> isExistList: {}", isExistLike);
    if (isAddOperation) {
      log.debug("[processCommunityLike] isAddOperation: {}", isAddOperation);
      if (isExistLike) {
        return false; // 이미 좋아요가 존재
      }
      communityLikeRepository.insertLikeByCommunityIdAndUserId(communityId, userId);
      log.info("[processCommunityLike] 좋아요 저장 완료(Successfully saved community like) ===> communityId: {}, userId: {}", communityId, userId);
    } else {
      if (!isExistLike) {
        return false; // 좋아요가 존재하지 않음
      }
      communityLikeRepository.deleteLikeByCommunityIdAndUserId(communityId, userId);
      log.info("[processCommunityLike] 좋아요 삭제 완료(Successfully deleted community like) ===> communityId: {}, userId: {}", communityId, userId);
    }

    return true;
  }

  private int getUserProfileRandomIndex(Long communityId, int imageLength) {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    String key = today + "-" + communityId;
    return Math.abs(key.hashCode()) % imageLength;
  }

  private CommunityResponse convertToCommentResponse(final Long communityId,
                                                     final Long userId,
                                                     final Community community,
                                                     final List<Comment> comments,
                                                     final String userProfile) {
    return CommunityResponse.fromResponse(community, comments.stream()
                                                             .filter(comment -> comment.getParentId() == null)
                                                             .map(comment -> convertToCommentResponse(userId, comment))
                                                             .toList(),
                                          userProfile,
                                          communityLikeRepository.existsLikesByCommunityIdAndUserId(communityId, userId), userId);
  }

  private CommentResponse convertToCommentResponse(final Long userId, final Comment comment) {
    // 부모 댓글일 경우
    if (comment.getParentId() == null) {
      // 자식 댓글을 부모 댓글에서 찾아서 재귀적으로 처리
      List<Comment> childrenComment = commentRepository.findByParentId(comment.getId());
      return CommentResponse.from(comment, commentLikeRepository.countByCommentLikePkCommentId(comment.getId()),
                                  commentLikeRepository.existsLikesByCommentIdAndUserId(comment.getId(), userId), userId,
                                  childrenComment.stream().map(childComment -> convertToCommentResponse(userId, childComment)) // 재귀 호출
                                                 .toList());
    }
    // 자식 댓글인 경우 (부모 댓글이 없으므로 자식만 리턴)
    return CommentResponse.from(comment, commentLikeRepository.countByCommentLikePkCommentId(comment.getId()),
                                commentLikeRepository.existsLikesByCommentIdAndUserId(comment.getId(), userId), userId, List.of()
                                // 자식 댓글이므로 더 이상 하위 댓글 없음
    );
  }

  private void saveCommunityImage(final List<MultipartFile> images, final Community savedCommunity) {
    List<CommunityImage> communityImages = images.stream()
                                                 .map(image -> CommunityImage.builder()
                                                                             .community(savedCommunity)
                                                                             .s3Path(s3Utils.saveImage(image))
                                                                             .originalFileName(image.getOriginalFilename())
                                                                             .build())
                                                 .toList();
    communityImageRepository.saveAll(communityImages);
    log.debug("[saveCommunityImage] 게시글 이미지 완료 ===> images: {}", communityImages);
  }

  private void deleteCommunityImage(final Long communityId, final List<CommunityImage> communityImages) {
    if (!communityImages.isEmpty()) {
      communityImages.forEach(image -> s3Utils.deleteS3Image(image.getS3Path()));
      communityImageRepository.deleteByCommunityId(communityId);
      log.debug("[saveCommunityImage] 게시글 이미지 삭제 완료 ===> communityId: {}, images: {}", communityId, communityImages);
    }
  }
}
