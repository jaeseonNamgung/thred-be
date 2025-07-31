package com.thred.datingapp.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thred.datingapp.chat.dto.PartnerInfoDto;
import com.thred.datingapp.chat.dto.response.ChatRoomAllResponse;
import com.thred.datingapp.chat.dto.response.ChatRoomResponse;
import com.thred.datingapp.chat.repository.ChatPartRepository;
import com.thred.datingapp.chat.repository.ChatRoomRepository;
import com.thred.datingapp.chat.repository.queryDsl.ChatRepository;
import com.thred.datingapp.common.api.response.PageResponse;
import com.thred.datingapp.common.entity.chat.Chat;
import com.thred.datingapp.common.entity.chat.ChatPart;
import com.thred.datingapp.common.entity.chat.ChatRoom;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.BaseErrorCode;
import com.thred.datingapp.common.error.errorCode.ChatErrorCode;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.common.utils.RedisUtils;
import com.thred.datingapp.main.service.CardService;
import com.thred.datingapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static com.thred.datingapp.chat.properties.ChatProperties.CHAT_ROOM_PREFIX;

@Log4j2
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ChatRoomService {

  private final RedisUtils         redisUtil;
  private final ChatRoomRepository chatRoomRepository;
  private final UserRepository     userRepository;
  private final ChatPartRepository chatPartRepository;
  private final ChatRepository     chatRepository;
  private final CardService        cardService;

  /**
   * @Author NamgungJaeseon
   * @Date 2025.01.19
   * @Description 채팅방이 만들어지는 과정 -> 유저A가 유저B에게 호감을 표시하고 유저B가 수락하는 순간 채팅방 생성
   */
  @Transactional
  public ChatRoomResponse createChatRoom(final Long userId, final Long receiverId) {
    User sender = userRepository.findById(userId).orElseThrow(() -> {
      log.error("[createChatRoom] 존재하지 않은 발신자(Not found sender) ===> senderId={}", userId);
      return new CustomException(UserErrorCode.USER_NOT_FOUND);
    });
    log.debug("[createChatRoom] 발신자 조회 완료(Sender found successfully) ===>  sender: {}", sender);
    User receiver = userRepository.findById(receiverId).orElseThrow(() -> {
      log.error("[createChatRoom] 존재하지 않은 수신자(Not found receiver) ===> receiverId={}", receiverId);
      return new CustomException(UserErrorCode.USER_NOT_FOUND);
    });
    log.debug("[createChatRoom] 수신자 조회 완료(Receiver found successfully) ===>  receiver: {}", receiver);
    ChatRoom savedChatRoom = chatRoomRepository.save(ChatRoom.createChatRoom());

    // 채팅 파트를 두개 생성하는 이유? 나중에 확장성을 고려 (예: 단체 채팅) - 동적으로 채팅 유저를 생성하기 위한 목적
    chatPartRepository.save(ChatPart.builder().chatRoom(savedChatRoom).user(sender).build());
    chatPartRepository.save(ChatPart.builder().chatRoom(savedChatRoom).user(receiver).build());
    log.info("[createChatRoom] ChatPart 저장 완료(Successfully saved ChatPart)");

    return ChatRoomResponse.fromResponse(savedChatRoom);
  }

  public PageResponse<ChatRoomAllResponse> getAllChatRooms(final Long userId, final Long pageLastId, final int pageSize) {
    // 채팅 파트 조회
    Page<ChatRoom> page = chatRoomRepository.findChatRoomsByUserIdWithPagination(userId, pageLastId, pageSize);
    List<ChatRoomAllResponse> chatRoomAllResponse = page.getContent().stream().map(chatRoom -> {
      Long chatRoomId = chatRoom.getId();

      // 1. 채팅 메시지 전체 조회
      List<Chat> chats = chatRepository.findChatsByRoomId(chatRoomId, userId);
      log.info("[getAllChatRooms] 파트너 채팅 메시지 조회 완료(Successfully selected partner chat messages) ===>  chatRoomId: {}, senderId: {}", chatRoomId,
               userId);

      // 2. 읽지 않은 메시지 수 조회
      int unReadCount =
          (int) chats.stream().filter(chat -> !chat.getChatPart().getUser().getId().equals(userId)).filter(chat -> !chat.isReadStatus()).count();
      log.info("[getAllChatRooms] 읽지 않은 메시지 수(Unread chat message count) ===>  unReadCount: {}", unReadCount);

      // 3. 파트너 정보 조회
      PartnerInfoDto partnerInfo = getPartnerInfo(chatRoom.getChatParts(), userId);

      return ChatRoomAllResponse.from(chatRoom.getId(), partnerInfo.id(), partnerInfo.nickName(), partnerInfo.mainProfile(),
                                      chats.isEmpty() ? "" : chats.get(0).getMessage(), unReadCount,
                                      chats.isEmpty() ? null : chats.get(0).getCreatedDate());
    }).toList();

    return PageResponse.of(page.getSize(), page.isLast(), chatRoomAllResponse);
  }

  /**
   * @Author NamgungJaeseon
   * @Date 2025.01.19
   * @Description 파트너의 파트너 ID, 닉네임, 프로필을 Map을 사용해서 값을 저장
   */
  private PartnerInfoDto getPartnerInfo(final List<ChatPart> chatParts, final Long userId) {
    return chatParts.stream()
                    .filter(chatPart -> !chatPart.getUser().getId().equals(userId))
                    .findFirst()
                    .map(PartnerInfoDto::toDto)
                    .orElseThrow(() -> {
                      log.error("[getPartnerInfo] 파트너 ChatPart 가 존재하지 않습니다.(Not found partner chatPart) ===>  userId: {}", userId);
                      return new CustomException(BaseErrorCode.INTERNAL_SERVER_ERROR);
                    });
  }

  @Transactional
  public boolean deleteChatRoom(final Long chatRoomId) {

    // 1. 채팅방 조회
    boolean hasChatRoom = chatRoomRepository.existChatRoomByChatRoomId(chatRoomId);

    if (!hasChatRoom) {
      log.error("[deleteChatRoom] 존재하지 않은 채팅방(Not found chatRoom) ===>  chatRoomId: {}", chatRoomId);
      throw new CustomException(ChatErrorCode.NOT_FOUND_CHATROOM);
    }

    chatRepository.deleteAllByChatRoomId(chatRoomId);
    log.info("[deleteChatRoom] 채팅 메시지 전체 삭제 완료(Successfully deleted all chat messages) ===>  chatRoomId: {}", chatRoomId);
    chatPartRepository.deleteAllByChatRoomId(chatRoomId);
    log.info("[deleteChatRoom] 채팅 파트 삭제 완료(Successfully deleted chatPart) ===>  chatRoomId: {}", chatRoomId);
    chatRoomRepository.deleteByChatRoomId(chatRoomId);
    log.info("[deleteChatRoom] 채팅방 삭제 완료(Successfully deleted chatRoom) ===>  chatRoomId: {}", chatRoomId);

    return true;
  }

  @Transactional
  public boolean deleteAllChatsForWithdrawnUser(final Long userId) {

    // 1. 회원 조회
    boolean existUser = userRepository.existsById(userId);
    if (!existUser) {
      log.error("[deleteUserChatHistory] 존재하지 않은 사용자(Not found user) ===> userId: {}", userId);
      throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    }

    // 2. 회원이 접속중인 채팅방 전체 조회
    List<Long> chatRoomIds = chatPartRepository.findChatRoomIdAllByUserId(userId);
    if (chatRoomIds.isEmpty()) {
      log.info("[deleteUserChatHistory] 존재하지 않은 채팅방(Not found chatRoom) ===> userId: {}", userId);
      return true;
    }

    // 3. 회원이 접속한 채팅방 전체 삭제 (채팅 메시지 -> 채팅 파트 -> 채팅방)
    chatRepository.deleteAllByChatRoomIds(chatRoomIds);
    log.info("[deleteChatRoom] 채팅 메시지 전체 삭제 완료(Successfully deleted all chat messages) ===>  chatRoomIds: {}", chatRoomIds);
    chatPartRepository.deleteAllByChatRoomIds(chatRoomIds);
    log.info("[deleteChatRoom] 채팅 파트 전체 삭제 완료(Successfully deleted all chatPart) ===>  chatRoomIds: {}", chatRoomIds);
    chatRoomRepository.deleteAllByChatRoomId(chatRoomIds);
    log.info("[deleteChatRoom] 채팅방 전체 삭제 완료(Successfully deleted all chatRoom) ===>  chatRoomIds: {}", chatRoomIds);
    return true;
  }

  /*
   * @Author NamgungJaeseon
   * @Date 2025.04.26
   * @Description 채팅방 ID + 회원 ID 레디스에 저장
   * */
  @Transactional
  public void saveChatConnectionInfo(final Long chatRoomId, final Long userId){
    boolean isExistChatRoom = chatRoomRepository.existChatRoomByChatRoomId(chatRoomId);
    if (isExistChatRoom) {
      redisUtil.saveConnected(CHAT_ROOM_PREFIX + chatRoomId, userId);
      log.info("[saveConnected] 레디스에 채팅 정보 저장 완료(Successfully saved chat information to Redis) ===>  chatRoomId: {}, userId: {}", chatRoomId, userId);
    } else {
      log.error("[saveConnected] 존재하지 않은 채팅방 또는 사용자입니다.(Not found chatRoom or user) ===>  chatRoomId: {}, userId: {}", chatRoomId, userId);
      throw new CustomException(ChatErrorCode.NOT_FOUND_CHATROOM);
    }
  }

  // FIXME 추후 사용하지 않으면 isConnected 메서드 삭제
  public boolean isConnected(final Long chatRoomId) {
    Set<Long> connectedUser = redisUtil.getConnected(CHAT_ROOM_PREFIX + chatRoomId, Long.class);
    // 유저 A 이외에 유저 B가 접속되었다면 true를 리턴
    return !connectedUser.isEmpty() && connectedUser.size() > 1;
  }

  @Transactional
  public void disconnectFromChatRoom(final Long chatRoomId, final Long userId) {
    redisUtil.removeConnected(CHAT_ROOM_PREFIX + chatRoomId, userId);
    log.debug("[disconnectFromChatRoom] 레디스 채팅 정보 삭제 완료 ===>  chatRoomId: {}, userId: {}", chatRoomId, userId);
  }

  /*
   * @Author NamgungJaeseon
   * @Date 2025.06.09
   * @Description 관리자 또는 회원탈퇴 시 채팅 정보 전부 삭제,
   * */
  @Transactional
  public void deleteAllChatHistory(Long chatRoomId, Long userId) {

    // 1. 채팅방 조회
    chatRoomRepository.findByChatRoomIdAndUserId(chatRoomId, userId).orElseThrow(() -> {
      log.error("[deleteChatRoomByAdmin] 삭제 실패 - 존재하지 않는 채팅방 ===>  chatRoomId: {}, userId: {}", chatRoomId, userId);
      return new CustomException(ChatErrorCode.NOT_FOUND_CHATROOM);
    });

    // 2. 채팅 메시지 -> 채팅 파트 -> 채팅방 순으로 삭제
    chatRepository.deleteAllByChatRoomId(chatRoomId);
    log.info("[deleteChatRoomByAdmin] 채팅 메시지 전체 삭제 완료(Successfully deleted all chat messages) ===>  chatRoomId: {}", chatRoomId);
    chatPartRepository.deleteAllByChatRoomId(chatRoomId);
    log.info("[deleteChatRoomByAdmin] 채팅 파트 전체 삭제 완료(Successfully deleted all chatPart) ===>  chatRoomId: {}", chatRoomId);
    chatRoomRepository.deleteByChatRoomId(chatRoomId);
    log.info("[deleteChatRoomByAdmin] 채팅방 전체 삭제 완료(Successfully deleted chatRoom) ===>  chatRoomId: {}", chatRoomId);

  }

}
