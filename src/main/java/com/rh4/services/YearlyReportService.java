package com.rh4.services;

import com.rh4.entities.WeeklyReport;
import com.rh4.entities.YearlyReport;
import com.rh4.repositories.WeeklyReportRepo;
import com.rh4.repositories.YearlyReportRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class YearlyReportService {

    @Autowired
    private WeeklyReportRepo weeklyReportRepo;

    @Autowired
    private YearlyReportRepo yearlyReportRepo;

    public YearlyReportService(YearlyReportRepo yearlyReportRepository) {
        this.yearlyReportRepo = yearlyReportRepository;
    }


}
