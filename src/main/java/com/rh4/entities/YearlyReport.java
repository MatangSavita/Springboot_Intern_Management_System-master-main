package com.rh4.entities;

import com.rh4.entities.WeeklyReport;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "yearly_report")
public class YearlyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int year;

    @Temporal(TemporalType.DATE)
    private Date generatedDate;

    @JoinColumn(name = "intern_id")
    @ManyToOne
    private Intern intern;

    @JoinColumn(name = "group_id")
    @ManyToOne
    private GroupEntity group;

    private Date reportSubmittedDate;
    private Date deadline;

    private int weekNo;


    @Lob
    @Column(name = "submitted_pdf", columnDefinition = "LONGBLOB")
    private byte[] submittedPdf;


    @JoinColumn(name = "guide_id")
    @ManyToOne
    private Guide guide;

    @ManyToOne
    private MyUser replacedBy;

    private String status;


    private int lateSubmissions;

    public YearlyReport() {}

    public YearlyReport(int year, Date generatedDate, Intern intern, GroupEntity group, Date reportSubmittedDate , Date deadline,
                        int weekNo, byte[] submittedPdf, Guide guide, MyUser replacedBy, String status) {
        this.year = year;
        this.generatedDate = generatedDate;
        this.intern = intern;
        this.group = group;
        this.reportSubmittedDate = reportSubmittedDate;
        this.deadline = deadline;
        this.weekNo = weekNo;
        this.submittedPdf = submittedPdf;
        this.guide = guide;
        this.replacedBy = replacedBy;
        this.status = status;

    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Date getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(Date generatedDate) {
        this.generatedDate = generatedDate;
    }

    public Intern getIntern() {
        return intern;
    }
    public void setIntern(Intern intern) {
        this.intern = intern;
    }
    public GroupEntity getGroup() {
        return group;
    }
    public void setGroup(GroupEntity group) {
        this.group = group;
    }
    public Date getReportSubmittedDate() {
        return reportSubmittedDate;
    }
    public void setReportSubmittedDate(Date reportSubmittedDate) {
        this.reportSubmittedDate = reportSubmittedDate;
    }
    public Date getDeadline() {
        return deadline;
    }
    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }
    public int getWeekNo() {
        return weekNo;
    }
    public void setWeekNo(int weekNo) {
        this.weekNo = weekNo;
    }
    public byte[] getSubmittedPdf() {
        return submittedPdf;
    }
    public void setSubmittedPdf(byte[] submittedPdf) {
        this.submittedPdf = submittedPdf;
    }
    public Guide getGuide() {
        return guide;
    }
    public void setGuide(Guide guide) {
        this.guide = guide;
    }
    public MyUser getReplacedBy() {
        return replacedBy;
    }
    public void setReplacedBy(MyUser replacedBy) {
        this.replacedBy = replacedBy;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public int getLateSubmissions() {
        return lateSubmissions;
    }
    public void setLateSubmissions(int lateSubmissions) {
        this.lateSubmissions = lateSubmissions;
    }

}
