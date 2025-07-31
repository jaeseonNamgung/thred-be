package com.thred.datingapp.chat.repository.queryDsl;

import java.util.Optional;

public interface FcmTokenQueryDsl {
    Optional<String> findByMemberUserId(Long userId);
}
