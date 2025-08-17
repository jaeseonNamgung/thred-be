package com.thred.datingapp.common.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.thred.datingapp.chat.dto.NotificationDto;
import com.thred.datingapp.chat.repository.FcmTokenRepository;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.BaseErrorCode;
import com.thred.datingapp.common.error.errorCode.FcmTokenErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationService {
    private final FcmTokenRepository fcmTokenRepository;

    public void sendMessageTo(final Long userId, final NotificationDto notificationDto) {
        fcmTokenRepository.findByMemberUserId(userId)
                          .ifPresent(token -> {
                              try {
                                  FirebaseMessaging.getInstance().send(Message.builder()
                                                                              .setNotification(makeMessage(notificationDto))
                                                                              .setToken(token).build());
                                  log.info("[sendMessageTo] Firebase 알림 메시지 전송 완료(Successfully sent Firebase notification) ===> notificationDto: {}", notificationDto);
                              } catch (FirebaseMessagingException e) {
                                  log.error("[sendMessageTo] 알림 메세지 생성 중 오류(Error during notification message creation)");
                                  log.error(e.toString());
                                  throw new CustomException(FcmTokenErrorCode.NOTIFICATION_ERROR);
                              }
                          });
    }

    private Notification makeMessage(final NotificationDto notificationDto) {
        return Notification.builder()
                           .setTitle(notificationDto.nickName())
                           .setBody(notificationDto.message())
                           .setImage(notificationDto.profile())
                           .build();
    }

}
