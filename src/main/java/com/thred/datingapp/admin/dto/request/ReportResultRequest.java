package com.thred.datingapp.admin.dto.request;

import jakarta.validation.constraints.NotNull;

public record ReportResultRequest(
        @NotNull
        boolean result,
        int suspensionDays
) {
}
