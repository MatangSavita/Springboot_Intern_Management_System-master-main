package com.rh4.services;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Base64;


import com.rh4.entities.Intern;
//import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.rh4.entities.GroupEntity;
import com.rh4.entities.WeeklyReport;
import com.rh4.repositories.WeeklyReportRepo;

@Service
public class WeeklyReportService {

	@Autowired
	private WeeklyReportRepo weeklyReportRepo;

	public int getRecentWeekNo(GroupEntity group) {
			List<WeeklyReport> reports = weeklyReportRepo.getRecentWeekNo(group);
	    if (!reports.isEmpty()) {
	        return reports.get(0).getWeekNo() + 1;
	    } else {
	        // Handle the case where no reports are found
	        return 1; // Or any other appropriate value
	    }
	}

	public void addReport(WeeklyReport weeklyReport) {
		weeklyReportRepo.save(weeklyReport);
		
	}

	public List<WeeklyReport> getReportsByGroupId(long id) {
		return weeklyReportRepo.findAllByGroupId(id);
	}
	
	public List<WeeklyReport> getReportsByGuideId(long id) {
		return weeklyReportRepo.findAllByGuideId(id);
	}
	
//	public WeeklyReport getReportByWeekNoAndGroupId(int weekNo, GroupEntity group) {
//		return weeklyReportRepo.findByWeekNoAndGroup(weekNo,group);
//	}

	public List<WeeklyReport> getAllReports() {
		return weeklyReportRepo.findAll();
	}

	public WeeklyReport getReportByInternIdAndWeekNo(String internId, int weekNo) {
		return weeklyReportRepo.findByInternInternIdAndWeekNo(internId, weekNo);
	}


	//self write code
	public WeeklyReport getReportByWeekNoAndGroupId(int weekNo, GroupEntity group) {
		if (group == null) {
			throw new IllegalArgumentException("Group cannot be null");
		}
		return Optional.ofNullable(weeklyReportRepo.findByWeekNoAndGroup(weekNo, group))
				.orElseThrow(() -> new IllegalArgumentException("Report not found for group " + group.getGroupId() + " and weekNo " + weekNo));
	}

	//	for search report yearwise------------

	public List<WeeklyReport> getReportsByYear(int year) {
		return weeklyReportRepo.findReportsByYear(year);
	}


// fetch the report of view pdf in admin controller

	public String convertToBase64(byte[] filedata) {
		if (filedata == null || filedata.length == 0) {
			return null;
		}
		return Base64.getEncoder().encodeToString(filedata);
	}


//	for year------------

	public List<WeeklyReport> getReportsByYear(int year) {
		return weeklyReportRepo.findReportsByYear(year);
	}

//	public WeeklyReport getReportByWeekNoGroupIdAndInternId(int weekNo, GroupEntity group, String internId) {
//		return weeklyReportRepo.findByWeekNoAndGroupAndInternId(weekNo, group, internId);
//	}
}
