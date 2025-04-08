package com.rh4.controllers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.rh4.entities.*;
import com.rh4.repositories.GuideRepo;
import com.rh4.repositories.LeaveApplicationRepo;
import com.rh4.repositories.TaskAssignmentRepo;
import com.rh4.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.rh4.repositories.GroupRepo;

import jakarta.servlet.http.HttpSession;

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
    private MyUserService myUserService;
    @Autowired
    private LogService logService;
    @Autowired
    private GuideRepo guideRepo;
    @Autowired
    private LeaveApplicationRepo leaveApplicationRepo;
    @Autowired
    private MessageService messageService;
    @Autowired
    private TaskAssignmentRepo taskAssignmentRepo;
    @Autowired
    private TaskAssignmentService taskAssignmentService;
    @Autowired
    private FieldService fieldService;
    Intern internFromUploadFileMethod;
    int CurrentWeekNo;
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private InternApplicationService internApplicationService;

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
        Guide guide = getSignedInGuide();
        ModelAndView mv;

        // Check if it's the guide's first login
        Guide guideApplication = guideService.getGuideByUsername(guide.getEmailId());
        if (guideApplication != null && guideApplication.getFirstLogin() == 1) {
            mv = new ModelAndView("guide/change_passwordd");
            mv.addObject("forcePasswordChange", true);
        } else {
            mv = new ModelAndView("guide/guide_dashboard");
            long gPendingCount = groupService.countGPendingGroups();
            mv.addObject("gPendingCount", gPendingCount);
            mv.addObject("username", getUsername());
            mv.addObject("guide", guide);
            logService.saveLog(String.valueOf(guide.getGuideId()), "Guide Accessed Dashboard", "Guide " + guide.getName() + " visited their dashboard.");
        }

        session.setAttribute("id", guide.getGuideId());
        session.setAttribute("username", getUsername());

        return mv;
    }


    //Intern Groups
    @GetMapping("/intern_groups")
    public ModelAndView internGroups(HttpSession session, Model model) {
        ModelAndView mv = new ModelAndView("/guide/intern_groups");


        Guide guide = getSignedInGuide();
        String id = String.valueOf(guide.getGuideId());

        logService.saveLog(id, "Viewed Intern Groups", "Guide: " + guide.getName() + " accessed the intern groups list.");

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
        String guideId = String.valueOf(guide.getGuideId());

        logService.saveLog(guideId, "Viewed Intern Groups Detail", "Guide: " + guide.getName() + " accessed the intern groups detail.");

        List<GroupEntity> internGroups = guideService.getInternGroups(guide);
        mv.addObject("internGroups", internGroups);
        return mv;
    }

    @GetMapping("/intern/{id}")
    public ModelAndView internDetails(@PathVariable("id") String id) {
        ModelAndView mv = new ModelAndView();

        Guide guide = getSignedInGuide();
        String guideId = String.valueOf(guide.getGuideId());

        logService.saveLog(guideId, "Viewed Intern Details", "Guide: " + guide.getName() + " accessed details for intern with ID: " + id);

        Optional<Intern> intern = internService.getIntern(id);
        mv.addObject("intern", intern);
        mv.setViewName("guide/intern_detail");
        return mv;
    }

    @GetMapping("/update_guide/{id}")
    public ModelAndView updateGuide(@PathVariable("id") long id) {
        ModelAndView mv = new ModelAndView("admin/update_guide");

        Guide guide = getSignedInGuide();
        String guideId = String.valueOf(guide.getGuideId());

        logService.saveLog(guideId, "Updated Guide Details", "Guide: " + guide.getName() + " accessed the guide update page for guide with ID: " + id);

        Optional<Guide> guideDetails = guideService.getGuide(id);
        mv.addObject("guide", guideDetails.orElse(new Guide()));
        return mv;
    }

    @PostMapping("/update_guide/{id}")
    public String updateGuide(@ModelAttribute("guide") Guide guide, @PathVariable("id") long id) {
        Optional<Guide> existingGuide = guideService.getGuide(guide.getGuideId());

        if (existingGuide.isPresent()) {
            Guide updatedGuide = existingGuide.get();
            String guideName = updatedGuide.getName();

            updatedGuide.setName(guide.getName());
            updatedGuide.setLocation(guide.getLocation());
            updatedGuide.setFloor(guide.getFloor());
            updatedGuide.setLabNo(guide.getLabNo());
            updatedGuide.setContactNo(guide.getContactNo());
            updatedGuide.setEmailId(guide.getEmailId());

            guideService.updateGuide(updatedGuide, existingGuide);

            logService.saveLog(String.valueOf(updatedGuide.getGuideId()), "Guide Profile Updated",
                    "Guide " + guideName + " updated their profile.");
        }

        return "redirect:/logout";
    }

    @GetMapping("/guide_pending_def_approvals")
    public ModelAndView pendingFromGuide(HttpSession session, Model model) {
        ModelAndView mv = new ModelAndView("/guide/guide_pending_def_approvals");

        Guide guide = getSignedInGuide();
        String guideId = String.valueOf(guide.getGuideId());

        logService.saveLog(guideId, "Viewed Pending Group Approvals", "Guide: " + guide.getName() + " accessed the pending group approvals page.");

        List<GroupEntity> groups = groupService.getGPendingGroups(guide);
        mv.addObject("groups", groups);
        mv.addObject("guide", guide);
        return mv;
    }

    @PostMapping("/guide_pending_def_approvals/ans")
    public String pendingFromGuide(@RequestParam("gpendingAns") String gpendingAns, @RequestParam("groupId") String groupId) {

        GroupEntity group = groupService.getGroup(groupId);
        Guide guide = group.getGuide();
        String guideId = String.valueOf(guide.getGuideId());
        String guideName = guide.getName();

        if (gpendingAns.equals("approve")) {
            group.setProjectDefinitionStatus("gapproved");
            logService.saveLog(guideId, "Project Definition Approved",
                    "Guide " + guideName + " approved a project definition for Group ID: " + groupId);
        } else {
            group.setProjectDefinitionStatus("pending");
            logService.saveLog(guideId, "Project Definition Pending",
                    "Guide " + guideName + " set project definition to pending for Group ID: " + groupId);
        }

        groupRepo.save(group);
        return "redirect:/bisag/guide/guide_pending_def_approvals";
    }

    @GetMapping("/admin_pending_def_approvals")
    public ModelAndView pendingFromAdmin() {
        ModelAndView mv = new ModelAndView();

        Guide guide = getSignedInGuide();
        String guideId = String.valueOf(guide.getGuideId());

        logService.saveLog(guideId, "Viewed Pending Groups for Admin Approval", "Guide: " + guide.getName() + " accessed the pending groups for admin approval.");

        List<GroupEntity> groups = groupService.getAPendingGroups();
        mv.addObject("groups", groups);
        return mv;
    }

    @GetMapping("/weekly_report")
    public ModelAndView weeklyReport() {
        ModelAndView mv = new ModelAndView("/guide/weekly_report");

        Guide guide = getSignedInGuide();
        String guideId = String.valueOf(guide.getGuideId());

        logService.saveLog(guideId, "Viewed Weekly Reports", "Guide: " + guide.getName() + " accessed the weekly reports page.");

        List<GroupEntity> groups = guideService.getInternGroups(guide);
        List<WeeklyReport> reports = weeklyReportService.getReportsByGuideId(guide.getGuideId());
        mv.addObject("groups", groups);
        mv.addObject("reports", reports);
        mv.addObject("guide", getSignedInGuide());
        return mv;
    }

    @GetMapping("/guide_weekly_report_details/{groupId}/{weekNo}")
    public ModelAndView changeWeeklyReportSubmission(@PathVariable("groupId") String groupId, @PathVariable("weekNo") int weekNo) {
        ModelAndView mv = new ModelAndView("/guide/guide_weekly_report_details");

        Guide guide = getSignedInGuide();
        String guideId = String.valueOf(guide.getGuideId());

        logService.saveLog(guideId, "Viewed Weekly Report Details", "Guide: " + guide.getName() + " accessed weekly report details for group ID: " + groupId + " and week number: " + weekNo);

        GroupEntity group = groupService.getGroup(groupId);
        WeeklyReport report = weeklyReportService.getReportByWeekNoAndGroupId(weekNo, group);
        MyUser user = myUserService.getUserByUsername(getSignedInGuide().getEmailId());
        if (user.getRole().equals("GUIDE")) {
            String name = guide.getName();
            mv.addObject("replacedBy", name);
        } else if (user.getRole().equals("INTERN")) {
            Intern intern = internService.getInternByUsername(user.getUsername());
            mv.addObject("replacedBy", intern.getFirstName() + intern.getLastName());
        }
        mv.addObject("report", report);
        mv.addObject("group", group);

        return mv;
    }

    @PostMapping("/guide_weekly_report_details/{groupid}/changed_report")
    public String chanegWeeklyReportSubmission(@PathVariable("groupid") String groupId,
                                               @RequestParam("weekNo") int weekNo,
                                               MultipartHttpServletRequest req)
            throws IllegalStateException, IOException, Exception {

        GroupEntity group = groupService.getGroup(groupId);
        Guide guide = getSignedInGuide();
        WeeklyReport report = weeklyReportService.getReportByWeekNoAndGroupId(weekNo, group);
        CurrentWeekNo = weekNo;

        MyUser user = myUserService.getUserByUsername(guide.getEmailId());
        report.setReplacedBy(user);
        Date currentDate = new Date();

        // Check if the deadline is greater than or equal to the reportSubmittedDate
        if (report.getDeadline().compareTo(currentDate) >= 0) {
            report.setStatus("submitted");
            logService.saveLog(String.valueOf(guide.getGuideId()), "Weekly Report Submitted",
                    "Guide " + guide.getName() + " submitted a weekly report for Group ID: " + groupId + ", Week No: " + weekNo);
        } else {
            report.setStatus("late submitted");
            logService.saveLog(String.valueOf(guide.getGuideId()), "Weekly Report Late Submitted",
                    "Guide " + guide.getName() + " submitted a late weekly report for Group ID: " + groupId + ", Week No: " + weekNo);
        }

        weeklyReportService.addReport(report);
        return "redirect:/bisag/guide/weekly_report";
    }

    @GetMapping("/guide_pending_final_reports")
    public ModelAndView guidePendingFinalReports(HttpSession session, Model model) {
        ModelAndView mv = new ModelAndView("/guide/guide_pending_final_reports");

        Guide guide = getSignedInGuide();
        String guideId = String.valueOf(guide.getGuideId());

        logService.saveLog(guideId, "Viewed Pending Final Reports", "Guide: " + guide.getName() + " accessed the pending final reports list.");

        List<GroupEntity> groups = groupService.getGPendingFinalReports(guide);
        mv.addObject("groups", groups);
        mv.addObject("guide", getSignedInGuide());

        return mv;
    }

    @PostMapping("/guide_pending_final_reports/ans")
    public String guidePendingFinalReports(@RequestParam("gpendingAns") String gpendingAns,
                                           @RequestParam("groupId") String groupId) {

        GroupEntity group = groupService.getGroup(groupId);
        Guide guide = getSignedInGuide();

        if (gpendingAns.equals("approve")) {
            group.setFinalReportStatus("approved");
            logService.saveLog(String.valueOf(guide.getGuideId()), "Final Report Approved",
                    "Guide " + guide.getName() + " approved the final report for Group ID: " + groupId);
        } else {
            group.setFinalReportStatus("pending");
            logService.saveLog(String.valueOf(guide.getGuideId()), "Final Report Approval Pending",
                    "Guide " + guide.getName() + " marked the final report as pending for Group ID: " + groupId);
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
        List<Intern> interns = internService.getInterns();
        List<Guide> guides = guideService.getGuide();
        Guide guide = getSignedInGuide();
        List<GroupEntity> groups = guideService.getInternGroups(guide);
        mv.addObject("admins", admins);
        mv.addObject("interns", interns);
        mv.addObject("guides", guides);
        mv.addObject("groups", groups);
        mv.addObject("guide", getSignedInGuide());
        return mv;
    }

    @GetMapping("/change_passwordd")
    public ModelAndView changePasswordPage(HttpSession session) {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            return new ModelAndView("redirect:/bisag/guide/login");
        }

        ModelAndView mv = new ModelAndView("guide/change_passwordd");
        mv.addObject("username", username);
        mv.addObject("forcePasswordChange", true); // This will trigger the auto-show script in HTML
        return mv;
    }

    @PostMapping("/change_password")
    public String changePassword(@RequestParam("newPassword") String newPassword) {
        Guide guide = getSignedInGuide(); // Get the currently signed-in guide

        guideService.changePassword(guide, newPassword);

        logService.saveLog(String.valueOf(guide.getGuideId()), "Password Changed",
                "Guide " + guide.getName() + " changed their password.");

        return "redirect:/logout";
    }

    @PostMapping("/change_passwordd")
    public String changePasswordd(@RequestParam("newPassword") String newPassword) {
        Guide guide = getSignedInGuide(); // Get currently logged-in guide

        // Update password
        guideService.changePassword(guide, newPassword);

        // Fetch guide entity using guideId or email
        Guide guideEntity = guideService.getGuideByUsername(guide.getEmailId());
        if (guideEntity != null) {
            System.out.println("Before update, firstLogin: " + guideEntity.getFirstLogin());

            // Update firstLogin field
            guideEntity.setFirstLogin(0); // Mark first login as complete
            guideService.updateGuidee(guideEntity); // Save changes

            System.out.println("After update, firstLogin: " + guideEntity.getFirstLogin());
        } else {
            System.out.println("Guide entity not found for email: " + guide.getEmailId());
        }

        // Log the password change
        logService.saveLog(String.valueOf(guide.getGuideId()), "First Password Changed", "Password changed successfully for first time.");

        // Redirect to logout
        return "redirect:/logout";
    }

    // Fetch pending leaves for guide view
    @GetMapping("/pending_leaves")
    public String viewPendingLeaves(Model model) {
        Guide guide = getSignedInGuide();

        List<LeaveApplication> pendingLeaves = leaveApplicationRepo.findByStatus("Pending");
        model.addAttribute("pendingLeaves", pendingLeaves);

        logService.saveLog(String.valueOf(guide.getGuideId()), "Viewed Pending Leaves",
                "Guide " + guide.getName() + " viewed the list of pending leave applications.");

        return "guide/pending_leaves";
    }

    // Approve leave request
    @PostMapping("/approve_leave/{id}")
    public String approveLeave(@PathVariable Long id) {
        Guide guide = getSignedInGuide();

        Optional<LeaveApplication> optionalLeave = leaveApplicationRepo.findById(id);
        if (optionalLeave.isPresent()) {
            LeaveApplication leave = optionalLeave.get();
            leave.setGuideApproval(true);

            if (leave.isAdminApproval() && leave.isGuideApproval()) {
                leave.setStatus("Approved");
            }

            leaveApplicationRepo.save(leave);

            logService.saveLog(String.valueOf(guide.getGuideId()), "Approved Leave Request",
                    "Guide " + guide.getName() + " approved leave request for Intern ID: " + leave.getInternId());
        }
        return "redirect:/bisag/guide/pending_leaves";
    }

    // Reject leave request
    @PostMapping("/reject_leave/{id}")
    public String rejectLeave(@PathVariable Long id, @RequestParam("remarks") String remarks) {
        Optional<LeaveApplication> optionalLeave = leaveApplicationRepo.findById(id);
        if (optionalLeave.isPresent()) {
            LeaveApplication leave = optionalLeave.get();
            leave.setStatus("Rejected");
            leave.setAdminApproval(false);
            leave.setGuideApproval(false);
            leave.setRemarks(remarks);
            leaveApplicationRepo.save(leave);

            Guide guide = getSignedInGuide();
            if (guide != null) {
                String guideId = String.valueOf(guide.getGuideId());
                logService.saveLog(guideId, "Rejected Leave Application",
                        "Guide " + guide.getName() + " rejected leave application for intern ID: " + leave.getInternId() + ". Remarks: " + remarks);
            }
        }
        return "redirect:/bisag/guide/pending_leaves";
    }

    // View Leave Details
    @GetMapping("/leave_details/{id}")
    public String viewLeaveDetails(@PathVariable Long id, Model model) {
        LeaveApplication leave = leaveApplicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave Application Not Found"));
        model.addAttribute("leave", leave);

        Guide guide = getSignedInGuide();
        if (guide != null) {
            String guideId = String.valueOf(guide.getGuideId());
            logService.saveLog(guideId, "Viewed Leave Details",
                    "Guide " + guide.getName() + " viewed details of leave application ID: " + id);
        }

        return "guide/leave_details";
    }

    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-Messaging Module_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    // Guide sends a message
    @PostMapping("/chat/send")
    public ResponseEntity<Message> sendMessageAsGuide(
            @RequestParam String senderId,
            @RequestParam String receiverId,
            @RequestParam String messageText) {

        Optional<Guide> guide = guideService.findById(Long.valueOf(senderId));
        if (guide.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Message message = messageService.sendMessage(String.valueOf(guide.get().getGuideId()), receiverId, messageText);

        Guide senderGuide = guide.get();
        logService.saveLog(senderId, "Sent Message",
                "Guide " + senderGuide.getName() + " sent a message to Intern ID: " + receiverId + ". Message: " + messageText);

        return ResponseEntity.ok(message);
    }

    @GetMapping("/chat/history")
    public ResponseEntity<List<Message>> getChatHistoryAsGuide(
            @RequestParam Long senderId,
            @RequestParam String receiverId) {

        Optional<Guide> guide = guideService.findById(senderId);
        if (guide.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String guideId = String.valueOf(guide.get().getGuideId());

        List<Message> messages = messageService.getChatHistory(guideId, receiverId);
        messages.addAll(messageService.getChatHistory(receiverId, guideId));

        messages.sort(Comparator.comparing(Message::getTimestamp));

        Guide senderGuide = guide.get();
        logService.saveLog(guideId, "Viewed Chat History",
                "Guide " + senderGuide.getName() + " viewed chat history with ID: " + receiverId);

        return ResponseEntity.ok(messages);
    }

    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-Task Assignment Module_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    @GetMapping("/tasks_assignments")
    public String viewTaskAssignmentsPage(Model model) {

        List<TaskAssignment> tasks = taskAssignmentService.getAllTasks();
        List<Intern> interns = internService.getAllInterns();

        model.addAttribute("interns", interns);
        model.addAttribute("tasks", tasks);

        Guide guide = getSignedInGuide();
        if (guide != null) {
            String guideId = String.valueOf(guide.getGuideId());
            logService.saveLog(guideId, "Viewed Task Assignments",
                    "Guide " + guide.getName() + " viewed the task assignments page.");
        }

        return "guide/task_assignments";
    }

    // Assign a New Task
    @PostMapping("/tasks/assign")
    public String assignTask(
            @RequestParam("intern") String intern,
            @RequestParam("assignedById") String assignedById,
            @RequestParam("assignedByRole") String assignedByRole,
            @RequestParam("taskDescription") String taskDescription,
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr) {

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);

            Optional<Intern> optionalIntern = internService.getIntern(intern);
            if (optionalIntern.isPresent()) {
                TaskAssignment task = new TaskAssignment();
                task.setIntern(intern);
                task.setAssignedById(assignedById);
                task.setAssignedByRole(assignedByRole);
                task.setTaskDescription(taskDescription);
                task.setStartDate(startDate);
                task.setEndDate(endDate);
                task.setStatus("Pending");
                task.setApproved(false);

                taskAssignmentService.saveTask(task);

                logService.saveLog(assignedById, "Assigned Task",
                        assignedByRole + " assigned a new task to Intern ID: " + intern);
            }
        } catch (ParseException e) {
            // Handle exception silently
        }

        return "redirect:/bisag/guide/tasks_assignments";
    }

    // View Task Details ID-wise
    @GetMapping("/task_details/{id}")
    public String viewTaskDetails(@PathVariable Long id, Model model) {
        TaskAssignment tasks = taskAssignmentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Task Assignment Not Found"));
        model.addAttribute("tasks", tasks);

        Guide guide = getSignedInGuide();
        if (guide != null) {
            String guideId = String.valueOf(guide.getGuideId());
            logService.saveLog(guideId, "Viewed Task Details",
                    "Guide " + guide.getName() + " viewed details of Task Assignment ID: " + id);
        }

        return "guide/task_details";
    }

    // Get Tasks Assigned by Admin/Guide
    @GetMapping("/tasks/assignedBy/{assignedById}")
    public ResponseEntity<List<TaskAssignment>> getTasksAssignedBy(@PathVariable("assignedById") String assignedById) {
        List<TaskAssignment> tasks = taskAssignmentService.getTasksAssignedBy(assignedById);

        logService.saveLog(assignedById, "Viewed Assigned Tasks",
                "User with ID " + assignedById + " viewed tasks assigned by them.");

        return ResponseEntity.ok(tasks);
    }

    // Approve Task Completion
    @PostMapping("/tasks/approve/{taskId}")
    public ResponseEntity<String> approveTask(@PathVariable("taskId") Long taskId) {
        Optional<TaskAssignment> optionalTask = taskAssignmentService.getTaskById(taskId);

        if (optionalTask.isPresent()) {
            TaskAssignment task = optionalTask.get();
            task.setApproved(true);
            task.setStatus("Completed");

            taskAssignmentService.saveTask(task);

            logService.saveLog(task.getAssignedById(), "Approved Task",
                    "User with ID " + task.getAssignedById() + " approved Task ID: " + taskId);

            return ResponseEntity.ok("Task approved successfully.");
        }
        return ResponseEntity.badRequest().body("Task not found.");
    }

    @GetMapping("/tasks/proof/{taskId}")
    public ResponseEntity<Resource> getProofAttachment(@PathVariable Long taskId) {
        Optional<TaskAssignment> taskOpt = taskAssignmentRepo.findById(taskId);

        if (taskOpt.isEmpty() || taskOpt.get().getProofAttachment() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        try {

            String fileName = taskOpt.get().getProofAttachment();
            Path filePath = Paths.get("uploads/task_proofs/", fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            logService.saveLog(taskOpt.get().getAssignedById(), "Viewed Task Proof",
                    "Guide with ID " + taskOpt.get().getAssignedById() + " viewed proof for Task ID: " + taskId);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/update-task/{id}")
    public ResponseEntity<Map<String, Object>> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, String> taskData) {
        try {
            String newStatus = taskData.get("status");
            if (newStatus == null || newStatus.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Status cannot be empty"));
            }

            Optional<TaskAssignment> optionalTask = taskAssignmentService.getTaskById(id);
            if (optionalTask.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Task not found"));
            }

            TaskAssignment task = optionalTask.get();
            String oldStatus = task.getStatus();
            task.setStatus(newStatus); // Updating only the status
            taskAssignmentService.saveTask(task);

            Guide guide = getSignedInGuide();
            if (guide != null) {
                String guideId = String.valueOf(guide.getGuideId());
                logService.saveLog(guideId, "Updated Task Status",
                        "Guide " + guide.getName() + " updated Task ID: " + id + " from '" + oldStatus + "' to '" + newStatus + "'.");
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An error occurred: " + e.getMessage()));
        }
    }


    //Shortlisting Interns
    @PostMapping("/intern_application/approved_intern/ans")
    public String approvedInterns(@RequestParam String message, @RequestParam long id, @RequestParam String finalStatus) {
        System.out.println("id: " + id + ", finalStatus: " + finalStatus);

        Optional<InternApplication> intern = internService.getInternApplication(id);
        intern.get().setFinalStatus(finalStatus);
        internService.addInternApplication(intern.get());

        if (finalStatus.equals("failed")) {

        } else {
            String finalMessage = message + "\n" + "Username: " + intern.get().getFirstName() +
                    intern.get().getLastName() + "\n Password: " + intern.get().getFirstName() + "_" + intern.get().getId();
        }

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Updated Final Status",
                    "Admin with ID: " + admin.getAdminId() + " updated final status of intern with ID: " + id + " to " + finalStatus);
        }

        return "redirect:/bisag/guide/intern_application/approved_interns";
    }

    @GetMapping("/approved_interns")
    public ModelAndView approvedInterns(Model model) {
        ModelAndView mv = new ModelAndView();

        // Get the logged-in guide's username from session
        String username = (String) session.getAttribute("username");
        Guide guide = guideService.getGuideByUsername(username);

        if (guide != null) {
            long guideId = guide.getGuideId();

            List<InternApplication> intern = internApplicationService.getApprovedInternsByGuideId(guideId);

            mv.addObject("intern", intern);
            session.setAttribute("id", guideId);

            logService.saveLog(String.valueOf(guideId),
                    "View Shortlisted Intern Applications",
                    "Guide " + guide.getName() + " accessed the shortlisted intern applications page.");
        } else {
            System.out.println("Error: Guide not found for logging!");
            mv.addObject("interns", List.of());
        }

        mv.setViewName("guide/approved_interns");
        return mv;
    }

    @GetMapping("/intern_application/{id}")
    public ModelAndView internApplication(@PathVariable("id") long id, Model model) {
        System.out.println("id" + id);
        ModelAndView mv = new ModelAndView();

        Optional<InternApplication> intern = internService.getInternApplication(id);
        List<Guide> guides = guideService.getGuide();

        Guide signedInGuide = getSignedInGuide();
        if (signedInGuide != null) {
            logService.saveLog(String.valueOf(signedInGuide.getGuideId()), "View Intern Application Details",
                    "Guide " + signedInGuide.getName() + " viewed the details of Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in guide not found for logging!");
        }

        mv.addObject("intern", intern);
        model.addAttribute("guides", guides);

        List<College> colleges = fieldService.getColleges();
        List<Domain> domains = fieldService.getDomains();
//        List<Branch> branches = fieldService.getBranches();
        mv.addObject("colleges", colleges);
        mv.addObject("domains", domains);
//        mv.addObject("branches", branches);

        mv.setViewName("guide/intern_application_detail");
        return mv;
    }



//    @PostMapping("/intern_application/ans")
//    public String internApplicationSubmission(@RequestParam String message, @RequestParam long id,
//                                              @RequestParam String status, @RequestParam String finalStatus) {
//        System.out.println("id: " + id + ", status: " + status);
//
//        Optional<InternApplication> intern = internService.getInternApplication(id);
//
//        if (intern.isPresent()) {
//            intern.get().setStatus(status);
//            intern.get().setFinalStatus(finalStatus);
//            internService.addInternApplication(intern.get());
//
//            logService.saveLog(String.valueOf(id), "Updated application status for intern", "Status Change");
//
//            if (status.equals("rejected")) {
//            } else {
//            }
//        }
//        return "redirect:/bisag/guide/approved_interns";
//    }


    @PostMapping("/intern_application/ans")
    public String internApplicationSubmission(@RequestParam long id, @RequestParam String status) {
        Optional<InternApplication> internOpt = internService.getInternApplication(id);

        if (internOpt.isPresent()) {
            InternApplication intern = internOpt.get();
            intern.setGuideStatus(status);

            // Only update finalStatus if admin has already approved
            if ("passed".equals(status) && "passed".equals(intern.getAdminStatus())) {
                intern.setFinalStatus("passed");
            } else {
                intern.setFinalStatus("pending");
            }

            internService.addInternApplication(intern);
            logService.saveLog(String.valueOf(id), "Guide approved/rejected intern", "Status Change");
        }

        return "redirect:/bisag/guide/approved_interns";
    }


    @GetMapping("/approveDefinition")
    public String showApproveDefinitionForm(Model model) {
        List<GroupEntity> pendingGroups = groupRepo.findByProjectDefinitionStatus("gpending");

        if (pendingGroups.isEmpty()) {
            model.addAttribute("error", "No pending project definitions found!");
        } else {
            model.addAttribute("groups", pendingGroups);
        }

        Guide guide = getSignedInGuide();
        if (guide != null) {
            logService.saveLog(String.valueOf(guide.getGuideId()), "Viewed Pending Project Definitions", "Guide " + guide.getName() + " accessed the pending project definitions page.");
        }

        return "guide/guide_pending_def_approvals";
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
                String oldStatus = group.getProjectDefinitionStatus();
                group.setProjectDefinitionStatus(status.equalsIgnoreCase("Approved") ? "Approved" : "Rejected");
                groupRepo.save(group);

                Guide guide = getSignedInGuide();
                if (guide != null) {
                    logService.saveLog(String.valueOf(guide.getGuideId()), "Updated Project Definition Status",
                            "Guide " + guide.getName() + " updated Group ID: " + groupId + " from '" + oldStatus + "' to '" + group.getProjectDefinitionStatus() + "'.");
                }
            }
            return ResponseEntity.ok("Project Definition " + status);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Guide not found.");
    }

    @GetMapping("/create_project_def")
    public String showAssignProjectDefinitionForm(Model model) {
        Guide guide = getSignedInGuide();
        List<GroupEntity> groups = guideService.getInternGroups(guide);
        model.addAttribute("groups", groups);

        if (guide != null) {
            logService.saveLog(String.valueOf(guide.getGuideId()), "Viewed Assign Project Definition Form",
                    "Guide " + guide.getName() + " accessed the assign project definition form.");
        }

        return "guide/create_project_def";
    }

    @PostMapping("/create_project_def")
    public String submitProjectDefinition(@RequestParam String groupId,
                                          @RequestParam String description,
                                          @RequestParam String projectDefinition,
                                          Model model) {
        GroupEntity group = groupRepo.getByGroupId(groupId);

        if (group == null) {
            model.addAttribute("success", "Group not found.");
            return "guide/create_project_def";
        }
        String oldStatus = group.getProjectDefinitionStatus();
        group.setProjectDefinition(projectDefinition);
        group.setDescription(description);
        group.setProjectDefinitionStatus("pending");
        groupRepo.save(group);

        Guide guide = getSignedInGuide();
        if (guide != null) {
            logService.saveLog(String.valueOf(guide.getGuideId()), "Submitted Project Definition",
                    "Guide " + guide.getName() + " updated Group ID: " + groupId + " from '" + oldStatus + "' to 'pending'.");
        }

        return "guide/create_project_def";
    }

    @GetMapping("/view_weekly_reports/{weekNo}")
    public ModelAndView chanegWeeklyReportSubmission(@PathVariable("weekNo") int weekNo) {
        Guide guide = getSignedInGuide();
        if (guide != null) {
            logService.saveLog(String.valueOf(guide.getGuideId()), "Viewed Weekly Report",
                    "Guide " + guide.getName() + " viewed Weekly Report for Week No: " + weekNo);
        }
        ModelAndView mv = new ModelAndView("intern/change_weekly_report");
        Intern inetrn = getSignedInIntern();
        GroupEntity group = inetrn.getGroup();
        WeeklyReport report = weeklyReportService.getReportByWeekNoAndGroupId(weekNo, group);
        MyUser user = myUserService.getUserByUsername(report.getReplacedBy().getUsername());
        if (user.getRole().equals("GUIDE")) {
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
        if (intern != null && intern.getIsActive()) {
            return intern;
        } else {
            return null;
        }
    }

    @GetMapping("/viewPdf/{internId}/{weekNo}")
    public ResponseEntity<byte[]> viewPdf(@PathVariable String internId, @PathVariable int weekNo) {
        Guide guide = getSignedInGuide();
        if (guide != null) {
            logService.saveLog(String.valueOf(guide.getGuideId()), "Viewed PDF",
                    "Guide " + guide.getName() + " viewed Weekly Report PDF for Intern ID: " + internId + ", Week No: " + weekNo);
        }
        WeeklyReport report = weeklyReportService.getReportByInternIdAndWeekNo(internId, weekNo);
        byte[] pdfContent = report.getSubmittedPdf();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }

    @GetMapping("/viewProjectDefinition/{groupId}")
    public ResponseEntity<byte[]> viewProjectDefinition(@PathVariable String groupId) {
        Guide guide = getSignedInGuide();
        if (guide != null) {
            logService.saveLog(String.valueOf(guide.getGuideId()), "Viewed Project Definition",
                    "Guide " + guide.getName() + " viewed Project Definition for Group ID: " + groupId);
        }
        GroupEntity group = groupService.getGroupByGroupId(groupId);

        if (group == null || group.getProjectDefinitionDocument() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        byte[] pdfContent = group.getProjectDefinitionDocument();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }

       
//    show the cancellations requests

    @GetMapping("/intern_request")
    public String viewCancellationRequests(Model model) {
        List<Intern> cancellationRequests = internService.getInternsByCancellationStatus("requested");
        model.addAttribute("cancellationRequests", cancellationRequests);
        return "guide/intern_request"; // your Thymeleaf template
    }

        
    @PostMapping("/handleCancellation")
    public String handleCancellation(@RequestParam String internId,
                                     @RequestParam String action) {
        Intern intern = internService.getInternById(internId);
    
        if ("approve".equalsIgnoreCase(action)) {
            intern.setCancellationStatus("gapproved");
            logService.saveLog(intern.getInternId(),
                    "Guide Approved Cancellation",
                    "Guide approved intern cancellation.");
        } else if ("reject".equalsIgnoreCase(action)) {
            intern.setCancellationStatus("grejected");
            logService.saveLog(intern.getInternId(),
                    "Guide Rejected Cancellation",
                    "Guide rejected intern cancellation.");
        }
        internService.updateCancellationStatus(intern);
        return "redirect:/bisag/guide/intern_request";
    }
    
}
