package com.thred.datingapp.join.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Vector;

import com.thred.datingapp.user.repository.UserRepository;
import com.thred.datingapp.user.service.RandomStringGeneratorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class RandomStringGeneratorServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RandomStringGeneratorService random;

    @Test
    @DisplayName("기본적인 랜덤 문자열 생성 테스트")
    void randomString() {
        String randomCode = random.createRandomCode();

        assertThat(randomCode).isNotNull();
        assertThat(randomCode.length()).isEqualTo(6);
        assertThat(randomCode.matches("[A-Z0-9]+"));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    @DisplayName("중복 방지 문자열 테스트")
    void checkDuplicate() {
        Vector<String> v = new Vector<>();
        for (int i = 0; i < 1000; i++) {
            String randomCode = random.createRandomCode();
            given(userRepository.existsByCode(randomCode)).willAnswer(answer -> {
                String code = answer.getArgument(0);
                return v.contains(code);
            });
            assertThat(v.contains(randomCode)).isFalse();
            v.add(randomCode);
        }
    }

    @Test
    @DisplayName("문자열 직접 확인")
    @MockitoSettings(strictness = Strictness.LENIENT)
    void check() {
        Vector<String> v = new Vector<>();
        for (int i = 0; i < 1000; i++) {
            String randomCode = random.createRandomCode();
            given(userRepository.existsByCode(randomCode)).willAnswer(answer -> {
                String code = answer.getArgument(0);
                return v.contains(code);
            });
            System.out.println(randomCode);
            v.add(randomCode);
        }
    }
}
