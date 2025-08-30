package com.thred.datingapp.user.service;

import com.testFixture.UserFixture;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.user.api.request.BlockInfoRequest;
import com.thred.datingapp.user.repository.BlockBulkRepository;
import com.thred.datingapp.user.repository.BlockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class BlockServiceTest {

  @Mock
  private UserService userService;
  @Mock
  private BlockRepository blockRepository;
  @Mock
  private BlockBulkRepository blockBulkRepository;
  @InjectMocks
  private BlockService sut;

  @Test
  @DisplayName("setBlockNumber 성공 테스트")
  void givenValidUserIdAndRequest_whenSetBlockNumber_thenReturnVoid() {
    // given
    List<BlockInfoRequest> blocks = createBlocks();
    User user = UserFixture.createTestUser(1);
    List<User> blockedUsers = IntStream.rangeClosed(2, 6).mapToObj(UserFixture::createTestUser).toList();
    given(userService.getUserById(anyLong())).willReturn(user);
    given(userService.getAllBlockedUsersByPhoneNumberAndName(any())).willReturn(blockedUsers);
    // when
    sut.setBlockNumber(1L, blocks);
    // then
    then(userService).should().getUserById(anyLong());
    then(userService).should().getAllBlockedUsersByPhoneNumberAndName(any());
    then(blockRepository).should().deleteByBlockerId(anyLong());
    then(blockBulkRepository).should().bulkInsert(any(User.class), anyList());
  }

  private static List<BlockInfoRequest> createBlocks() {
    return List.of(new BlockInfoRequest("김민수", "010-1234-5678"), new BlockInfoRequest("이영희", "010-2345-6789"),
                   new BlockInfoRequest("박준형", "010-3456-7890"), new BlockInfoRequest("최지우", "010-4567-8901"),
                   new BlockInfoRequest("정해인", "010-5678-9012"));
  }

}
