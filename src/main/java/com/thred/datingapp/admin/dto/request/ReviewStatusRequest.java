package com.thred.datingapp.admin.dto.request;

public record ReviewStatusRequest(
        boolean status,
        String reason
) {
}
