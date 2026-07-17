package com.vasuarora.shareiscare.report;

import com.vasuarora.shareiscare.common.dto.ApiResponse;
import com.vasuarora.shareiscare.report.dto.ReportRequest;
import com.vasuarora.shareiscare.report.dto.ReportResponse;
import com.vasuarora.shareiscare.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReportResponse>> fileReport(@Valid @RequestBody ReportRequest request) {
        ReportResponse response = reportService.fileReport(CurrentUser.id(), request);
        return ResponseEntity.ok(ApiResponse.success("Report submitted successfully.", response));
    }
}
