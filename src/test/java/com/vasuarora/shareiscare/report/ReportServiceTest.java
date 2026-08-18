package com.vasuarora.shareiscare.report;

import com.vasuarora.shareiscare.common.exception.ApiException;
import com.vasuarora.shareiscare.report.dto.ReportRequest;
import com.vasuarora.shareiscare.report.dto.ReportResponse;
import com.vasuarora.shareiscare.user.User;
import com.vasuarora.shareiscare.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void fileReport_success() {
        User reporter = User.builder().id(1L).name("Reporter").phone("9000000001").build();
        User reportedUser = User.builder().id(2L).name("Reported").phone("9000000002").build();
        ReportRequest request = new ReportRequest(2L, "Was rude.");

        when(userRepository.findById(2L)).thenReturn(Optional.of(reportedUser));
        when(userRepository.getReferenceById(1L)).thenReturn(reporter);
        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));

        ReportResponse response = reportService.fileReport(1L, request);

        assertThat(response.reportedUserId()).isEqualTo(2L);
        assertThat(response.reason()).isEqualTo("Was rude.");
    }

    @Test
    void fileReport_cannotReportSelf_throws400() {
        ReportRequest request = new ReportRequest(1L, "Was rude.");

        assertThatThrownBy(() -> reportService.fileReport(1L, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("cannot report yourself");
    }

    @Test
    void fileReport_reportedUserNotFound_throws404() {
        ReportRequest request = new ReportRequest(2L, "Was rude.");
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.fileReport(1L, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Reported user not found");
    }
}
