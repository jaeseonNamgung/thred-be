package com.thred.datingapp.main.dto.request;

import jakarta.validation.Valid;
import java.util.List;

public record EditTotalRequest(
        @Valid EditUserRequest user,
        @Valid EditDetailsRequest details,
        String mainProfile,
        List<String> extraProfiles
) {
    public static EditTotalRequest of(EditUserRequest user,
                                      EditDetailsRequest details,
                                      String mainProfile,
                                      List<String> extraProfiles) {
        return new EditTotalRequest(user, details, mainProfile, extraProfiles);
    }
}
