package com.thred.datingapp.user.api.request;

import java.util.List;

public record BlockNumbersRequest(
        List<BlockInfoRequest> numbers
) {
}
