package com.rh4.repositories;


import com.rh4.entities.YearlyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface YearlyReportRepo extends JpaRepository<YearlyReport, Long> {
//    List<YearlyReport> yearlyReports = YearlyReportRepo.findAll();
    List<YearlyReport> findByYear(Integer year);
}