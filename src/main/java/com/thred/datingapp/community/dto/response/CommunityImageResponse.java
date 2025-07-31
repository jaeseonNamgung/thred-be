package com.thred.datingapp.community.dto.response;

import com.thred.datingapp.common.entity.community.CommunityImage;

import java.util.List;

public record CommunityImageResponse(
        long imageId,
        String s3Path,
        String originalFileName
) {

    public static CommunityImageResponse of(
            long imageId,
            String s3Path,
            String originalFileName
    ) {
        return new CommunityImageResponse(imageId, s3Path, originalFileName);
    }

    public static CommunityImageResponse fromResponse(CommunityImage communityImage) {
        return CommunityImageResponse.of(communityImage.getId(), communityImage.getS3Path(), communityImage.getOriginalFileName());
    }

    public static List<CommunityImageResponse> fromResponse(List<CommunityImage> communityImages) {
        return communityImages.stream().map(CommunityImageResponse::fromResponse).toList();
    }
}
