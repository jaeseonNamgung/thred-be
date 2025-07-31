package com.thred.datingapp.user.api.response;

public record DuplicateResponse(
        Boolean check
) {
    public static DuplicateResponse of(boolean check) {
        return new DuplicateResponse(check);
    }
}
