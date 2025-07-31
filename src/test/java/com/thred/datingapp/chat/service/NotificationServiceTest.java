package com.thred.datingapp.chat.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.thred.datingapp.chat.dto.NotificationDto;
import com.thred.datingapp.chat.repository.FcmTokenRepository;
import com.thred.datingapp.common.type.NotificationType;
import com.thred.datingapp.common.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;


@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private FcmTokenRepository tokenRepository;

    @InjectMocks
    private NotificationService sut;

    @BeforeEach
    public void init() throws Exception {
        try {
            ClassPathResource classPathResource = new ClassPathResource("serviceAccountKey.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(classPathResource.getInputStream()))
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            throw new Exception(e);
        }
    }

    @Test
    @DisplayName("FCM Request Url 테스트")
    void sendMessageToTest() throws Exception {
        //given
        String fcmToken = "cazMSPIQRBGjf9bWgyhB4q:APA91bEWqifIhDwhebUvC3k-0TT02o9--sv4Rx2IczIQPwXUEVcHJigZGvjQzhNElGk0bOtrbQDin7Vjp4uLbwx-JuUZLhC4AKXMH7GsPHdGFVCoVdw-x-k";
        NotificationDto notificationResponse = createNotificationResponse();
        given(tokenRepository.findByMemberUserId(any())).willReturn(Optional.of(fcmToken));

        //when
        sut.sendMessageTo(1L,notificationResponse);
        //then
        then(tokenRepository).should().findByMemberUserId(any());

    }

    private static NotificationDto createNotificationResponse() {
        return NotificationDto.of(
            NotificationType.MESSAGE_SENT,
                1L,
                "userA",
                "chat message",
                "profile",
            LocalDateTime.now()
            );
    }

}
