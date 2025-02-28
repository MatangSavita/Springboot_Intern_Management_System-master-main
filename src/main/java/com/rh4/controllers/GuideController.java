package com.rh4.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.*;

import com.rh4.entities.*;
import com.rh4.models.ProjectDefinition;
import com.rh4.repositories.GuideRepo;
import com.rh4.repositories.InternRepo;
import com.rh4.services.*;
import jakarta.persistence.Id;
import org.apache.xmlbeans.impl.soap.SOAPElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.rh4.repositories.GroupRepo;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bisag/guide")
public class GuideController {

    @Autowired
    HttpSession session;
    @Autowired
    private GuideService guideService;
    @Autowired
    private InternService internService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private WeeklyReportService weeklyReportService;
    @Autowired
    private GroupRepo groupRepo;
    @Autowired
    private VerificationService verificationService;
    @Autowired
    private InternRepo internRepo;
    @Autowired
    private MyUserService myUserService;
    Intern internFromUploadFileMethod;
    int CurrentWeekNo;
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private GuideRepo guideRepo;
    @Autowired
    private GroupEntity groupEntity;
    @Autowired
    private GroupEntity group;

    public static String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public Guide getSignedInGuide() {
        String username = (String) session.getAttribute("username");
        Guide guide = guideService.getGuideByUsername(username);
        return guide;
    }

    public String getUsername() {
        String username = (String) session.getAttribute("username");
        return username;
    }

    @GetMapping("/guide_dashboard")
    public ModelAndView guide_dashboard(HttpSession session, Model model) {

        ModelAndView mv = new ModelAndView("guide/guide_dashboard");

        Guide guide = getSignedInGuide();
        String username = getUsername();

        long gPendingCount = groupService.countGPendingGroups();
        mv.addObject("gPendingCount", gPendingCount);

        // Set the "id" and "username" attributes in the session
        session.setAttribute("id", guide.getGuideId());
        session.setAttribute("username", username);

        // Add the username to the ModelAndView
        mv.addObject("username", username);

        // Add intern details to the ModelAndView
        mv.addObject("guide", guide);

        return mv;
    }

    //Intern Groups
    @GetMapping("/intern_groups")
    public ModelAndView internGroups(HttpSession session, Model model) {

        ModelAndView mv = new ModelAndView("/guide/intern_groups");
        Guide guide = getSignedInGuide();
        List<GroupEntity> internGroups = guideService.getInternGroups(guide);
        List<Intern> interns = internService.getInterns();
        mv.addObject("internGroups", internGroups);
        mv.addObject("intern", interns);
        mv.addObject("guide", getSignedInGuide());
        return mv;
    }

    @GetMapping("/intern_groups/{id}")
    public ModelAndView internGroups(@PathVariable("id") String id) {

        ModelAndView mv = new ModelAndView("/guide/intern_groups_detail");
        Guide guide = getSignedInGuide();
        List<GroupEntity> internGroups = guideService.getInternGroups(guide);
        mv.addObject("internGroups", internGroups);
        return mv;

    }

    @GetMapping("/intern/{id}")
    public ModelAndView internDetails(@PathVariable("id") String id) {
        ModelAndView mv = new ModelAndView();
        Optional<Intern> intern = internService.getIntern(id);
        mv.addObject("intern", intern);
        mv.setViewName("guide/intern_detail");
        return mv;
    }

    @GetMapping("/update_guide/{id}")
    public ModelAndView updateGuide(@PathVariable("id") long id) {
        ModelAndView mv = new ModelAndView("admin/update_guide");
        Optional<Guide> guide = guideService.getGuide(id);
        mv.addObject("guide", guide.orElse(new Guide()));
        return mv;
    }

    @PostMapping("/update_guide/{id}")
    public String updateGuide(@ModelAttribute("guide") Guide guide, @PathVariable("id") long id) {
        Optional<Guide> existingGuide = guideService.getGuide(guide.getGuideId());

        if (existingGuide.isPresent()) {

            String currentPassword = existingGuide.get().getPassword();
            Guide updatedGuide = existingGuide.get();
            updatedGuide.setName(guide.getName());
            updatedGuide.setLocation(guide.getLocation());
            updatedGuide.setFloor(guide.getFloor());
            updatedGuide.setLabNo(guide.getLabNo());
            updatedGuide.setContactNo(guide.getContactNo());
            updatedGuide.setEmailId(guide.getEmailId());

            // Save the updated admin entity
            guideService.updateGuide(updatedGuide, existingGuide);

        }
        return "redirect:/logout";
    }

    @GetMapping("/guide_pending_def_approvals")
    public ModelAndView pendingFromGuide(HttpSession session, Model model) {
        ModelAndView mv = new ModelAndView("/guide/guide_pending_def_approvals");
        Guide guide = getSignedInGuide();
        List<GroupEntity> groups = groupService.getGPendingGroups(guide);
        mv.addObject("groups", groups);
        mv.addObject("guide", guide);
        return mv;
    }

    @PostMapping("/guide_pending_def_approvals/ans")
    public String pendingFromGuide(@RequestParam("gpendingAns") String gpendingAns, @RequestParam("groupId") String groupId) {

        GroupEntity group = groupService.getGroup(groupId);
        if (gpendingAns.equals("approve")) {
            group.setProjectDefinitionStatus("gapproved");
        } else {
            group.setProjectDefinitionStatus("pending");
        }
        groupRepo.save(group);
        return "redirect:/bisag/guide/guide_pending_def_approvals";
    }


    //===================guide side approve definitions=========================================///
//    @PostMapping("/guide_pending_def_approvals/{internId}")
//    public ResponseEntity<Map<String, Object>> approveProjectDefinition(@PathVariable String internId) {
//        Map<String, Object> response = new HashMap<>();
//
//        Optional<Intern> internOptional = internRepo.findById(internId);
//        if (internOptional.isPresent()) {
//            Intern intern = internOptional.get();
//            intern.setProjectDefinitionName("Approved"); // Change status to Approved
//            internRepo.save(intern);
//
//            response.put("success", true);
//            response.put("message", "Project Definition Approved!");
//            return ResponseEntity.ok(response);
//        } else {
//            response.put("success", false);
//            response.put("message", "Intern not found");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//    }

    @GetMapping("/admin_pending_def_approvals")
    public ModelAndView pendingFromAdmin() {
        ModelAndView mv = new ModelAndView();
        List<GroupEntity> groups = groupService.getAPendingGroups();
        mv.addObject("groups", groups);
        return mv;
    }

    @GetMapping("/weekly_report")
    public ModelAndView weeklyReport() {
        ModelAndView mv = new ModelAndView("/guide/weekly_report");
        Guide guide = getSignedInGuide();
        List<GroupEntity> groups = guideService.getInternGroups(guide);
        List<WeeklyReport> reports = weeklyReportService.getReportsByGuideId(guide.getGuideId());
        mv.addObject("groups", groups);
        mv.addObject("reports", reports);
        mv.addObject("guide", getSignedInGuide());
        return mv;
    }

    @GetMapping("/guide_weekly_report_details/{groupId}/{weekNo}")
    public ModelAndView chanegWeeklyReportSubmission(@PathVariable("groupId") String groupId, @PathVariable("weekNo") int weekNo) {
        ModelAndView mv = new ModelAndView("/guide/guide_weekly_report_details");
        Guide guide = getSignedInGuide();
        GroupEntity group = groupService.getGroup(groupId);
        WeeklyReport report = weeklyReportService.getReportByWeekNoAndGroupId(weekNo, group);
        MyUser user = myUserService.getUserByUsername(getSignedInGuide().getEmailId());
        if (user.getRole().equals("GUIDE")) {

            String name = guide.getName();
            mv.addObject("replacedBy", name);

        } else if (user.getRole().equals("INTERN")) {
            Intern intern = internService.getInternByUsername(user.getUsername());
            mv.addObject("replacedBy", intern.getFirstName() + intern.getLastName());
        } else {
        }
        mv.addObject("report", report);
        mv.addObject("group", group);

        return mv;
    }

    @PostMapping("/guide_weekly_report_details/{groupid}/changed_report")
    public String chanegWeeklyReportSubmission(@PathVariable("groupid") String groupId, @RequestParam("weekNo") int weekNo, MultipartHttpServletRequest req) throws IllegalStateException, IOException, Exception {
        GroupEntity group = groupService.getGroup(groupId);
        Guide guide = getSignedInGuide();
        WeeklyReport report = weeklyReportService.getReportByWeekNoAndGroupId(weekNo, group);
        CurrentWeekNo = weekNo;
//			report.setSubmittedPdf(changeWeeklyReport(req.getFile("weeklyReportSubmission"), group));
        MyUser user = myUserService.getUserByUsername(guide.getEmailId());
        report.setReplacedBy(user);
        Date currentDate = new Date();
        // Check if the deadline is greater than or equal to the reportSubmittedDate
        if (report.getDeadline().compareTo(currentDate) >= 0) {
            // If the deadline is greater than or equal to the reportSubmittedDate, set the status to "submitted"
            report.setStatus("submitted");
        } else {
            // If the deadline is less than the reportSubmittedDate, set the status to "late submitted"
            report.setStatus("late submitted");
        }

        weeklyReportService.addReport(report);
        return "redirect:/bisag/guide/weekly_report";
    }

    @GetMapping("/guide_pending_final_reports")
    public ModelAndView guidePendingFinalReports(HttpSession session, Model model) {
        ModelAndView mv = new ModelAndView("/guide/guide_pending_final_reports");
        Guide guide = getSignedInGuide();
        List<GroupEntity> groups = groupService.getGPendingFinalReports(guide);
        mv.addObject("groups", groups);
        mv.addObject("guide", getSignedInGuide());
        return mv;
    }

    @PostMapping("/guide_pending_final_reports/ans")
    public String guidePendingFinalReports(@RequestParam("gpendingAns") String gpendingAns, @RequestParam("groupId") String groupId) {

        GroupEntity group = groupService.getGroup(groupId);
        if (gpendingAns.equals("approve")) {
            group.setFinalReportStatus("gapproved");
        } else {
            group.setFinalReportStatus("pending");
        }
        groupRepo.save(group);
        return "redirect:/bisag/guide/guide_pending_final_reports";
    }
//		private String changeWeeklyReport(MultipartFile file, GroupEntity group) {
//
//			try {
//				File myDir = new File(weeklyReportSubmission + "/"+ group.getGroupId());
//				if(!myDir.exists())
//				{
//					myDir.mkdirs();
//				}
//				if(!file.isEmpty())
//				{
//					file.transferTo(Paths.get(myDir.getAbsolutePath(), group.getGroupId() + "_week_" + CurrentWeekNo + ".pdf"));
//					return group.getGroupId() + "_week_" + CurrentWeekNo + ".pdf";
//				}
//				else
//					return null;
//			}
//			catch (Exception e) {
//				e.printStackTrace();
//				return "redirect:/";
//			}
//		}

    @GetMapping("/query_to_admin")
    public ModelAndView queryToAdmin() {
        ModelAndView mv = new ModelAndView("/guide/query_to_admin");
        List<Admin> admins = adminService.getAdmin();
        List<Guide> guides = guideService.getGuide();
        Guide guide = getSignedInGuide();
        List<GroupEntity> groups = guideService.getInternGroups(guide);
        mv.addObject("admins", admins);
        mv.addObject("guides", guides);
        mv.addObject("groups", groups);
        mv.addObject("guide", getSignedInGuide());
        return mv;
    }

    @PostMapping("/change_password")
    public String changePassword(@RequestParam("newPassword") String newPassword) {
        Guide guide = getSignedInGuide();
        guideService.changePassword(guide, newPassword);
        return "redirect:/logout";
    }


//============================approve definition by  guide and provided by admin=============================================//

    @GetMapping("/approveDefinition")
    public String showApproveDefinitionForm(Model model) {
        // Fetch all groups where project definition status is "gpending"
        List<GroupEntity> pendingGroups = groupRepo.findByProjectDefinitionStatus("gpending");

        if (pendingGroups.isEmpty()) {
            model.addAttribute("error", "No pending project definitions found!");
        } else {
            model.addAttribute("groups", pendingGroups);
        }

        return "guide/guide_pending_def_approvals"; // Thymeleaf template
    }

    @PostMapping("/approveDefinition")
    public ResponseEntity<String> approveProjectDefinition(@RequestParam String groupId,
                                                           @RequestParam String status) {
        List<Guide> guides = guideRepo.findByGroupId(groupId);
        if (!guides.isEmpty()) {
            for (Guide guide : guides) {
                guide.setDefinitionStatus(status);
                guideRepo.save(guide);
            }

            GroupEntity group = groupRepo.getByGroupId(groupId);
            if (group != null) {
                if ("Approved".equalsIgnoreCase(status)) {
                    group.setProjectDefinitionStatus("Approved");
                } else {
                    group.setProjectDefinitionStatus("Rejected");
                }
                groupRepo.save(group);
            }

            return ResponseEntity.ok("Project Definition " + status);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Guide not found.");
    }

    //============================ end approve definition by  guide and provided by admin=============================================//


    //============================ create definition by  guide and sent to the admin=============================================//


    @GetMapping("/create_project_def")
    public String showAssignProjectDefinitionForm(Model model) {
        Guide guide = getSignedInGuide();
        List<GroupEntity> groups = guideService.getInternGroups(guide);
        model.addAttribute("groups", groups);
        return "guide/create_project_def";

}

//    @PostMapping("/create_project_def")
//    public ResponseEntity<String> submitProjectDefinition(@RequestParam String groupId,
//                                                          @RequestParam String description,
//                                                          @RequestParam String projectDefinition) {
//        // Fetch the group based on groupId
//        GroupEntity group = groupRepo.getByGroupId(groupId);
//
//        if (group == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found.");
//        }
//
//        // Set project details and send to Admin for approval
//        group.setProjectDefinition(projectDefinition);
//        group.setDescription(description);
//        group.setProjectDefinitionStatus("pending"); // Waiting for Admin Approval
//        groupRepo.save(group);
//
//        return ResponseEntity.ok("Project Definition Submitted. Waiting for Admin Approval.");
//    }

    @PostMapping("/create_project_def")
    public String submitProjectDefinition(@RequestParam String groupId,
                                          @RequestParam String description,
                                          @RequestParam String projectDefinition,
                                          Model model) {
        GroupEntity group = groupRepo.getByGroupId(groupId);

        if (group == null) {
            model.addAttribute("success", "❌ Group not found.");
            return "guide/create_project_def"; // Stay on the same page
        }

        group.setProjectDefinition(projectDefinition);
        group.setDescription(description);
        group.setProjectDefinitionStatus("pending");
        groupRepo.save(group);

//        model.addAttribute("success", "✅ Project Definition Submitted. Waiting for Admin Approval.");
//        return "redirect:/bisag/guide/guide_dashboard"; // Stay on the same page with alert box
        return "guide/create_project_def";
    }

    //============================ end create definition by  guide and sent to the admin=============================================//


    @GetMapping("/view_weekly_reports/{weekNo}")
    public ModelAndView chanegWeeklyReportSubmission(@PathVariable("weekNo") int weekNo) {
        ModelAndView mv = new ModelAndView("intern/change_weekly_report");
        Intern inetrn = getSignedInIntern();
        GroupEntity group = inetrn.getGroup();
        WeeklyReport report = weeklyReportService.getReportByWeekNoAndGroupId(weekNo, group);
        MyUser user = myUserService.getUserByUsername(report.getReplacedBy().getUsername());
        if (user.getRole().equals("GUIDE")) {
            Guide guide = guideService.getGuideByUsername(user.getUsername());
            String status = "Your Current Weekly report is required some modifications given by guide. Please check it out.";
            mv.addObject("status", status);
            mv.addObject("replacedBy", guide.getName());
        } else if (user.getRole().equals("INTERN")) {
            Intern intern = internService.getInternByUsername(user.getUsername());
            mv.addObject("replacedBy", intern.getFirstName() + " " + intern.getLastName());
            mv.addObject("status",
                    "Your current weekly report is accepted and if any changes are required then you will be notified.");
        }
        mv.addObject("report", report);
        mv.addObject("group", group);
        return mv;
    }


    public Intern getSignedInIntern() {
        String username = (String) session.getAttribute("username");
        Intern intern = internService.getInternByUsername(username);
        if (intern.getIsActive()) {
            return intern;
        } else {
            return null;
        }
    }

    @GetMapping("/viewPdf/{internId}/{weekNo}")
    public ResponseEntity<byte[]> viewPdf(@PathVariable String internId, @PathVariable int weekNo) {
        WeeklyReport report = weeklyReportService.getReportByInternIdAndWeekNo(internId, weekNo);
        byte[] pdfContent = report.getSubmittedPdf();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }



    @GetMapping("/viewProjectDefinition/{groupId}")
    public ResponseEntity<byte[]> viewProjectDefinition(@PathVariable String groupId) {
        GroupEntity group = groupService.getGroupByGroupId(groupId);

        if (group == null || group.getProjectDefinitionDocument() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        byte[] pdfContent = group.getProjectDefinitionDocument();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }


}