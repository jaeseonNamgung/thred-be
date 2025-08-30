package com.thred.datingapp.user.service;

import com.thred.datingapp.common.entity.user.Block;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.common.utils.PhoneNumberUtils;
import com.thred.datingapp.user.api.request.BlockInfoRequest;
import com.thred.datingapp.user.api.response.BlockNumberResponse;
import com.thred.datingapp.user.api.response.BlockNumbersResponse;
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
  private final UserService         userService;

  @Transactional
  public void setBlockNumber(final Long userId, final List<BlockInfoRequest> blockInfoRequests) {
    // 1. 차단 요청을 보낸 회원 조회 (차단자)
    User blocker = userService.getUserById(userId);
    // 2. 차단 요청 목록이 비어 있을 경우, 차단 처리 없이 종료
    if (blockInfoRequests.isEmpty()) {
      log.info("[setBlockNumber] 차단된 연락처 없음 ===> userId: {}", userId);
      return;
    }
    // 3. 차단 대상 회원 목록 조회 (이름 + 전화번호로 매칭)
    List<User> blockedUsers = getBlockedUsers(blockInfoRequests);
    // 4. 해당 사용자가 이전에 차단했던 모든 기록 삭제 (차단 목록 초기화)
    deleteByBlockerId(userId);
    // 5. 새로운 차단 대상이 있을 경우, 차단 정보 저장
    bulkInsert(blocker, blockedUsers);
    log.info("[setBlockNumber] 연락처 차단 정보 수정 완료 ===> userId: {}", userId);
  }

  public BlockNumbersResponse getBlockNumber(final Long userId) {
    List<Block> blocks = getAllByUserId(userId);
    List<BlockNumberResponse> blockResponses = blocks.stream()
                                                     .map(block -> new BlockNumberResponse(block.getBlockedUser().getUsername(), PhoneNumberUtils.toLocalFormat(block.getBlockedUser().getPhoneNumber())))
                                                     .toList();
    log.info("[getBlockNumber] 연락처 차단 조회 성공 ===> blockerId: {}", userId);
    return new BlockNumbersResponse(blockResponses);
  }


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

  private List<User> getBlockedUsers(final List<BlockInfoRequest> blockInfoRequests) {
    return userService.getAllBlockedUsersByPhoneNumberAndName(blockInfoRequests);
  }

}
