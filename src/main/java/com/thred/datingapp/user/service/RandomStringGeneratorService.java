package com.thred.datingapp.user.service;

import java.security.SecureRandom;

import com.thred.datingapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RandomStringGeneratorService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    private final UserRepository userRepository;

    public String createRandomCode() {
        String code;
        do {
            code = generateRandom();
        } while (userRepository.existsByCode(code));

        return code;
    }

    private String generateRandom() {
        StringBuilder result = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            result.append(CHARACTERS.charAt(index));
        }
        return result.toString();
    }
}
