package com.thred.datingapp.common.entity.user.field;

import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Job {
    OFFICE_WORKER("직장인"),
    JOB_SEEKER("취준생"),
    PART_TIME("파트타임"),
    STUDENT("학생");
    private static final Map<String, Job> descriptions =
            Collections.unmodifiableMap(Stream.of(values())
                    .collect(Collectors.toMap(Job::getJob, Function.identity())));
    private final String job;

    public static Job findJob(String input) {
        Job findJob = descriptions.get(input);
        if (findJob == null) {
            throw new CustomException(UserErrorCode.INVALID_JOB_STATUS);
        }
        return findJob;
    }
}
