package com.thred.datingapp.user.api.response;

import com.thred.datingapp.common.entity.user.Picture;

public record ProfileResponse(
        Long id,
        String profile
) {
    public static ProfileResponse of(Picture picture) {
        return new ProfileResponse(picture.getId(), picture.getS3Path());
    }
}
