package com.thred.datingapp.main.dto.response;

import static java.util.stream.Collectors.toList;

import com.thred.datingapp.common.entity.user.Picture;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.main.dto.request.EditProfileRequest;
import com.thred.datingapp.main.dto.request.EditTotalRequest;
import java.util.List;
import java.util.Map;

public record PicturesResponse(
        String mainProfile,
        List<String> profiles
) {
    public static PicturesResponse of(User user) {
        List<String> profiles = user.getProfiles().stream()
                .map(Picture::getS3Path).collect(toList());
        String mainProfile = user.getMainProfile();
        return new PicturesResponse(mainProfile, profiles);
    }

    public static PicturesResponse of(EditTotalRequest request) {
        List<String> profiles = request.extraProfiles();
        String mainProfile = request.mainProfile();
        return new PicturesResponse(mainProfile, profiles);
    }

    public static PicturesResponse of(EditProfileRequest request, User user) {
        Map<String, String> newProfiles = request.newExtraProfiles();
        List<String> newProfileslist = newProfiles.values().stream().toList();
        List<String> exist = request.existImage();
        List<String> profiles = new java.util.ArrayList<>(List.copyOf(exist));
        profiles.addAll(newProfileslist);
        if (request.mainChange()) {
            return new PicturesResponse(request.newMainProfile(), profiles);
        }
        return new PicturesResponse(user.getMainProfile(), profiles);
    }
}
