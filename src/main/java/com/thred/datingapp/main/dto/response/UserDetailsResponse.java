package com.thred.datingapp.main.dto.response;

import com.thred.datingapp.common.entity.user.UserDetail;
import com.thred.datingapp.common.entity.user.Question;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.main.dto.request.EditProfileRequest;
import com.thred.datingapp.main.dto.request.EditTotalRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
public class UserDetailsResponse {
    private final DetailsResponse details;
    private final PicturesResponse profiles;
    private final UserResponse user;
    @Setter
    boolean answer;

    public UserDetailsResponse(DetailsResponse details, PicturesResponse profiles, UserResponse user) {
        this.details = details;
        this.profiles = profiles;
        this.user = user;
    }

    public static UserDetailsResponse of(UserDetail userDetail, Question question) {
        return new UserDetailsResponse(
                DetailsResponse.of(userDetail),
                PicturesResponse.of(userDetail.getUser()),
                UserResponse.of(userDetail.getUser(), question)
        );
    }

    public static UserDetailsResponse of(EditTotalRequest request, User user) {
        return new UserDetailsResponse(
                DetailsResponse.of(request),
                PicturesResponse.of(request),
                UserResponse.of(request, user)
        );
    }

    public static UserDetailsResponse of(EditTotalRequest request, User user, String introduce) {
        return new UserDetailsResponse(
                DetailsResponse.of(request),
                PicturesResponse.of(request),
                UserResponse.of(request, user, introduce)
        );
    }

    public static UserDetailsResponse of(EditTotalRequest request, User user, Question question) {
        return new UserDetailsResponse(
                DetailsResponse.of(request),
                PicturesResponse.of(request),
                UserResponse.of(request, user, question)
        );
    }

    public static UserDetailsResponse of(EditProfileRequest request, UserDetail userDetail, Question question) {
        return new UserDetailsResponse(
                DetailsResponse.of(userDetail),
                PicturesResponse.of(request, userDetail.getUser()),
                UserResponse.of(userDetail.getUser(), question)
        );
    }
}
