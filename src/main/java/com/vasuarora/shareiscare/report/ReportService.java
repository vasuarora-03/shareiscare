package com.vasuarora.shareiscare.report;

import com.vasuarora.shareiscare.common.exception.ApiException;
import com.vasuarora.shareiscare.report.dto.ReportRequest;
import com.vasuarora.shareiscare.report.dto.ReportResponse;
import com.vasuarora.shareiscare.user.User;
import com.vasuarora.shareiscare.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReportResponse fileReport(Long reporterId, ReportRequest request) {
        if (request.reportedUserId().equals(reporterId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot report yourself.");
        }

        User reportedUser = userRepository.findById(request.reportedUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reported user not found."));

        Report report = Report.builder()
                .reportedBy(userRepository.getReferenceById(reporterId))
                .reportedUser(reportedUser)
                .reason(request.reason())
                .build();

        return ReportResponse.from(reportRepository.save(report));
    }
}
