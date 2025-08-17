package com.thred.datingapp.community.dto.response;

import com.thred.datingapp.common.entity.community.Community;

import java.time.LocalDateTime;
import java.util.List;

public record CommunityResponse(
        long communityId,
        String title,
        String content,
        List<CommunityImageResponse> images,
        boolean isAuthor,
        boolean isPublicProfile,
        long userId,
        String nickName,
        String profile,
        LocalDateTime createdDate,
        boolean statusLike,
        List<CommentResponse> parentComments
) {

    public static CommunityResponse of(
            long communityId,
            String title,
            String content,
            List<CommunityImageResponse> images,
            boolean isAuthor,
            boolean isPublicProfile,
            long userId,
            String nickName,
            String profile,
            LocalDateTime createdDate,
            boolean statusLike,
            List<CommentResponse> parentComments
    ) {
        return new CommunityResponse(communityId, title, content, images, isAuthor, isPublicProfile, userId, nickName, profile, createdDate, statusLike, parentComments);
    }

    public static CommunityResponse fromResponse(Community community, List<CommentResponse> comments, String userProfile, boolean statusLike, long userId) {
        return CommunityResponse.of(
                community.getId(),
                community.getTitle(),
                community.getContent(),
                community.getCommunityImages().isEmpty() ? List.of() : CommunityImageResponse.fromResponse(community.getCommunityImages()),
                community.getUser().getId().equals(userId),
                community.getIsPublicProfile(),
                community.getUser().getId(),
                community.getUser().getUsername(),
                userProfile,
                community.getCreatedDate(),
                statusLike,
                comments != null ? comments : List.of()
        );
    }
}
