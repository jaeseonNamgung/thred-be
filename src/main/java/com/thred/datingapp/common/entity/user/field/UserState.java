package com.thred.datingapp.common.entity.user.field;

import lombok.Getter;

@Getter
public enum UserState {
    ACTIVE,
    INACTIVE,
    BLOCKED,
    SUSPENDED,
    WITHDRAW_REQUESTED
}
