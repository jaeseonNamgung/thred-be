package com.thred.datingapp.user.api.response;

import java.util.List;

public record BlockNumbersResponse(
        List<BlockNumberResponse> numbers
) {
}
