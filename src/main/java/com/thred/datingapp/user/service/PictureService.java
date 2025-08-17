package com.thred.datingapp.user.service;

import com.thred.datingapp.common.entity.user.Picture;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.user.repository.PictureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PictureService {

  private final PictureRepository pictureRepository;

  public List<Picture> getAllByUserId(final Long userId) {
    if(userId == null) {
      log.error("[getAllByUserId] UserId is Null");
      throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    }
    return pictureRepository.findAllByUserId(userId);
  }
  public List<Picture> getAllByUserIdAndIdNotIn(final Long userId, final List<Long> profileIds) {
    if(userId == null) {
      log.error("[getAllByUserIdAndIdNotIn] UserId is Null");
      throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    }
    return pictureRepository.findAllByUserIdAndIdNotIn(userId, profileIds);
  }

  public List<Picture> getAllByProfileIds(final List<Long> profileIds) {
    return pictureRepository.findAllByProfileIds(profileIds);
  }

  @Transactional
  public void deleteProfileByIds(final List<Long> profileIds) {
    if(profileIds.isEmpty()) {
      log.error("[deleteProfileByIds] profileIds is Empty");
      throw new CustomException(UserErrorCode.PROFILE_NOT_FOUND);
    }
    pictureRepository.deleteProfileByIds(profileIds);
    log.debug("[deleteProfileByIds] profile 전체 삭제 완료 ===> profileIds: {}", profileIds);
  }

  @Transactional
  public void save(final Picture picture) {
    Picture savedPicture = pictureRepository.save(picture);
    log.debug("[pictureService - save] profile 저장 완료 ===> profileId: {}", savedPicture.getId());
  }
  @Transactional
  public void saveAll(final List<Picture> pictures) {
    pictureRepository.saveAll(pictures);
    log.debug("[pictureService - save] profile 전체 저장 완료");
  }

  @Transactional
  public void deleteByUserId(final Long userId) {
    if(userId == null) {
      log.error("[deleteByUserId] UserId is Null");
      throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    }
    pictureRepository.deleteByUserId(userId);
    log.debug("[deleteByUserId] profile 삭제 완료 ===> userId: {}", userId);
  }


}
