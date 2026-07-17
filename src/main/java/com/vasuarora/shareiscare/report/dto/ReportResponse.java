package com.vasuarora.shareiscare.report.dto;

import com.vasuarora.shareiscare.report.Report;

import java.time.LocalDateTime;

public record ReportResponse(
        Long id,
        Long reportedUserId,
        String reason,
        LocalDateTime createdAt
) {

    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getReportedUser().getId(),
                report.getReason(),
                report.getCreatedAt()
        );
    }
}
