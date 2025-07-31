package com.thred.datingapp.admin.dto.response;

import java.time.Duration;
import java.time.LocalDateTime;

public record ReviewResponse(
    Long reviewId,
    String username,
    String mainProfile,
    String createTime
) {
    public static ReviewResponse of(
        Long reviewId,
        String username,
        String mainProfile,
        LocalDateTime lastModifiedDate
    ) {
        return new ReviewResponse(reviewId, username, mainProfile, getTimeAgo(lastModifiedDate));
    }

    private static String getTimeAgo(LocalDateTime createDate) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createDate, now);

        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) {
            return seconds + "초 전";
        } else if (minutes < 60) {
            return minutes + "분 전";
        } else if (hours < 24) {
            return hours + "시간 전";
        } else {
            return days + "일 전";
        }
    }
}
