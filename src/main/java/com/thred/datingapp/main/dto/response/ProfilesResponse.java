package com.thred.datingapp.main.dto.response;

import com.thred.datingapp.user.api.response.ProfileResponse;
import java.util.List;

public record ProfilesResponse(
        String mainProfile,
        List<ProfileResponse> extraProfiles
) {
    public static ProfilesResponse of(String mainProfile, List<ProfileResponse> extraProfiles) {
        return new ProfilesResponse(mainProfile, extraProfiles);
    }
}
