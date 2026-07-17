package com.vasuarora.shareiscare.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportRequest(

        @NotNull(message = "Reported user is required.")
        Long reportedUserId,

        @NotBlank(message = "Reason is required.")
        String reason
) {
}
