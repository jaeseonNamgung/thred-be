package com.thred.datingapp.main.dto.response;

import com.thred.datingapp.main.dto.request.EditDetailsRequest;
import com.thred.datingapp.main.dto.request.EditUserRequest;
import java.util.List;

public record EditTotalResponse(
        EditUserRequest user,
        EditDetailsRequest details,
        String mainProfile,
        List<String> profiles
) {
    public static EditTotalResponse of(EditUserRequest user,
                                       EditDetailsRequest details,
                                       String mainProfile,
                                       List<String> profiles) {
        return new EditTotalResponse(user, details, mainProfile, profiles);
    }
}
