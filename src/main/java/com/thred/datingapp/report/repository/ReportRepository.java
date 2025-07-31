package com.thred.datingapp.report.repository;

import com.thred.datingapp.common.entity.report.Report;
import com.thred.datingapp.common.entity.report.ReportType;
import com.thred.datingapp.report.repository.queryDsl.ReportQueryDsl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long>, ReportQueryDsl {

  @Query("select r from Report r join fetch r.reportedUser where r.id = :reportId")
  Optional<Report> findByIdFetchReportedUser(@Param("reportId") Long reportId);

  @Query("select r from Report r where r.reportedUser.id = :reportedUserId and r.targetId = :targetId and r.reportType = :reportType")
  Optional<Report> findByReportedUserIdAndTargetIdAndReportType(@Param("reportedUserId") Long reportedUserId, @Param("targetId") Long targetId, @Param("reportType") ReportType reportType);

  @Query("select count(r) > 0 from Report r where r.reporter.id = :reporterId and r.reportedUser.id = :reportedUserId and r.targetId = :targetId and r.reportType = :reportType")
  boolean existsReport(@Param("reporterId") Long reporterId,
                     @Param("reportedUserId") Long reportedUserId,
                     @Param("targetId") Long targetId,
                     @Param("reportType") ReportType reportType);

  @Modifying(clearAutomatically = true)
  @Query("delete from Report r where r.id = :reportId")
  void deleteByReportId(@Param("reportId") Long reportId);

  @Modifying(clearAutomatically = true)
  @Query("delete from Report r where r.reportedUser.id = :userId or r.reporter.id = :userId")
  void deleteAllByReporterOrReportedUser(@Param("userId") Long userId);

}
