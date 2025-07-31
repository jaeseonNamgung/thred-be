package com.thred.datingapp.main.dto.request;

import java.util.List;
import java.util.Map;

public record EditProfileRequest(
        boolean mainChange,
        String newMainProfile,
        List<Long> changedFileIds,
        List<String> existImage,
        Map<String, String> newExtraProfiles
) {
    public static EditProfileRequest of(boolean mainChange, String newMainProfile, List<Long> changedFileIds,
                                        List<String> existImage, Map<String, String> newExtraProfiles) {
        return new EditProfileRequest(mainChange, newMainProfile, changedFileIds, existImage, newExtraProfiles);
    }
}
