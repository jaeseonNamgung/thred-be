package com.thred.datingapp.community.dto.request;

import com.thred.datingapp.common.entity.community.Community;
import com.thred.datingapp.common.entity.user.User;
import jakarta.validation.constraints.NotBlank;

public record CommunityRequest(
        @NotBlank(message = "제목을 입력해 주세요")
        String title,
        String content,
        boolean isPublicProfile
) {
    public static CommunityRequest of(String title, String content, boolean isPublicProfile) {
        return new CommunityRequest(title, content, isPublicProfile);
    }

    public Community toEntity(User user){
        return Community.builder()
                .title(title)
                .content(content)
                .isPublicProfile(isPublicProfile)
                .user(user)
                .build();
    }
}
