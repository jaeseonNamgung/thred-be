package com.thred.datingapp.report.repository;

import com.thred.datingapp.common.entity.report.ReportHistory;
import com.thred.datingapp.common.entity.report.ReportResult;
import com.thred.datingapp.common.entity.report.ReportType;
import com.thred.datingapp.report.repository.queryDsl.ReportHistoryQueryDsl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReportHistoryRepository extends JpaRepository<ReportHistory, Long>, ReportHistoryQueryDsl {

    Optional<ReportHistory> findByReportedUserIdAndTargetId(Long reportedUserId, Long targetId);

    @Query("select rh.suspendedDate from ReportHistory rh where rh.reportedUserId = :reportedUserId")
    List<LocalDate> findSuspendedDateByReportedUserId(@Param("reportedUserId") Long reportedUserId);

    @Modifying(clearAutomatically = true)
    @Query("update ReportHistory set result = :result, suspendedDate = :suspendedDate, reportType =:reportType  where id = :reportHistoryId")
    void updateReportApprovalResult(
            @Param("result") ReportResult reportResult,
            @Param("suspendedDate") LocalDate suspendedDate,
            @Param("reportHistoryId") Long reportHistoryId,
            @Param("reportType")ReportType reportType
            );

    @Query("select rh.id from ReportHistory rh where rh.reportedUserId = :reportedUserId and rh.targetId = :targetId")
    Optional<Long> findHistoryIdByReportedUserIdAndTargetId(@Param("reportedUserId") Long reportedUserId, @Param("targetId") Long targetId);

    @Modifying(clearAutomatically = true)
    @Query("update ReportHistory set result = :result where id = :reportHistoryId")
    void updateReportRejectResult(@Param("result") ReportResult reportResult, @Param("reportHistoryId") Long reportHistoryId);

    @Query("select rh from ReportHistory rh where rh.suspendedDate >= CURRENT DATE order by rh.createdDate desc")
    List<ReportHistory> findAll();

}
