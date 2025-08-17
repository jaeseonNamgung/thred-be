package com.thred.datingapp.user.service;

import com.thred.datingapp.common.entity.user.Block;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.user.repository.BlockBulkRepository;
import com.thred.datingapp.user.repository.BlockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class BlockService {

  private final BlockRepository     blockRepository;
  private final BlockBulkRepository blockBulkRepository;

  @Transactional
  public void deleteByBlockerId(final Long userId) {
    if (userId == null) {
      log.debug("[deleteByBlockerId] blockerId is Null");
      throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    }
    blockRepository.deleteByBlockerId(userId);
    log.debug("[deleteByBlockerId] 사용자 차단 정보 삭제 완료 ===> userId: {}", userId);
  }

  @Transactional
  public void bulkInsert(final User blocker, final List<User> blockedUsers) {
    if (blockedUsers.isEmpty()) {
      log.debug("[saveBulk] blocks is Empty");
      return;
    }
    blockBulkRepository.bulkInsert(blocker, blockedUsers);
    log.debug("[bulkInsert] Block 저장 완료");
  }

  public List<Block> getAllByUserId(final Long blockerId) {
    return blockRepository.findAllByBlockerId(blockerId);
  }
}
