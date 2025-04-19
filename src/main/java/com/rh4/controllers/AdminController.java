package com.rh4.controllers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.rh4.repositories.*;
import com.rh4.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.rh4.models.ReportFilter;
import com.rh4.services.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bisag/admin")
public class AdminController {
    Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    HttpSession session;
    @Autowired
    AdminRepo adminRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private InternService internService;
    @Autowired
    private MyUserService myUserService;
    @Autowired
    private InternApplicationRepo internApplicationRepo;
    @Autowired
    private EmailSenderService emailService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private WeeklyReportService weeklyReportService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private GroupRepo groupRepo;
    @Autowired
    private CancelledRepo cancelledRepo;
    @Autowired
    private InternRepo internRepo;
    @Autowired
    private GuideService guideService;
    @Autowired
    private DataExportService dataExportService;
    @Autowired
    private ThesisService thesisService;
    @Autowired
    private LogService logService;
    @Autowired
    private InternApplicationService internApplicationService;
    @Autowired
    private VerificationService verificationService;
    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private ExcelService excelService;
    @Autowired
    private AttendanceRepo attendanceRepo;
//    @Autowired
    /// /    private RecordService recordService;
    @Autowired
    private LeaveApplicationService leaveApplicationService;
    @Autowired
    private LeaveApplicationRepo leaveApplicationRepo;
    @Autowired
    private AnnouncementRepo announcementRepo;
    @Autowired
    private UndertakingRepo undertakingRepo;
    @Autowired
    private ThesisStorageRepo thesisStorageRepo;
    @Autowired
    private MessageService messageService;
    @Autowired
    private TaskAssignmentService taskAssignmentService;
    @Autowired
    private TaskAssignmentRepo taskAssignmentRepo;
    @Autowired
    private FeedBackService feedbackService;
    @Autowired
    private GuideRepo guideRepo;
    @Autowired
    private AnnoucementService announcementService;
    @Autowired
    private MyUserService userService;


    @Value("${app.storage.base-dir}")
    private String baseDir;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private GroupEntity groupEntity;
    @Autowired
    private LogRepo logRepo;

    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public static String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public Admin adminName(HttpSession session) {
        String username = (String) session.getAttribute("username");
        return adminService.getAdminByUsername(username);
    }

    public Admin getSignedInAdmin() {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            System.out.println("No username found in session.");
            return null;
        }

        Admin admin = adminService.getAdminByUsername(username);

        if (admin == null) {
            System.out.println("No admin found for username: " + username);
        }

        return admin;
    }

    public String generateInternId() {
        // Generate custom internId using current year and serial number
        SimpleDateFormat yearFormat = new SimpleDateFormat("yy");
        String currentYear = yearFormat.format(new Date());

        // Assuming you have a method to get the next serial number
        int serialNumber = generateSerialNumber();
        ++serialNumber;
        // Combine the parts to form the custom internId
        String sno = String.valueOf(serialNumber);
        String formattedSerialNumber = String.format("%04d", Integer.parseInt(sno));
        System.out.println("serialNumber..." + serialNumber);
        System.out.println("formated..." + formattedSerialNumber);
        String internId = currentYear + "BISAG" + formattedSerialNumber;
        return internId;
    }

    public int generateSerialNumber() {

        String id = internService.getMostRecentInternId();
        if (id == null)
            return 0;
        String serialNumber = id.substring(id.length() - 4);
        int lastFourDigits = Integer.parseInt(serialNumber);
        return lastFourDigits;
    }

    // Method to generate the group ID
    public String generateGroupId() {
        // Check if the current year has changed
        int year = getCurrentYear();
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        String currentYear = yearFormat.format(new Date());
        int currentYearInt = Integer.parseInt(currentYear);
        int serialNumber = generateSerialNumberForGroup();
        if (year != currentYearInt) {
            currentYearInt = year; // Update the current year
            serialNumber = 0;   // Reset the serial number to 0
        }

        // Increment the serial number
        serialNumber++;

        // Format the serial number
        String formattedSerialNumber = String.format("%03d", serialNumber);

        // Combine the parts to form the custom group ID
        String groupId = currentYear + "G" + formattedSerialNumber;

        return groupId;
    }

    // Method to get the current year
    private int getCurrentYear() {
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        String yearString = yearFormat.format(new Date());
        return Integer.parseInt(yearString);
    }

    // Method to generate the serial number for group
    private int generateSerialNumberForGroup() {
        String id = groupService.getMostRecentGroupId();
        if (id == null || id.isEmpty()) {
            return 0;
        } else {
            // Extract the serial number part from the group ID
            String serialNumberString = id.substring(5); // Assuming "yyyyG" prefix
            return Integer.parseInt(serialNumberString);
        }
    }

    Model countNotifications(Model model) {
        // count

        //count approve for interview
        long approveForInterviewApplicationsCount = internService.approveForInterviewApplicationsCount();
        model.addAttribute("interviewPendingApplicationsCount", approveForInterviewApplicationsCount);

        //count approve for internship(get interview pending results )
        long countPendingInterviewApplications = internService.countPendingInterviewApplications();
        model.addAttribute("countInterviewApplications", countPendingInterviewApplications);

        //Project definitions remaining to approve from admin
        long adminPendingProjectDefinitionCount = groupService.adminPendingProjectDefinitionCount();
        model.addAttribute("adminPendingProjectDefinitionCount", adminPendingProjectDefinitionCount);

        long pendingGroupCreationCount = internService.pendingGroupCreationCount();
        model.addAttribute("pendingGroupCreationCount", pendingGroupCreationCount);

        //cancellation request count
        long requestedCancellationsCount = internService.countRequestedCancellations();
        model.addAttribute("requestedCancellationsCount", requestedCancellationsCount);

        long countGuide = guideService.countGuides();
        model.addAttribute("countGuide", countGuide);

        // New notifications
        long pendingLeaveApplicationsCount = leaveApplicationService.countPendingLeaveApplications();
        model.addAttribute("pendingLeaveApplicationsCount", pendingLeaveApplicationsCount);

        long pendingVerificationRequestsCount = verificationService.countPendingVerificationRequests();
        model.addAttribute("pendingVerificationRequestsCount", pendingVerificationRequestsCount);

        return model;
    }

    // Admin Dashboard

    @GetMapping("/admin_dashboard")
    public ModelAndView admin_dashboard(Model model) {

        ModelAndView mv = new ModelAndView("admin/admin_dashboard");

        String username = (String) session.getAttribute("username");

        Admin admin = adminService.getAdminByUsername(username);

        if (admin != null) {
            session.setAttribute("id", admin.getAdminId());
            session.setAttribute("username", username);

            logService.saveLog(String.valueOf(admin.getAdminId()), "Admin Dashboard Accessed",
                    "Admin " + admin.getName() + " accessed the dashboard.");

            model = countNotifications(model);

            long countInterns = internService.countInterns();
            model.addAttribute("countInterns", countInterns);

            // Add the username to the ModelAndView
            mv.addObject("username", username);
            mv.addObject("admin", admin);
        } else {
            System.out.println("Error: Admin not found for logging!");
        }

        return mv;
    }

    @GetMapping("/register_internn")
    public ModelAndView registerInternn() {
        ModelAndView mv = new ModelAndView("admin/register_internn");
        List<College> colleges = fieldService.getColleges();
        List<Domain> domains = fieldService.getDomains();
//        List<Branch> branches = fieldService.getBranches();
        List<Degree> degrees = fieldService.getDegrees();
        mv.addObject("colleges", colleges);
        mv.addObject("domains", domains);
//        mv.addObject("branches", branches);
        mv.addObject("degrees", degrees);

        return mv;
    }

    @PostMapping("/register_internn")
    public String registerInternn(@RequestParam("firstName") String firstName,
//                                  @RequestParam("lastName") String lastName,
                                  @RequestParam("contactNo") String contactNo,
                                  @RequestParam("email") String email,
                                  @RequestParam("collegeName") String collegeName,
//                                  @RequestParam("branch") String branch,
                                  @RequestParam("passportSizeImage") MultipartFile passportSizeImage,
                                  @RequestParam("icardImage") MultipartFile icardImage,
                                  @RequestParam("nocPdf") MultipartFile nocPdf,
                                  @RequestParam("resumePdf") MultipartFile resumePdf,
                                  @RequestParam("semester") int semester,
                                  @RequestParam("password") String password,
                                  @RequestParam("degree") String degree,
                                  @RequestParam("domain") String domain,
                                  @RequestParam("joiningDate") java.sql.Date joiningDate,
                                  @RequestParam("completionDate") java.sql.Date completionDate,
                                  @RequestParam("securityPin") String securityPin,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {

        try {
            String storageDir = baseDir + email + "/";
            File directory = new File(storageDir);

            if (!directory.exists()) {
                directory.mkdirs();
            }


            // Save files to local storage
            String passportFileName = storageDir + "passportSizeImage.jpg";
            String icardFileName = storageDir + "collegeIcardImage.jpg";
            String nocFileName = storageDir + "nocPdf.pdf";
            String resumeFileName = storageDir + "resumePdf.pdf";

            // Save Passport Size Image
            Files.write(Paths.get(passportFileName), passportSizeImage.getBytes());

            // Save College Icard Image
            Files.write(Paths.get(icardFileName), icardImage.getBytes());

            // Save NOC PDF
            Files.write(Paths.get(nocFileName), nocPdf.getBytes());

            // Save Resume PDF
            Files.write(Paths.get(resumeFileName), resumePdf.getBytes());

            InternApplication internApplication = new InternApplication();
            internApplication.setFirstName(firstName);
//            internApplication.setLastName(lastName);
            internApplication.setContactNo(contactNo);
            internApplication.setEmail(email);
            internApplication.setCollegeName(collegeName);
//            internApplication.setBranch(branch);
            internApplication.setSemester(semester);
            internApplication.setPassword(password);
            internApplication.setDegree(degree);
            internApplication.setDomain(domain);
            internApplication.setJoiningDate(joiningDate);
            internApplication.setCompletionDate(completionDate);
            internApplication.setSecurityPin(securityPin);

            internApplication.setPassportSizeImage(passportSizeImage.getBytes());
            internApplication.setCollegeIcardImage(icardImage.getBytes());
            internApplication.setNocPdf(nocPdf.getBytes());
            internApplication.setResumePdf(resumePdf.getBytes());

            internApplicationRepo.save(internApplication);

            MyUser user = new MyUser();
            user.setUsername(email);
            String encryptedPassword = passwordEncoder().encode(password);
            user.setPassword(encryptedPassword);
            user.setSecurityPin(securityPin);
            user.setEnabled(true);
            user.setUserId(Long.toString(internApplication.getId()));
            user.setRole("UNDERPROCESSINTERN");
            userRepo.save(user);

            // Send success email
            emailService.sendSimpleEmail(
                    internApplication.getEmail(),
                    "Notification: Successful Application for BISAG Internship\r\n" +
                            "\r\n" +
                            "Dear " + internApplication.getFirstName() + ",\r\n" +
                            "\r\n" +
                            "Congratulations! We are pleased to inform you that your application for the BISAG internship has been successful. Your enthusiasm, qualifications, and potential have stood out, and we believe that you will make valuable contributions to our team.\r\n" +
                            "\r\n" +
                            "As an intern, you will have the opportunity to learn, grow, and gain hands-on experience in a dynamic and innovative environment. We trust that your time with us will be rewarding, and we look forward to seeing your skills and talents in action.\r\n" +
                            "\r\n" +
                            "Please find attached detailed information about the internship program, including your start date, orientation details, and any additional requirements. If you have any questions or need further assistance, feel free to contact [Contact Person/Department].\r\n" +
                            "\r\n" +
                            "Once again, congratulations on being selected for the BISAG internship program. We are excited to welcome you to our team and wish you a fulfilling and successful internship experience.\r\n" +
                            "\r\n" +
                            "Best regards,\r\n" +
                            "\r\n" +
                            "Your Colleague,\r\n" +
                            "Internship Coordinator\r\n" +
                            "BISAG INTERNSHIP PROGRAM\r\n" +
                            "1231231231",
                    "BISAG ADMINISTRATIVE OFFICE"
            );
//            session.setAttribute("successMsg", "Application submitted successfully!");
            redirectAttributes.addFlashAttribute("successMsg", "Intern Application Submitted Successfully!");
            return "redirect:/bisag/admin/register_internn";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error submitting application. Please try again.");
        }
        return "redirect:/bisag/admin/intern_application";
    }


    @PostMapping("/register_intern")
    public String registerIntern(@ModelAttribute("intern") Intern intern) {
        intern.setInternId(generateInternId());
        internService.registerIntern(intern);

        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Admin admin = adminService.getAdminByUsername(adminUsername);

        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Intern Registered",
                    "Admin " + admin.getName() + " registered a new intern with ID: " + intern.getInternId());
        } else {
            System.out.println("Error: Admin not found for logging!");
        }

        return "redirect:/bisag";
    }

    @GetMapping("/intern/{id}")
    public ModelAndView internDetails(@PathVariable("id") String id, Model model) {
        ModelAndView mv = new ModelAndView();

        Optional<Intern> intern = internService.getIntern(id);
        model = countNotifications(model);

        mv.addObject("intern", intern);

        List<College> colleges = fieldService.getColleges();
        List<Domain> domains = fieldService.getDomains();
//        List<Branch> branches = fieldService.getBranches();
        List<GroupEntity> groups = groupService.getGroups();

        mv.addObject("colleges", colleges);
        mv.addObject("domains", domains);
//        mv.addObject("branches", branches);
        mv.addObject("groups", groups);

        mv.setViewName("admin/intern_detail");

        Admin admin = getSignedInAdmin();
        if (admin != null && intern.isPresent()) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Intern Details",
                    "Admin " + admin.getName() + " viewed the details of intern ID: " + id + ", Name: " + intern.get().getFirstName());
        } else {
            System.out.println("Error: Admin or Intern not found for logging!");
        }

        return mv;
    }

    @GetMapping("/update_admin/{id}")
    public ModelAndView updateAdmin(@PathVariable("id") long id, Model model) {
        ModelAndView mv = new ModelAndView("super_admin/update_admin");

        Optional<Admin> admin = adminService.getAdmin(id);
        mv.addObject("admin", admin.orElse(new Admin()));

        model = countNotifications(model);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null && admin.isPresent()) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "Updating Admin Details",
                    "Admin " + signedInAdmin.getName() + " is updating the details of admin ID: " + id + ", Name: " + admin.get().getName());
        } else {
            System.out.println("Error: Signed-in admin or target admin not found for logging!");
        }

        return mv;
    }

    @PostMapping("/update_admin/{id}")
    public String updateAdmin(@ModelAttribute("admin") Admin admin, @PathVariable("id") long id) {
        Optional<Admin> existingAdmin = adminService.getAdmin(admin.getAdminId());

        if (existingAdmin.isPresent()) {

            String currentPassword = existingAdmin.get().getPassword();
            Admin updatedAdmin = existingAdmin.get();
            updatedAdmin.setName(admin.getName());
            updatedAdmin.setLocation(admin.getLocation());
            updatedAdmin.setContactNo(admin.getContactNo());
            updatedAdmin.setEmailId(admin.getEmailId());
            Admin signedInAdmin = getSignedInAdmin();

            if (signedInAdmin != null) {
                logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "Updating Admin Details",
                        "Admin " + signedInAdmin.getName() + " updated the details of admin ID: " + id + ", Name: " + updatedAdmin.getName());

            } else {
                System.out.println("Error: Signed-in admin not found for logging!");
            }

            adminService.updateAdmin(updatedAdmin, existingAdmin);
        }
        return "redirect:/logout";
    }

    // Manage intern application
    @GetMapping("/intern_application")
    public ModelAndView internApplication(Model model) {
        ModelAndView mv = new ModelAndView("admin/intern_application");

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View Intern Applications",
                    "Admin " + signedInAdmin.getName() + " accessed the intern application page.");
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        List<InternApplication> interns = internService.getInternApplication();
        model = countNotifications(model);
        mv.addObject("interns", interns);
        mv.addObject("admin", adminName(session));

        return mv;
    }

    @GetMapping("/intern_application/{id}")
    public ModelAndView internApplication(@PathVariable("id") long id, Model model) {
        System.out.println("id" + id);
        ModelAndView mv = new ModelAndView();

        Optional<InternApplication> intern = internService.getInternApplication(id);
        List<Guide> guides = guideService.getGuide();

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View Intern Application Details",
                    "Admin " + signedInAdmin.getName() + " viewed the details of Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        mv.addObject("intern", intern);
        model.addAttribute("guides", guides);

        List<College> colleges = fieldService.getColleges();
        List<Domain> domains = fieldService.getDomains();
//        List<Branch> branches = fieldService.getBranches();
        model = countNotifications(model);

        mv.addObject("colleges", colleges);
        mv.addObject("domains", domains);
//        mv.addObject("branches", branches);

        mv.setViewName("admin/intern_application_detail");
        return mv;
    }



    @GetMapping("/intern_application_docs/{id}")
    public ModelAndView internApplicationDocs(@PathVariable("id") long id, Model model) {
        Optional<InternApplication> optionalApplication = internService.getInternApplication(id);
        System.out.println("ID: " + id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View Intern Application Documents",
                    "Admin " + signedInAdmin.getName() + " viewed the documents for Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        // Check if the intern application exists
        if (optionalApplication.isPresent()) {
            InternApplication application = optionalApplication.get();
            model.addAttribute("id", id);
            model.addAttribute("passportSizeImage", application.getPassportSizeImage());
            model.addAttribute("collegeIcardImage", application.getCollegeIcardImage());
            model.addAttribute("nocPdf", application.getNocPdf());
            model.addAttribute("resumePdf", application.getResumePdf());
        } else {
            model.addAttribute("error", "Intern Application not found");
        }

        ModelAndView mv = new ModelAndView();
        mv.setViewName("admin/intern_application_docs");
        return mv;
    }

    @GetMapping("/intern_docs/{id}")
    public ModelAndView internDocs(@PathVariable("id") String id, Model model) {
        Optional<Intern> optionalApplication = internService.getIntern(id);
        System.out.println("ID: " + id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View Intern Documents",
                    "Admin " + signedInAdmin.getName() + " viewed the documents for Intern with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        // Check if the intern exists
        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();
            model.addAttribute("id", id);
            model.addAttribute("intern", application);
            model.addAttribute("passportSizeImage", application.getPassportSizeImage());
            model.addAttribute("collegeIcardImage", application.getCollegeIcardImage());
            model.addAttribute("nocPdf", application.getNocPdf());
            model.addAttribute("resumePdf", application.getResumePdf());
            model.addAttribute("icardForm", application.getIcardForm());
            model.addAttribute("registrationForm", application.getRegistrationForm());
            model.addAttribute("securityForm", application.getSecurityForm());
        } else {
            model.addAttribute("error", "Intern Application not found");
        }

        ModelAndView mv = new ModelAndView();
        mv.setViewName("admin/intern_docs");
        return mv;
    }

    @GetMapping("/documents/passport/{id}")
    public ResponseEntity<byte[]> getPassportSizeImageForInternApplication(@PathVariable("id") long id) {
        Optional<InternApplication> optionalApplication = internService.getInternApplication(id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View Passport Size Image",
                    "Admin " + signedInAdmin.getName() + " viewed the passport size image for Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            InternApplication application = optionalApplication.get();

            byte[] image = application.getPassportSizeImage();

            if (image != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(image);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/intern/documents/passport/{id}")
    public ResponseEntity<byte[]> getPassportSizeImageForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View Passport Size Image",
                    "Admin " + signedInAdmin.getName() + " viewed the passport size image for Intern with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            byte[] image = application.getPassportSizeImage();

            if (image != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(image);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/documents/icard/{id}")
    public ResponseEntity<byte[]> getICardImageForInternApplication(@PathVariable("id") long id) {
        Optional<InternApplication> optionalApplication = internService.getInternApplication(id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View College I-card Image",
                    "Admin " + signedInAdmin.getName() + " viewed the college I-card image for Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            InternApplication application = optionalApplication.get();

            byte[] image = application.getCollegeIcardImage();

            if (image != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(image);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/intern/documents/icard/{id}")
    public ResponseEntity<byte[]> getICardImageForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View College I-card Image",
                    "Admin " + signedInAdmin.getName() + " viewed the college I-card image for Intern with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            byte[] image = application.getCollegeIcardImage();

            if (image != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(image);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/documents/noc/{id}")
    public ResponseEntity<byte[]> getNocPdfForInternApplication(@PathVariable("id") long id) {
        Optional<InternApplication> optionalApplication = internService.getInternApplication(id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View NOC PDF",
                    "Admin " + signedInAdmin.getName() + " viewed the NOC PDF for Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            InternApplication application = optionalApplication.get();

            byte[] pdf = application.getNocPdf();

            if (pdf != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdf);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/intern/documents/noc/{id}")
    public ResponseEntity<byte[]> getNocPdfForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View NOC PDF",
                    "Admin " + signedInAdmin.getName() + " viewed the NOC PDF for Intern with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            byte[] pdf = application.getNocPdf();

            if (pdf != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdf);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/documents/resume/{id}")
    public ResponseEntity<byte[]> getResumePdfForInternApplication(@PathVariable("id") long id) {
        Optional<InternApplication> optionalApplication = internService.getInternApplication(id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View Resume PDF",
                    "Admin " + signedInAdmin.getName() + " viewed the Resume PDF for Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            InternApplication application = optionalApplication.get();

            byte[] pdf = application.getResumePdf();

            if (pdf != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdf);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/intern/documents/resume/{id}")
    public ResponseEntity<byte[]> getResumePdfForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View Resume PDF",
                    "Admin " + signedInAdmin.getName() + " viewed the Resume PDF for Intern with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            byte[] pdf = application.getResumePdf();

            if (pdf != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdf);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/intern/documents/registration/{id}")
    public ResponseEntity<byte[]> getRegistrationFormForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            byte[] pdf = application.getRegistrationForm();

            if (pdf != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdf);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/intern/documents/security/{id}")
    public ResponseEntity<byte[]> getSecurityFormForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            byte[] pdf = application.getSecurityForm();

            if (pdf != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdf);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/intern/documents/icardForm/{id}")
    public ResponseEntity<byte[]> getICardFormForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            byte[] pdf = application.getIcardForm();

            if (pdf != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdf);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/intern/documents/projectDefinitionForm/{id}")
    public ResponseEntity<byte[]> getProjectDefinitionFormForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            byte[] pdf = application.getProjectDefinitionForm();

            if (pdf != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdf);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/intern/documents/projectDefinitionForm/{id}")
    public String updateProjectDefinitionFormForIntern(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<Intern> optionalApplication = internService.getIntern(id);
        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "projectDefinitionForm.pdf";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "projectDefinitionForm.pdf";
            Files.write(Paths.get(newFilePath), file.getBytes());

            application.setProjectDefinitionForm(file.getBytes());
            internService.save(application);

            return "redirect:/bisag/admin/intern_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_docs/" + id;
    }
    @PostMapping("/documents/passport/{id}")
    public String updatePassportSizeImage(@PathVariable("id") long id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<InternApplication> optionalApplication = internService.getInternApplication(id);
        Admin signedInAdmin = getSignedInAdmin();

        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "Update Passport Size Image",
                    "Admin " + signedInAdmin.getName() + " updated the passport size image for Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            InternApplication application = optionalApplication.get();
            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "passportSizeImage.jpg";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "passportSizeImage.jpg";
            Files.write(Paths.get(newFilePath), file.getBytes());
            application.setPassportSizeImage(file.getBytes());
            internService.save(application);
            return "redirect:/bisag/admin/intern_application_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_application_docs/" + id;
    }

    @PostMapping("/documents/icard/{id}")
    public String updateICardImage(@PathVariable("id") long id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<InternApplication> optionalApplication = internService.getInternApplication(id);
        Admin signedInAdmin = getSignedInAdmin();

        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "Update I-Card Image",
                    "Admin " + signedInAdmin.getName() + " updated the college I-Card image for Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            InternApplication application = optionalApplication.get();
            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "collegeIcardImage.jpg";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "collegeIcardImage.jpg";
            Files.write(Paths.get(newFilePath), file.getBytes());
            application.setCollegeIcardImage(file.getBytes());
            internService.save(application);
            return "redirect:/bisag/admin/intern_application_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_application_docs/" + id;
    }

    @PostMapping("/documents/noc/{id}")
    public String updateNocPdf(@PathVariable("id") long id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<InternApplication> optionalApplication = internService.getInternApplication(id);
        Admin signedInAdmin = getSignedInAdmin();

        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "Update NOC PDF",
                    "Admin " + signedInAdmin.getName() + " updated the NOC PDF for Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            InternApplication application = optionalApplication.get();
            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "nocPdf.pdf";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "nocPdf.pdf";
            Files.write(Paths.get(newFilePath), file.getBytes());
            application.setNocPdf(file.getBytes());
            internService.save(application);
            return "redirect:/bisag/admin/intern_application_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_application_docs/" + id;
    }

    @PostMapping("/documents/resume/{id}")
    public String updateResumePdf(@PathVariable("id") long id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<InternApplication> optionalApplication = internService.getInternApplication(id);
        Admin signedInAdmin = getSignedInAdmin();

        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "Update Resume PDF",
                    "Admin " + signedInAdmin.getName() + " updated the Resume PDF for Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            InternApplication application = optionalApplication.get();
            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "resumePdf.pdf";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "resumePdf.pdf";
            Files.write(Paths.get(newFilePath), file.getBytes());

            application.setResumePdf(file.getBytes());
            internService.save(application);

            return "redirect:/bisag/admin/intern_application_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_application_docs/" + id;
    }

    @PostMapping("/intern/documents/passport/{id}")
    public String updatePassportSizeImageForIntern(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<Intern> optionalApplication = internService.getIntern(id);
        Admin signedInAdmin = getSignedInAdmin();

        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "Update Passport Size Image",
                    "Admin " + signedInAdmin.getName() + " updated the passport size image for Intern with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();
            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "passportSizeImage.jpg";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "passportSizeImage.jpg";
            Files.write(Paths.get(newFilePath), file.getBytes());
            application.setPassportSizeImage(file.getBytes());
            internService.save(application);
            return "redirect:/bisag/admin/intern_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_docs/" + id;
    }

    @PostMapping("/intern/documents/icard/{id}")
    public String updateICardImageForIntern(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<Intern> optionalApplication = internService.getIntern(id);
        Admin signedInAdmin = getSignedInAdmin();

        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "Update I-Card Image",
                    "Admin " + signedInAdmin.getName() + " updated the college I-Card image for Intern with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();
            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "collegeIcardImage.jpg";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "collegeIcardImage.jpg";
            Files.write(Paths.get(newFilePath), file.getBytes());
            application.setCollegeIcardImage(file.getBytes());
            internService.save(application);
            return "redirect:/bisag/admin/intern_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_docs/" + id;
    }

    @PostMapping("/intern/documents/noc/{id}")
    public String updateNocPdfForIntern(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<Intern> optionalApplication = internService.getIntern(id);
        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();
            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "nocPdf.pdf";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "nocPdf.pdf";
            Files.write(Paths.get(newFilePath), file.getBytes());
            application.setNocPdf(file.getBytes());
            internService.save(application);
            return "redirect:/bisag/admin/intern_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_docs/" + id;
    }

    @PostMapping("/intern/documents/resume/{id}")
    public String updateResumePdfForIntern(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<Intern> optionalApplication = internService.getIntern(id);
        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "resumePdf.pdf";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "resumePdf.pdf";
            Files.write(Paths.get(newFilePath), file.getBytes());

            application.setResumePdf(file.getBytes());
            internService.save(application);

            return "redirect:/bisag/admin/intern_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_docs/" + id;
    }

    @PostMapping("/intern/documents/icardForm/{id}")
    public String updateICardFormForIntern(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<Intern> optionalApplication = internService.getIntern(id);
        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "icardForm.pdf";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "icardForm.pdf";
            Files.write(Paths.get(newFilePath), file.getBytes());

            application.setIcardForm(file.getBytes());
            internService.save(application);

            return "redirect:/bisag/admin/intern_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_docs/" + id;
    }

    @GetMapping("/intern/documents/extraForm/{id}")
    public ResponseEntity<byte[]> getExtraFormForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();
            byte[] pdf = application.getExtraForm();

            if (pdf != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdf);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/intern/documents/extraForm/{id}")
    public String updateExtraFormForIntern(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<Intern> optionalApplication = internService.getIntern(id);
        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "extraForm.pdf";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "extraForm.pdf";
            Files.write(Paths.get(newFilePath), file.getBytes());

            application.setExtraForm(file.getBytes());
            internService.save(application);

            return "redirect:/bisag/admin/intern_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_docs/" + id;
    }

    @GetMapping("/intern/documents/extraForm2/{id}")
    public ResponseEntity<byte[]> getExtraForm2ForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();
            byte[] pdf = application.getExtraForm2();

            if (pdf != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdf);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/intern/documents/extraForm2/{id}")
    public String updateExtraForm2ForIntern(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<Intern> optionalApplication = internService.getIntern(id);
        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "extraForm2.pdf";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "extraForm2.pdf";
            Files.write(Paths.get(newFilePath), file.getBytes());

            application.setExtraForm2(file.getBytes());
            internService.save(application);

            return "redirect:/bisag/admin/intern_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_docs/" + id;
    }

    @PostMapping("/intern/documents/registration/{id}")
    public String updateRegistrationFormForIntern(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<Intern> optionalApplication = internService.getIntern(id);
        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "registrationForm.pdf";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "registrationForm.pdf";
            Files.write(Paths.get(newFilePath), file.getBytes());

            application.setRegistrationForm(file.getBytes());
            internService.save(application);

            return "redirect:/bisag/admin/intern_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_docs/" + id;
    }

    @PostMapping("/intern/documents/security/{id}")
    public String updateSecurityFormForIntern(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<Intern> optionalApplication = internService.getIntern(id);
        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "securityForm.pdf";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "securityForm.pdf";
            Files.write(Paths.get(newFilePath), file.getBytes());

            application.setSecurityForm(file.getBytes());
            internService.save(application);

            return "redirect:/bisag/admin/intern_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_docs/" + id;
    }

    @PostMapping("/intern_application/ans")
    public String internApplicationSubmission(@RequestParam String message, @RequestParam long id,
                                              @RequestParam String status, @RequestParam String finalStatus) {
        System.out.println("id: " + id + ", status: " + status);

        Optional<InternApplication> intern = internService.getInternApplication(id);

        if (intern.isPresent()) {
            intern.get().setStatus(status);
            intern.get().setFinalStatus(finalStatus);
            internService.addInternApplication(intern.get());

            logService.saveLog(String.valueOf(id), "Updated application status for intern", "Status Change");

            // Send email notifications based on status
            if (status.equals("rejected")) {
                emailService.sendSimpleEmail(intern.get().getEmail(),
                        "Notification: Rejection of BISAG Internship Application\r\n" + "\r\n" + "Dear "
                                + intern.get().getFirstName() + ",\r\n" + "\r\n"
                                + "We appreciate your interest in the BISAG internship program and the effort you put into your application. After careful consideration, we regret to inform you that your application has not been successful on this occasion.\r\n"
                                + "\r\n"
                                + "Please know that the decision was a difficult one, and we had many qualified candidates. We want to thank you for your interest in joining our team and for taking the time to apply for the internship position.\r\n"
                                + "\r\n"
                                + "We encourage you to continue pursuing your goals, and we wish you the best in your future endeavors. If you have any feedback or questions about the decision, you may reach out to [Contact Person/Department].\r\n"
                                + "\r\n"
                                + "Thank you again for considering BISAG for your internship opportunity. We appreciate your understanding.\r\n"
                                + "\r\n" + "Best regards,\r\n" + "\r\n" + "Your Colleague,\r\n"
                                + "Internship Coordinator\r\n" + "BISAG INTERNSHIP PROGRAM\r\n" + "1231231231",
                        "BISAG INTERNSHIP RESULT");
            } else {
                emailService.sendSimpleEmail(intern.get().getEmail(), message + " your unique id is " + intern.get().getId(),
                        "BISAG INTERNSHIP RESULT");
            }
        }
        return "redirect:/bisag/admin/intern_application";
    }

    @PostMapping("/intern_application/update")
    public String internApplicationSubmission(@RequestParam long id, InternApplication internApplication, MultipartHttpServletRequest req) throws IllegalStateException, IOException, Exception {
        Optional<InternApplication> intern = internService.getInternApplication(id);

        if (internApplication.getIsActive()) {
            intern.get().setFirstName(internApplication.getFirstName());
//            intern.get().setLastName(internApplication.getLastName());
            intern.get().setContactNo(internApplication.getContactNo());

            MyUser user = myUserService.getUserByUsername(intern.get().getEmail());
            user.setUsername(internApplication.getEmail());
            userRepo.save(user);

            intern.get().setEmail(internApplication.getEmail());
            intern.get().setCollegeName(internApplication.getCollegeName());
            intern.get().setIsActive(true);
//            intern.get().setBranch(internApplication.getBranch());
            intern.get().setDomain(internApplication.getDomain());
            intern.get().setSemester(internApplication.getSemester());
            intern.get().setJoiningDate(internApplication.getJoiningDate());
            intern.get().setCompletionDate(internApplication.getCompletionDate());
            intern.get().setGuideName(internApplication.getGuideName());
            intern.get().setGuideId(internApplication.getGuideId());

            logService.saveLog(String.valueOf(id), "Updated intern application details", "InternApplication Update");
        } else {
            intern.get().setIsActive(false);
            Cancelled cancelledEntry = new Cancelled();
            cancelledEntry.setTableName("InternApplication");
            cancelledEntry.setCancelId(Long.toString(intern.get().getId()));
            cancelledRepo.save(cancelledEntry);

            logService.saveLog(String.valueOf(id), "Cancelled intern application", "InternApplication Cancellation");
        }

        intern.get().setUpdatedAt(LocalDateTime.now());
        internService.addInternApplication(intern.get());
        return "redirect:/bisag/admin/intern_application/" + id;
    }

    @PostMapping("/intern/update")
    public String updateIntern(@RequestParam String id, Intern internApplication, @RequestParam("groupId") String groupId,  MultipartHttpServletRequest req, RedirectAttributes redirectAttributes) throws IllegalStateException, IOException, Exception {
        Optional<Intern> intern = internService.getIntern(id);

        if (groupId.equals("createOwnGroup")) {
            String generatedId = generateGroupId();
            GroupEntity group = new GroupEntity();
            group.setGroupId(generatedId);
            groupService.registerGroup(group);
            intern.get().setGroup(group);
        } else {
            intern.get().setGroup(groupService.getGroup(groupId));
        }

        if (intern.get().getIsActive()) {
            intern.get().setFirstName(internApplication.getFirstName());
//            intern.get().setLastName(internApplication.getLastName());
            intern.get().setContactNo(internApplication.getContactNo());

            MyUser user = myUserService.getUserByUsername(intern.get().getEmail());
            user.setUsername(internApplication.getEmail());
            userRepo.save(user);

            InternApplication internA = internService.getInternApplicationByUsername(intern.get().getEmail());
//            internA.setEmail(internApplication.getEmail());
            if (internA != null) {
                internA.setEmail(internApplication.getEmail());
                internApplicationRepo.save(internA);
            } else {
                // Handle gracefully, maybe log or redirect with error message
                System.out.println("No InternApplication found for ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Intern application not found.");
                return "redirect:/bisag/admin/intern/" + id;
            }
//            internApplicationRepo.save(internA);

            intern.get().setEmail(internApplication.getEmail());
            intern.get().setCollegeName(internApplication.getCollegeName());
//            intern.get().setBranch(internApplication.getBranch());
            intern.get().setIsActive(true);
            intern.get().setDomain(internApplication.getDomain());
            intern.get().setSemester(internApplication.getSemester());
            intern.get().setJoiningDate(internApplication.getJoiningDate());
            intern.get().setCompletionDate(internApplication.getCompletionDate());
            intern.get().setPermanentAddress(internApplication.getPermanentAddress());
            intern.get().setDateOfBirth(internApplication.getDateOfBirth());
            intern.get().setGender(internApplication.getGender());
            intern.get().setCollegeGuideHodName(internApplication.getCollegeGuideHodName());
            intern.get().setDegree(internApplication.getDegree());
            intern.get().setAggregatePercentage(internApplication.getAggregatePercentage());
            intern.get().setUsedResource(internApplication.getUsedResource());
//            intern.get().setCancellationRemarks(cancellationRemarks);

            logService.saveLog(id, "Updated intern details", "Intern Update");
        }

        if (!internApplication.getIsActive()) {
            Intern internData = intern.get();

            if (internData.getFirstName() != null &&
//                    internData.getLastName() != null &&
                    internData.getContactNo() != null &&
                    internData.getEmail() != null &&
                    internData.getCollegeName() != null &&
//                    internData.getBranch() != null &&
                    internData.getDomain() != null &&
                    internData.getJoiningDate() != null &&
                    internData.getCompletionDate() != null &&
                    internData.getPermanentAddress() != null &&
                    internData.getDateOfBirth() != null &&
                    internData.getGender() != null &&
                    internData.getCollegeGuideHodName() != null &&
                    internData.getDegree() != null &&
                    internData.getAggregatePercentage() != null &&
                    internData.getUsedResource() != null &&
                    internData.getIcardForm() != null &&
                    internData.getDegree() != null &&
                    internData.getGender() != null &&
                    internData.getNocPdf() != null &&
                    internData.getProjectDefinitionName() != null &&
                    internData.getRegistrationForm() != null &&
                    internData.getResumePdf() != null &&
                    internData.getSecurityForm() != null &&
                    internData.getProjectDefinitionForm() != null) {

                internData.setIsActive(false);
                internData.setCancellationStatus("Cancelled");

                Cancelled cancelledEntry = new Cancelled();
                cancelledEntry.setTableName("intern");
                cancelledEntry.setCancelId(internData.getInternId());
                cancelledRepo.save(cancelledEntry);

                logService.saveLog(id, "Cancelled intern", "Intern Cancellation");
            } else {
                System.out.println("All fields must be filled to cancel intern.");
            }
        }

        intern.get().setUpdatedAt(LocalDateTime.now());
        internRepo.save(intern.get());
        redirectAttributes.addFlashAttribute("success","Intern Details updated successfully");
        return "redirect:/bisag/admin/intern/" + id;
    }



    @GetMapping("/intern_application/approved_interns")
    public ModelAndView approvedInterns(Model model) {
        ModelAndView mv = new ModelAndView();
        List<InternApplication> intern = internService.getApprovedInterns();
        model = countNotifications(model);

        mv.addObject("interns", intern);
        mv.setViewName("admin/approved_interns");
        mv.addObject("admin", adminName(session));

        String username = (String) session.getAttribute("username");

        Admin admin = adminService.getAdminByUsername(username);

        if (admin != null) {

            session.setAttribute("id", admin.getAdminId());

            logService.saveLog(String.valueOf(admin.getAdminId()),
                    "View Shortlisted Intern Applications",
                    "Admin " + admin.getName() + " accessed the shortlisted intern applications page.");
        } else {
            System.out.println("Error: Admin not found for logging!");
        }

        return mv;
    }

    @GetMapping("/intern_application/approved_intern/{id}")
    public ModelAndView approvedInterns(@PathVariable("id") long id, Model model) {
        System.out.println("approved id: " + id);
        ModelAndView mv = new ModelAndView();

        Optional<InternApplication> intern = internService.getInternApplication(id);
        mv.addObject("intern", intern);

        List<College> colleges = fieldService.getColleges();
        List<Domain> domains = fieldService.getDomains();
//        List<Branch> branches = fieldService.getBranches();

        model = countNotifications(model);
        mv.addObject("colleges", colleges);
        mv.addObject("domains", domains);
//        mv.addObject("branches", branches);
        mv.setViewName("admin/approved_intern_application_detail");

        String username = (String) session.getAttribute("username");

        Admin admin = adminService.getAdminByUsername(username);

        if (admin != null) {

            session.setAttribute("id", admin.getAdminId());

            logService.saveLog(String.valueOf(admin.getAdminId()),
                    "View Shortlisted Intern Application Details",
                    "Admin " + admin.getName() + " accessed the details of shortlisted intern with ID: " + id);
        } else {
            System.out.println("Error: Admin not found for logging!");
        }

        return mv;
    }

    @PostMapping("/intern_application/approved_intern/update")
    public String approvedInterns(@RequestParam long id, InternApplication internApplication, MultipartHttpServletRequest req, RedirectAttributes   redirectAttributes)
            throws IllegalStateException, IOException, Exception {
        Optional<InternApplication> intern = internService.getInternApplication(id);

        if (internApplication.getIsActive()) {
            intern.get().setFirstName(internApplication.getFirstName());
//            intern.get().setLastName(internApplication.getLastName());
            intern.get().setContactNo(internApplication.getContactNo());
            intern.get().setEmail(internApplication.getEmail());
            intern.get().setIsActive(true);
            intern.get().setCollegeName(internApplication.getCollegeName());
//            intern.get().setBranch(internApplication.getBranch());
            intern.get().setDomain(internApplication.getDomain());
            intern.get().setSemester(internApplication.getSemester());
            intern.get().setJoiningDate(internApplication.getJoiningDate());
            intern.get().setCompletionDate(internApplication.getCompletionDate());
        } else {
            intern.get().setIsActive(false);
            Cancelled cancelledEntry = new Cancelled();
            cancelledEntry.setTableName("InternApplication");
            cancelledEntry.setCancelId(Long.toString(intern.get().getId()));
            cancelledRepo.save(cancelledEntry);
        }

        internService.addInternApplication(intern.get());
//        redirectAttributes.addFlashAttribute("success","Intern Details updated successfully");
        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Update Approved Intern",
                    "Admin " + admin.getName() + " updated details of approved intern with ID: " + id);
        }
        redirectAttributes.addFlashAttribute("success","intern details updated successfully");
        return "redirect:/bisag/admin/intern_application/approved_interns";
    }


    @PostMapping("/intern_application/approved_intern/ans")
    public String approvedInterns(@RequestParam String message, @RequestParam long id, @RequestParam String finalStatus) {
        System.out.println("id: " + id + ", finalStatus: " + finalStatus);

        Optional<InternApplication> intern = internService.getInternApplication(id);
        intern.get().setFinalStatus(finalStatus);
        internService.addInternApplication(intern.get());

        if (finalStatus.equals("failed")) {
            emailService.sendSimpleEmail(intern.get().getEmail(), "You are Failed", "BISAG INTERNSHIP RESULT");
        } else {
            String finalMessage = message + "\n" + "Username: " + intern.get().getFirstName() +
                    "\n Password: " + intern.get().getFirstName() + "_" + intern.get().getId();
            emailService.sendSimpleEmail(intern.get().getEmail(), finalMessage, "BISAG INTERNSHIP RESULT");
        }

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Updated Final Status",
                    "Admin with ID: " + admin.getAdminId() + " updated final status of intern with ID: " + id + " to " + finalStatus);
        }

        return "redirect:/bisag/admin/intern_application/approved_interns";
    }

    @GetMapping("/intern_application/new_interns")
    public ModelAndView newInterns(Model model) {
        ModelAndView mv = new ModelAndView();
        List<Intern> intern = internService.getInterns();
        mv.addObject("intern", intern);
        model = countNotifications(model);
        mv.setViewName("admin/new_interns");
        mv.addObject("admin", adminName(session));
//        List<RRecord> records = recordService.getAllRecords();
//        model.addAttribute("records", records);

        Map<String, String> finalReportStatuses = new HashMap<>();
        for (Intern i : intern) {
//            String finalReport = recordService.findFinalReportByInternId(i.getInternId());
//            finalReportStatuses.put(i.getInternId(), finalReport != null ? finalReport : "no");
        }
        model.addAttribute("finalReportStatuses", finalReportStatuses);

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed New Interns",
                    "Admin " + admin.getName() + " accessed the new interns page.");
        }

        return mv;
    }
    // Group Creation

    @GetMapping("/create_group")
    public ModelAndView groupCreation(Model model) {
        ModelAndView mv = new ModelAndView();
        List<InternApplication> intern = internService.getInternApplication();
        mv.addObject("interns", intern);
        mv.addObject("admin", adminName(session));
        model = countNotifications(model);
        mv.setViewName("admin/create_group");

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Group Creation",
                    "Admin " + admin.getName() + " accessed the group creation page.");
        }

        return mv;
    }

    @PostMapping("/create_group_details")
    public String createGroup(@RequestParam("selectedInterns") List<Long> selectedInterns) {

        System.out.println("Selected Intern IDs: " + selectedInterns);
        String id = generateGroupId();
        GroupEntity group = new GroupEntity();
        Optional<InternApplication> internoptional = internService.getInternApplication(selectedInterns.get(0));
        group.setDomain(internoptional.get().getDomain());
        group.setGroupId(id);
        groupService.registerGroup(group);

        for (Long internId : selectedInterns) {
            Optional<InternApplication> internApplicationOptional = internService.getInternApplication(internId);

            if (internApplicationOptional.isPresent()) {
                InternApplication internApplication = internApplicationOptional.get();
                internApplication.setGroupCreated(true);
                internService.addInternApplication(internApplication);

                Intern intern = new Intern(internApplication.getFirstName(),
//                        internApplication.getLastName(),
                        internApplication.getContactNo(), internApplication.getEmail(),
                        internApplication.getCollegeName(), internApplication.getJoiningDate(),
                        internApplication.getCompletionDate(),
//                        internApplication.getBranch(),
                        internApplication.getDegree(),
                        internApplication.getPassword(), internApplication.getCollegeIcardImage(),
                        internApplication.getNocPdf(), internApplication.getResumePdf(), internApplication.getPassportSizeImage(),
                        internApplication.getSemester(), internApplication.getDomain(), group);

                intern.setInternId(generateInternId());
                internService.addIntern(intern);

                String username = (String) session.getAttribute("username");
                Admin admin = adminService.getAdminByUsername(username);
                if (admin != null) {
                    logService.saveLog(String.valueOf(admin.getAdminId()), "Created Group and Registered Intern",
                            "Admin " + admin.getName() + " created a group and registered intern "
                                    + internApplication.getFirstName());
                }
            }
        }
        return "redirect:/bisag/admin/create_group";
    }
    // Add dynamic fields(college, branch)

    @GetMapping("/add_fields")
    public ModelAndView addFields(Model model) {
        ModelAndView mv = new ModelAndView();
        List<College> colleges = fieldService.getColleges();
        List<Branch> branches = fieldService.getBranches();
        List<Domain> domains = fieldService.getDomains();
        List<Degree> degrees = fieldService.getDegrees();
        model = countNotifications(model);
        mv.addObject("colleges", colleges);
        mv.addObject("branches", branches);
        mv.addObject("domains", domains);
        mv.addObject("degrees", degrees);
        mv.addObject("admin", adminName(session));
        mv.setViewName("admin/add_fields");

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Accessed Add Fields",
                    "Admin " + admin.getName() + " accessed the add fields page.");
        }

        return mv;
    }

    @PostMapping("/add_college")
    public String addCollege(College college, Model model) {
        fieldService.addCollege(college);

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Added College",
                    "Admin " + admin.getName() + " added a new college: " + college.getName());
        }

        return "redirect:/bisag/admin/add_fields";
    }

    @PostMapping("/add_domain")
    public String addDomain(Domain domain, Model model) {
        fieldService.addDomain(domain);

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Added Domain",
                    "Admin " + admin.getName() + " added a new domain: " + domain.getName());
        }

        return "redirect:/bisag/admin/add_fields";
    }

    @PostMapping("/add_branch")
    public String addBranch(Branch branch, Model model) {
        fieldService.addBranch(branch);

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Added Branch",
                    "Admin " + admin.getName() + " added a new branch: " + branch.getName());
        }

        return "redirect:/bisag/admin/add_fields";
    }

    @PostMapping("/add_degree")
    public String addDegree(Degree degree, Model model) {
        fieldService.addDegree(degree);

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Added Degree",
                    "Admin " + admin.getName() + " added a new degree: " + degree.getName());
        }

        return "redirect:/bisag/admin/add_fields";
    }

    // Guide Allocation
    @GetMapping("/manage_group")
    public ModelAndView allocateGuide(Model model) {
        ModelAndView mv = new ModelAndView("admin/manage_group");
        List<GroupEntity> group = groupService.getGuideNotAllocatedGroup();
        model = countNotifications(model);
        mv.addObject("groups", group);
        mv.addObject("admin", adminName(session));
        return mv;
    }

    @GetMapping("/delete_college/{id}")
    public String deleteCollege(@PathVariable("id") long id) {
        fieldService.deleteCollege(id);
        return "redirect:/bisag/admin/add_fields";
    }

    @GetMapping("/delete_degree/{id}")
    public String deleteDegree(@PathVariable("id") long id) {
        fieldService.deleteDegree(id);
        return "redirect:/bisag/admin/add_fields";
    }

    @GetMapping("/delete_branch/{id}")
    public String deleteBranch(@PathVariable("id") long id, Model model) {
        fieldService.deleteBranch(id);
        return "redirect:/bisag/admin/add_fields";
    }

    @GetMapping("/delete_domain/{id}")
    public String deleteDomain(@PathVariable("id") long id, Model model) {
        fieldService.deleteDomain(id);
        return "redirect:/bisag/admin/add_fields";
    }

    @PostMapping("/update_college/{id}")
    public String updateCollege(@ModelAttribute("college") College college, @PathVariable("id") long id) {
        Optional<College> existingCollege = fieldService.getCollege(id);
        System.out.println("In college update section");
        if (existingCollege.isPresent()) {
            College updatedCollege = existingCollege.get();
            updatedCollege.setName(college.getName());
            updatedCollege.setLocation(college.getLocation());
            fieldService.updateCollege(updatedCollege);

            String username = (String) session.getAttribute("username");
            Admin admin = adminService.getAdminByUsername(username);
            if (admin != null) {
                logService.saveLog(String.valueOf(admin.getAdminId()), "Updated College",
                        "Admin " + admin.getName() + " updated college: " + updatedCollege.getName());
            }
        }
        return "redirect:/bisag/admin/add_fields";
    }

    @PostMapping("/update_branch/{id}")
    public String updateBranch(@ModelAttribute("branch") Branch branch, @PathVariable("id") long id) {
        Optional<Branch> existingBranch = fieldService.getBranch(id);

        if (existingBranch.isPresent()) {

            Branch updatedBranch = existingBranch.get();
            updatedBranch.setName(branch.getName());

            fieldService.updateBranch(updatedBranch);

            String username = (String) session.getAttribute("username");
            Admin admin = adminService.getAdminByUsername(username);
            if (admin != null) {
                logService.saveLog(String.valueOf(admin.getAdminId()), "Updated Branch",
                        "Admin " + admin.getName() + " updated branch: " + updatedBranch.getName());
            }
        }
        return "redirect:/bisag/admin/add_fields";
    }

    @PostMapping("/update_degree/{id}")
    public String updateDegree(@ModelAttribute("degree") Degree degree, @PathVariable("id") long id) {
        Optional<Degree> existingDegree = fieldService.getDegree(id);

        if (existingDegree.isPresent()) {
            Degree updatedDegree = existingDegree.get();
            updatedDegree.setName(degree.getName());
            fieldService.updateDegree(updatedDegree);

            String username = (String) session.getAttribute("username");
            Admin admin = adminService.getAdminByUsername(username);
            if (admin != null) {
                logService.saveLog(String.valueOf(admin.getAdminId()), "Updated Degree",
                        "Admin " + admin.getName() + " updated degree: " + updatedDegree.getName());
            }
        }
        return "redirect:/bisag/admin/add_fields";
    }

    @PostMapping("/update_domain/{id}")
    public String updateDomain(@ModelAttribute("domain") Domain domain, @PathVariable("id") long id) {
        Optional<Domain> existingDomain = fieldService.getDomain(id);

        if (existingDomain.isPresent()) {
            Domain updatedDomain = existingDomain.get();
            updatedDomain.setName(domain.getName());
            fieldService.updateDomain(updatedDomain);

            String username = (String) session.getAttribute("username");
            Admin admin = adminService.getAdminByUsername(username);
            if (admin != null) {
                logService.saveLog(String.valueOf(admin.getAdminId()), "Updated Domain",
                        "Admin " + admin.getName() + " updated domain: " + updatedDomain.getName());
            }
        }
        return "redirect:/bisag/admin/add_fields";
    }
    // ----------------------------------- Guide registration

    @GetMapping("/register_guide")
    public ModelAndView registerGuide(Model model) {
        ModelAndView mv = new ModelAndView("admin/guide_registration");

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Visited Guide Registration Page",
                    "Admin " + admin.getName() + " visited the guide registration page.");
        }

        model = countNotifications(model);
        mv.addObject("admin", adminName(session));
        return mv;
    }

    @PostMapping("/register_guide")
    public String registerGuide(@ModelAttribute("guide") Guide guide) {
        guideService.registerGuide(guide);

        emailService.sendSimpleEmail(guide.getEmailId(), "Notification: Appointment as Administrator\r\n" + "\r\n"
                        + "Dear " + guide.getName() + "\r\n" + "\r\n"
                        + "I trust this email finds you well. We are pleased to inform you that you have been appointed as an administrator within our organization, effective immediately. Your dedication and contributions to the team have not gone unnoticed, and we believe that your new role will bring value to our operations.\r\n"
                        + "\r\n"
                        + "As an administrator, you now hold a position of responsibility within the organization. We trust that you will approach your duties with diligence, professionalism, and a commitment to upholding the values of our organization.\r\n"
                        + "\r\n"
                        + "It is imperative to recognize the importance of your role and the impact it may have on the functioning of our team. We have confidence in your ability to handle the responsibilities that come with this position and to contribute positively to the continued success of our organization.\r\n"
                        + "\r\n"
                        + "We would like to emphasize the importance of maintaining the highest standards of integrity and ethics in your role. It is expected that you will use your administrative privileges responsibly and refrain from any misuse.\r\n"
                        + "\r\n"
                        + "Should you have any questions or require further clarification regarding your new responsibilities, please do not hesitate to reach out to [Contact Person/Department].\r\n"
                        + "\r\n"
                        + "Once again, congratulations on your appointment as an administrator. We look forward to your continued contributions and success in this elevated role.\r\n"
                        + "\r\n" + "Best regards,\r\n" + "\r\n" + "Your Colleague,\r\n" + "Administrator\r\n" + "1231231231",
                "BISAG ADMINISTRATIVE OFFICE");

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Registered Guide",
                    "Admin " + admin.getName() + " registered guide " + guide.getName() + " as an administrator.");
        }

        return "redirect:/bisag/admin/guide_list";
    }

    @GetMapping("/guide_list")
    public ModelAndView guideList(Model model) {
        ModelAndView mv = new ModelAndView("admin/guide_list");
        List<Guide> guides = guideService.getGuide();
        model = countNotifications(model);
        mv.addObject("guides", guides);
        mv.addObject("admin", adminName(session));

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Guide List",
                    "Admin " + admin.getName() + " viewed the guide list.");
        }
        return mv;
    }

    @GetMapping("/guide_list/{id}")
    public ModelAndView guideList(@PathVariable("id") long id, Model model) {
        System.out.println("id" + id);
        ModelAndView mv = new ModelAndView();
        Optional<Guide> guide = guideService.getGuide(id);
        model = countNotifications(model);
        mv.addObject("guide", guide);
        mv.setViewName("admin/guide_list_detail");

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Guide Details",
                    "Admin " + admin.getName() + " viewed details of guide with ID: " + id);
        }

        return mv;
    }

    @GetMapping("/update_guide/{id}")
    public ModelAndView updateGuide(@PathVariable("id") long id, Model model) {
        ModelAndView mv = new ModelAndView("admin/update_guide");
        Optional<Guide> guide = guideService.getGuide(id);
        model = countNotifications(model);
        mv.addObject("guide", guide.orElse(new Guide()));

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Accessed Guide Update Page",
                    "Admin " + admin.getName() + " accessed the update page for guide with ID: " + id);
        }

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
            if (!currentPassword.equals(encodePassword(guide.getPassword())) && guide.getPassword() != "") {
                updatedGuide.setPassword(encodePassword(guide.getPassword()));
            }
            guideService.updateGuide(updatedGuide, existingGuide);

            String username = (String) session.getAttribute("username");
            Admin admin = adminService.getAdminByUsername(username);
            if (admin != null) {
                logService.saveLog(String.valueOf(admin.getAdminId()), "Updated Guide Information",
                        "Admin " + admin.getName() + " updated the guide information with ID: " + id);
            }
        }
        return "redirect:/bisag/admin/guide_list";
    }

    // Delete Guide
    @PostMapping("/guide_list/delete/{id}")
    public String deleteGuide(@PathVariable("id") long id) {
        guideService.deleteGuide(id);

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Deleted Guide",
                    "Admin " + admin.getName() + " deleted the guide with ID: " + id);
        }

        return "redirect:/bisag/admin/guide_list";
    }

    // Manage group
    @GetMapping("/allocate_guide")
    public ModelAndView manageGroup(Model model) {
        ModelAndView mv = new ModelAndView();
        List<GroupEntity> allocatedGroups = groupService.getAllocatedGroups();
        List<GroupEntity> notAllocatedGroups = groupService.getNotAllocatedGroups();
        List<Intern> interns = internService.getInterns();
        List<Guide> guides = guideService.getGuide();
        model = countNotifications(model);
        mv.setViewName("/admin/allocate_guide");
        mv.addObject("alloactedGroups", allocatedGroups);
        mv.addObject("notAllocatedGroups", notAllocatedGroups);
        mv.addObject("guides", guides);
        mv.addObject("interns", interns);
        mv.addObject("admin", adminName(session));
        return mv;
    }

    // Manage group details
    @GetMapping("/allocate_guide/{id}")
    public ModelAndView manageGroup(@PathVariable("id") String id, Model model) {
        ModelAndView mv = new ModelAndView();
        GroupEntity group = groupService.getGroup(id);
        List<Intern> interns = internService.getInterns();
        List<Guide> guides = guideService.getGuide();
        mv.setViewName("/admin/manage_group_detail");
        model = countNotifications(model);
        mv.addObject("groups", group);
        mv.addObject("guides", guides);
        mv.addObject("interns", interns);
        return mv;
    }

    @PostMapping("/allocate_guide/assign_guide")
    public String assignGuide(@RequestParam("guideid") long guideid, @RequestParam("groupId") String groupId) {
        System.out.println("guide id: " + guideid);
        groupService.assignGuide(groupId, guideid);

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Assigned Guide to Group",
                    "Admin " + admin.getName() + " assigned guide with ID: " + guideid + " to group with ID: " + groupId);
        }

        return "redirect:/bisag/admin/allocate_guide";
    }

    // Project Definition Approvals
    @GetMapping("/admin_pending_def_approvals")
    public ModelAndView pendingFromGuide(Model model) {
        ModelAndView mv = new ModelAndView("/admin/admin_pending_def_approvals");
        List<GroupEntity> groups = groupService.getAPendingGroups();
        List<GroupEntity> pendingGroups = groupRepo.findByProjectDefinitionStatus("gupending");
        // Add the fetched groups to the model
        model.addAttribute("groups", pendingGroups);
        model = countNotifications(model);

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Accessed Pending Project Definitions",
                    "Admin " + admin.getName() + " accessed the pending project definition approvals page.");
        }

        mv.addObject("groups", groups);
        mv.addObject("admin", adminName(session));
        return mv;
    }

    @PostMapping("/admin_pending_def_approvals/{groupId}")
    public String pendingFromAdmin(@RequestParam("apendingAns") String apendingAns,
                                   @PathVariable("groupId") String groupId,
                                   @RequestParam("projectDefinition") String projectDefinition,
                                   @RequestParam("description") String description) {

        GroupEntity group = groupService.getGroup(groupId);
        if (group != null) {
            group.setProjectDefinition(projectDefinition);
            group.setDescription(description);

            if (apendingAns.equals("approve")) {
                group.setProjectDefinitionStatus("approved");
                List<Intern> interns = internService.getInternsByGroupId(group.getId());
                for (Intern intern : interns) {
                    intern.setProjectDefinitionName(group.getProjectDefinition());
                    internRepo.save(intern);
                }

                String username = (String) session.getAttribute("username");
                Admin admin = adminService.getAdminByUsername(username);
                if (admin != null) {
                    logService.saveLog(String.valueOf(admin.getAdminId()), "Project Definition Approved",
                            "Admin " + admin.getName() + " approved project definition for group with ID: " + groupId);
                }

            } else {
                group.setProjectDefinitionStatus("pending");
            }
            groupRepo.save(group);
        } else {

        }
        return "redirect:/bisag/admin/admin_pending_def_approvals";
    }

    @GetMapping("admin_weekly_report")
    public ModelAndView weeklyReport(Model model) {
        ModelAndView mv = new ModelAndView("/admin/admin_weekly_report");
        List<GroupEntity> groups = groupService.getAllocatedGroups();
        List<WeeklyReport> reports = weeklyReportService.getAllReports();
        groups.sort(Comparator.comparing(GroupEntity::getGroupId));
        model = countNotifications(model);
        mv.addObject("groups", groups);
        mv.addObject("reports", reports);
        mv.addObject("admin", adminName(session));

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Accessed Weekly Reports",
                    "Admin " + admin.getName() + " accessed the weekly reports page.");
        }

        return mv;
    }

    @GetMapping("/admin_weekly_report_details/{groupId}/{weekNo}")
    public ModelAndView changeWeeklyReportSubmission(@PathVariable("groupId") String groupId, @PathVariable("weekNo") int weekNo, Model model) {
        ModelAndView mv = new ModelAndView("/admin/admin_weekly_report_details");
        model = countNotifications(model);
        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        GroupEntity group = groupService.getGroup(groupId);
        WeeklyReport report = weeklyReportService.getReportByWeekNoAndGroupId(weekNo, group);
        MyUser user = myUserService.getUserByUsername(admin.getEmailId());

        if (user.getRole().equals("ADMIN")) {
            String name = admin.getName();
            mv.addObject("replacedBy", name);

            logService.saveLog(String.valueOf(admin.getAdminId()), "Accessed Weekly Report Details",
                    "Admin " + admin.getName() + " accessed weekly report details for group " + groupId + " and week " + weekNo);
        } else if (user.getRole().equals("INTERN")) {
            Intern intern = internService.getInternByUsername(user.getUsername());
            mv.addObject("replacedBy", intern.getFirstName());
        } else {

        }

        mv.addObject("report", report);
        mv.addObject("group", group);

        return mv;
    }

    @GetMapping("/admin_weekly_report_form")
    public ModelAndView showWeeklyReportForm(Model model) {
        ModelAndView mv = new ModelAndView("/admin/admin_weekly_report_form");

        List<GroupEntity> groups = groupService.getAllocatedGroups();
        List<Intern> interns = internService.getAllInterns();

        model.addAttribute("groups", groups);
        model.addAttribute("interns", interns);
        model = countNotifications(model);

        mv.addObject("admin", adminName(session));
        mv.addObject("groups", groups);

        // Fetch Admin Details
        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Accessed Weekly Report Form",
                    "Admin " + admin.getName() + " accessed the weekly report submission form.");
        }

        return mv;
    }

    @PostMapping("/admin_weekly_report_form")
    public String submitWeeklyReport(@RequestParam("groupId") Long groupId,
                                     @RequestParam("internId") Intern internId,
                                     @RequestParam("guide") Guide guide,
                                     @RequestParam("weekNo") int weekNo,
                                     @RequestParam("deadline") String deadlineString,
                                     @RequestParam("status") String status,
                                     @RequestParam("submittedPdf") MultipartFile submittedPdf) {

        // Convert String to Date
        Date deadline = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            deadline = dateFormat.parse(deadlineString);
        } catch (ParseException e) {
            e.printStackTrace(); // Log error
            return "redirect:/bisag/admin/admin_weekly_report_form?error";
        }

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);

        weeklyReportService.submitAdminWeeklyReport(groupId, internId, guide, weekNo, deadline, status, submittedPdf);

        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Submitted Weekly Report",
                    "Admin " + admin.getName() + " submitted a weekly report for Group ID: " + groupId + ", Week No: " + weekNo);
        }

        return "redirect:/bisag/admin/admin_weekly_report_form?success";
    }


    //    @GetMapping("/admin_weekly_report_details/{groupId}/{weekNo}")
//    public ModelAndView changeWeeklyReportSubmission(
//            @PathVariable("groupId") String groupId,
//            @PathVariable("weekNo") int weekNo,
//            Model model) {
//        ModelAndView mv = new ModelAndView("/admin/admin_weekly_report_details");
//        model = countNotifications(model);
//
//        Admin admin = getSignedInAdmin();
//        GroupEntity group = groupService.getGroup(groupId);
//
//        if (group != null) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found" + groupId);
//        }
//
//        WeeklyReport report = weeklyReportService.getReportByWeekNoAndGroupId(weekNo, group);
//
//        if (report != null) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Weekly Report not found for group:" + groupId);
//        }
//
//        MyUser user = myUserService.getUserByUsername(admin.getEmailId());
//        if (user.getRole().equals("ADMIN")) {
//            String name = admin.getName();
//            mv.addObject("replacedBy", name);
//        } else if (user.getRole().equals("INTERN")) {
//            Intern intern = internService.getInternByUsername(user.getUsername());
//            mv.addObject("replacedBy", intern.getFirstName() + intern.getLastName());
//        }
//
//        mv.addObject("report", report);
//        mv.addObject("group", group);
//
//        return mv;
//    }
    @GetMapping("/admin_yearly_report")
    public String getReportsByYear(@RequestParam(value = "date", required = true) String selectedDate, Model model) {
        int year = 0;
        List<WeeklyReport> reports = null;

        if (selectedDate != null && !selectedDate.isEmpty()) {
            LocalDate date = LocalDate.parse(selectedDate, DateTimeFormatter.ISO_DATE);
            year = date.getYear();
            reports = weeklyReportService.getReportsByYear(year);

            if (reports == null || reports.isEmpty()) {
                model.addAttribute("message", "Report not found for the year " + year);
            }
        } else {
            reports = weeklyReportService.getAllReports();
        }

        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("year", year);
        model.addAttribute("reports", reports);

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);

        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Accessed Yearly Reports",
                    "Admin " + admin.getName() + " accessed the yearly reports page for the year " + year);
        }

        return "admin/admin_yearly_report";
    }

// -_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_View report -_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-

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
            mv.addObject("replacedBy", intern.getFirstName());
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

//intern cancellation request
    @GetMapping("/intern_cancellation_request")
    public String viewFinalApprovals(Model model) {
        List<Intern> reviewedByGuide = internService.getInternsByCancellationStatus("gapproved");
        model.addAttribute("guideReviewedList", reviewedByGuide);
        return "admin/intern_cancellation_request";
    }

    @PostMapping("/finalApproveCancellation")
    public String finalApproveCancellation(@RequestParam String internId) {
        Intern intern = internService.getInternById(internId);
        intern.setCancellationStatus("cancelled"); // final status
        internService.updateCancellationStatus(intern);

        logService.saveLog(intern.getInternId(), "Admin Approved Cancellation",
                "Admin approved cancellation request for intern.");
        return "redirect:/bisag/admin/intern_cancellation_request";
    }

    @PostMapping("/finalRejectCancellation")
    public String finalRejectCancellation(@RequestParam String internId) {
        Intern intern = internService.getInternById(internId);
        intern.setCancellationStatus("arejected"); // or use "rejected" based on your logic
        internService.updateCancellationStatus(intern);

        logService.saveLog(intern.getInternId(), "Admin Rejected Cancellation",
                "Admin rejected cancellation request for intern.");

        return "redirect:/bisag/admin/intern_cancellation_request";
    }


    @GetMapping("/query_to_guide")
    public ModelAndView queryToGuide(Model model) {
        ModelAndView mv = new ModelAndView("/admin/query_to_guide");
        List<Admin> admins = adminService.getAdmin();
        List<Guide> guides = guideService.getGuide();
        List<Intern> interns = internService.getInterns();
        List<GroupEntity> groups = groupService.getAllocatedGroups();
        model = countNotifications(model);

        Admin admin = getSignedInAdmin();
        logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Chat",
                "Admin " + admin.getName() + " viewed chat page.");

        mv.addObject("groups", groups);
        mv.addObject("interns", interns);
        mv.addObject("admins", admins);
        mv.addObject("guides", guides);
        mv.addObject("admin", adminName(session));
        return mv;
    }


//     old final report code commented
//    @GetMapping("/admin_pending_final_reports")
//    public ModelAndView adminPendingFinalReports(Model model) {
//        ModelAndView mv = new ModelAndView("/admin/admin_pending_final_reports");
////        List<GroupEntity> groups = groupService.getAPendingFinalReports();
//        model = countNotifications(model);
//
//        Admin admin = getSignedInAdmin();
//        logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Pending Final Reports",
//                "Admin " + admin.getName() + " viewed pending final reports.");
//
//        List<GroupEntity> groups = groupService.getAdminPendingFinalReports("gapproved");
//        mv.addObject("groups", groups);
//        mv.addObject("admin", adminName(session));
//        return mv;
//    }


    @GetMapping("/admin_pending_final_reports")
    public ModelAndView adminPendingFinalReports(Model model) {
        ModelAndView mv = new ModelAndView("/admin/admin_pending_final_reports");

        model = countNotifications(model);
        Admin admin = getSignedInAdmin();

        logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Pending Final Reports",
                "Admin " + admin.getName() + " viewed pending final reports.");

        // Fetch guide-approved reports pending for admin approval
        List<GroupEntity> groups = groupService.getAdminPendingFinalReportsStatus();

        mv.addObject("groups", groups);
        mv.addObject("admin", adminName(session));
        return mv;
    }

    @PostMapping("/admin_pending_final_reports/ans")
    public String adminFinalReportApproval(@RequestParam("adminFinalAns") String adminFinalAns,
                                           @RequestParam("groupId") String groupId) {

        GroupEntity group = groupService.getGroup(groupId);
        Admin admin = getSignedInAdmin();

        if ("approve".equalsIgnoreCase(adminFinalAns)) {
            group.setAdminFinalReportStatus("approved");
            group.setFinalReportStatus("approved"); // Ensure final report is also approved
            logService.saveLog(String.valueOf(admin.getAdminId()), "Final Report Approved",
                    "Admin " + admin.getName() + " approved the final report for Group ID: " + groupId);
        } else {
            group.setAdminFinalReportStatus("rejected");
            group.setFinalReportStatus("pending"); // Reset final report status if rejected
            logService.saveLog(String.valueOf(admin.getAdminId()), "Final Report Rejected",
                    "Admin " + admin.getName() + " rejected the final report for Group ID: " + groupId);
        }

        groupRepo.save(group);
        return "redirect:/bisag/admin/admin_pending_final_reports";
    }

    @GetMapping("/viewFinalReport/{groupId}")
    public ResponseEntity<byte[]> viewFinalReport(@PathVariable String groupId) {
        Admin admin = getSignedInAdmin();

        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Final Report",
                    "Guide " + admin.getName() + " viewed the Final Report for Group ID: " + groupId);
        }

        // Fetch the group entity
        GroupEntity group = groupService.getGroup(groupId);

        if (group == null || group.getFinalReport() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        byte[] pdfContent = group.getFinalReport(); // Get PDF as byte[]
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }



    @GetMapping("/weekly-reports/pending")
    public String getPendingReports(Model model) {
        Map<String, Integer> pendingReports = weeklyReportService.getPendingReports();
        model.addAttribute("pendingReports", pendingReports);
        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        logService.saveLog(String.valueOf(admin.getAdminId()), "Weekly Report Form Access", "Admin " + admin.getName() + " accessed the Add Weekly Report form.");

        return "admin/pending-weekly-reports";
    }

    //-------------------------------- Leave Application Module------------------------------------
    @GetMapping("/manage_leave_applications")
    public ModelAndView manageLeaveApplications(Model model, HttpSession session) {
        ModelAndView mv = new ModelAndView("/admin/manage_leave_applications");
        List<Intern> interns = internService.getInterns();
        List<LeaveApplication> leaveApplications = leaveApplicationService.getAllLeaveApplications();
        model = countNotifications(model);

        // Fetch Admin Details
        Admin admin = getSignedInAdmin();

        // Log Access Action
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Leave Applications",
                    "Admin " + admin.getName() + " viewed all leave applications.");
        }

        mv.addObject("interns", interns);
        mv.addObject("leaveApplications", leaveApplications);
        mv.addObject("admin", adminName(session));

        return mv;
    }

    @GetMapping("/manage_leave_applications_details/{id}")
    public ModelAndView manageLeaveApplicationsDetails(@PathVariable("id") String id, Model model, HttpSession session) {
        ModelAndView mv = new ModelAndView("/admin/manage_leave_applications_details");
        List<Intern> interns = internService.getInterns();
        List<LeaveApplication> leaveApplications = leaveApplicationService.getLeaveApplicationsByInternId(id);
        model = countNotifications(model);

        // Fetch Admin Details
        Admin admin = getSignedInAdmin();

        // Log Access Action
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Leave Application Details",
                    "Admin " + admin.getName() + " viewed leave application details for intern ID " + id);
        }

        mv.addObject("interns", interns);
        mv.addObject("leaveApplications", leaveApplications);
        mv.addObject("admin", adminName(session));

        return mv;
    }





//----------------------------------------------generate----------------------------------------------generate Report ---------------------------------------------------





    @GetMapping("/generate_intern_report")
    public ModelAndView generateInternReport(Model model) {
        ModelAndView mv = new ModelAndView("admin/generate_intern_report");

        List<College> college = fieldService.getColleges();
//        List<Branch> branch = fieldService.getBranches();
        List<Domain> domain = fieldService.getDomains();
        List<Guide> guide = guideService.getGuide();
        List<Degree> degree = fieldService.getDegrees();
        List<GroupEntity> groupEntities = groupService.getGroups();
        List<String> projectDefinitions = internService.getDistinctProjectDefinitions();
        List<Intern> interns = internService.getAllInterns();
        List<String> genders = internService.getDistinctGenders();

        Admin admin = getSignedInAdmin();
        logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Generate Intern Report Page",
                "Admin " + admin.getName() + " viewed the Generate Intern Report page.");

        model = countNotifications(model);
        mv.addObject("interns", interns);
        mv.addObject("project_definition_name", projectDefinitions);
        mv.addObject("colleges", college);
//        mv.addObject("branches", branch);
        mv.addObject("domains", domain);
        mv.addObject("guides", guide);
        mv.addObject("degrees", degree);
        mv.addObject("genders", genders);
        mv.addObject("admin", adminName(session));

        return mv;
    }

    @PostMapping("/generate_intern_report")
    public String generateInternReport(HttpServletResponse response, @ModelAttribute("ReportFilter") ReportFilter reportFilter) throws Exception {
        College college;
//        Branch branch;
        Optional<Guide> guide;
        Domain domain;
        Degree degree;

        // Log filter details before applying the filter
        Admin admin = getSignedInAdmin();
        logService.saveLog(String.valueOf(admin.getAdminId()), "Applied Report Filters",
                "Admin " + admin.getName() + " applied the following filters: College = " + reportFilter.getCollege() +
                        ", Guide = " + reportFilter.getGuide() + ", Domain = " + reportFilter.getDomain() + ", Degree = " + reportFilter.getDegree() +
                        ", Date Range = " + reportFilter.getStartDate() + " to " + reportFilter.getEndDate());

//        if (reportFilter.getBranch().equals("All")) {
//            reportFilter.setBranch(null);
//        } else {
//            branch = fieldService.findByBranchName(reportFilter.getBranch());
//        }

        if (reportFilter.getCollege().equals("All")) {
            reportFilter.setCollege(null);
        } else {
            college = fieldService.findByCollegeName(reportFilter.getCollege());
        }

        if (reportFilter.getGuide().equals("All")) {
            guide = null;
        } else {
            guide = guideService.getGuideByName(reportFilter.getGuide());
        }

        if (reportFilter.getDomain().equals("All")) {
            reportFilter.setDomain(null);
        } else {
            domain = fieldService.getDomainByName(reportFilter.getDomain());
        }

        if (reportFilter.getDegree().equals("All")) {
            reportFilter.setDegree(null);
        } else {
            degree = fieldService.findByDegreeName(reportFilter.getDegree());
        }

        List<Intern> filteredInterns = internService.getFilteredInterns(reportFilter.getCollege(),
                guide, reportFilter.getDomain(), reportFilter.getCancelled(),
                reportFilter.getStartDate(), reportFilter.getEndDate(), reportFilter.getCancelled());

        logService.saveLog(String.valueOf(admin.getAdminId()), "Filtered Interns for Report",
                "Admin " + admin.getName() + " filtered " + filteredInterns.size() + " interns based on the selected criteria.");

        for (Intern intern : filteredInterns) {
            System.out.println(intern.getFirstName());
        }

        // Exporting the report in the selected format (PDF or Excel)
        if (reportFilter.getFormat().equals("pdf")) {
            dataExportService.exportToPdf(filteredInterns, response);
            logService.saveLog(String.valueOf(admin.getAdminId()), "Exported Intern Report (PDF)",
                    "Admin " + admin.getName() + " exported the intern report in PDF format.");
        } else {
            dataExportService.exportToExcel(filteredInterns, response);
            logService.saveLog(String.valueOf(admin.getAdminId()), "Exported Intern Report (Excel)",
                    "Admin " + admin.getName() + " exported the intern report in Excel format.");
        }

        return "redirect:/bisag/admin/admin_dashboard";
    }













//----------------------------------------------generate----------------------------------------------generate Report ---------------------------------------------------















    @PostMapping("/change_password")
    public String changePassword(@RequestParam("newPassword") String newPassword) {
        Admin admin = getSignedInAdmin();

        logService.saveLog(String.valueOf(admin.getAdminId()), "Password Change Request",
                "Admin " + admin.getName() + " requested a password change.");

        adminService.changePassword(admin, newPassword);

        logService.saveLog(String.valueOf(admin.getAdminId()), "Password Changed Successfully",
                "Admin " + admin.getName() + " successfully changed their password.");

        return "redirect:/logout";
    }

    //-------------------------- View all thesis records-------------------------------
    //---------------------------------------------------------------------------------
//    @GetMapping("/thesis")
//    public String viewThesisList(Model model) {
//        List<Thesis> thesisList = thesisService.getAllTheses();
//        model.addAttribute("theses", thesisList);
//        return "thesis_list";
//    }

    public AdminController(ThesisService thesisService) {
        this.thesisService = thesisService;
    }

    // Show form to add a new thesis
    @GetMapping("thesis/new")
    public String showThesisForm(Model model) {
        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        model.addAttribute("thesis", new Thesis());

        logService.saveLog(String.valueOf(admin.getAdminId()), "Thesis Form Access", "Admin " + admin.getName() + " accessed the 'Add Thesis' form.");

        return "admin/thesis_form";
    }

    // Handle adding/updating a thesis record
    @PostMapping("thesis/save")
    public String saveThesis(@ModelAttribute("thesis") Thesis thesis, RedirectAttributes redirectAttributes) {
        try {
            Admin admin = getSignedInAdmin();

            thesisService.saveThesis(thesis);

            if (admin != null) {
                logService.saveLog(String.valueOf(admin.getAdminId()), "Thesis Saved",
                        "Admin " + admin.getName() + " added or updated a thesis with ID: " + thesis.getId());
            }

            // Set success message
            redirectAttributes.addFlashAttribute("successMessage", "Thesis record submitted successfully!");
        } catch (Exception e) {
            // Set error message
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving thesis record. Please try again.");
        }

        return "redirect:/bisag/admin/thesis_list";
    }

    // Show all thesis records
    @GetMapping("/thesis_list")
    public String getThesisList(Model model) {
        List<Thesis> thesisList = thesisService.getAllTheses();
        model.addAttribute("thesisList", thesisList);
        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        logService.saveLog(String.valueOf(admin.getAdminId()), "Thesis List View", "Admin " + admin.getName() + " viewed the list of thesis.");

        return "admin/thesis_list";
    }

    @GetMapping("/thesis/update/{id}")
    public String showUpdatePage(@PathVariable Long id, Model model) {
        Thesis thesis = thesisService.getThesisById(id)
                .orElseThrow(() -> new RuntimeException("Thesis not found with ID: " + id));

        model.addAttribute("thesis", thesis);

        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        if (admin != null) {
            String adminId = String.valueOf(admin.getAdminId());
            logService.saveLog(adminId, "Accessed Thesis Update Page",
                    "Admin " + admin.getName() + " accessed the update page for thesis ID: " + id);
        }

        return "admin/update_thesis";
    }

    @PostMapping("/thesis/update/{id}")
    public String updateThesis(@PathVariable Long id,
                               @RequestParam String actualReturnDate,
                               @RequestParam String location,
                               RedirectAttributes redirectAttributes) {
        java.sql.Date returnDate = java.sql.Date.valueOf(actualReturnDate);
        thesisService.updateThesisReturnDateAndLocation(id, returnDate, location);

        Admin admin = getSignedInAdmin();
        if (admin != null) {
            String adminId = String.valueOf(admin.getAdminId());
            logService.saveLog(adminId, "Updated Thesis Return Date & Location",
                    "Admin " + admin.getName() + " updated thesis ID: " + id +
                            " with Actual Return Date: " + actualReturnDate + " and Location: " + location);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Thesis details updated successfully!");
        return "redirect:/bisag/admin/thesis_list";
    }
    //Show thesis ID wise-------------------
    @GetMapping("/thesis_list_detail/{id}")
    public String getThesisDetails(@PathVariable("id") String id, Model model) {
        Optional<Thesis> thesis = thesisService.getThesisById(Long.parseLong(id));
        if (thesis.isPresent()) {
            model.addAttribute("thesis", thesis);
            Admin admin = getSignedInAdmin();
            model.addAttribute("admin", admin);


            logService.saveLog(String.valueOf(admin.getAdminId()), "Thesis Details View",
                    "Admin " + admin.getName() + " viewed the details of thesis with ID: " + id);
            return "admin/thesis_list_detail";
        } else {
            return "error/404";
        }
    }

    // Show rejected interns in the logs
    @GetMapping("/logs")
    public String showRejectedInterns(Model model) {
        List<InternApplication> rejectedInterns = internApplicationService.getRejectedInterns();

        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        String id = String.valueOf(admin.getAdminId());

        logService.saveLog(id, "Viewed Rejected Interns", "Admin " + admin.getName() + " accessed the rejected interns list.");

        model.addAttribute("rejectedInterns", rejectedInterns);
        return "admin/logs";
    }

    // -_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_Get activity logs-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_
    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    @GetMapping("/activity_logs")
    public String getInternActivityLogs(Model model) {
        List<Log> logs = logService.getAllLogs();
        if (logs == null) {
            logs = new ArrayList<>();
        }
        System.out.println("Logs fetched: " + logs.size());

        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        String id = String.valueOf(admin.getAdminId());

        logInternAction(id, "Viewed Activity Logs", "Admin " + admin.getName() + " accessed the activity logs.");

        model.addAttribute("logs", logs);
        return "admin/activity_logs";
    }

    public void logInternAction(String internId, String action, String details) {
        logService.saveLog(internId, action, details);
    }

    public void submitReport(String internId, String reportId) {

        logInternAction(internId, "Submitted Weekly Report", "Report ID: " + reportId);
    }

    public void updateProfile(String internId, String newEmail, String newPhone) {

        logInternAction(internId, "Updated Profile", "Changed Email: " + newEmail + ", Phone: " + newPhone);
    }

    // ========================== COMPANY VERIFICATION REQUESTS ==========================
    @PostMapping("/send_to_hr")
    public String sendToHR(@RequestParam("internId") String internId, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "Intern ID " + internId + " sent to HR successfully.");
        return "redirect:/bisag/admin/approved-verifications";
    }
    //View all pending verification requests
    @GetMapping("/verification_requests")
    public ModelAndView viewVerificationRequests(Model model, HttpSession session) {
        ModelAndView mv = new ModelAndView("admin/verification_requests");

        List<Verification> pendingRequests = verificationService.getPendingRequests();
        mv.addObject("requests", pendingRequests);
        model = countNotifications(model);
        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        String id = String.valueOf(admin.getAdminId());

        logService.saveLog(id, "Viewed Verification Requests",
                "Admin " + admin.getName() + " accessed the list of pending verification requests.");

        return mv;
    }

    // View details of a specific verification request
    @GetMapping("/verify/{id}")
    public String viewVerificationDetails(@PathVariable("id") long id, Model model) {

        Optional<Verification> verification = verificationService.getVerificationById(id);

        if (verification.isPresent()) {

            Admin admin = getSignedInAdmin();
            model.addAttribute("admin", admin);

            String adminId = String.valueOf(admin.getAdminId());
            logService.saveLog(adminId, "Verification Details View",
                    "Admin " + admin.getName() + " viewed the details of verification request with ID: " + id);

            model.addAttribute("verification", verification.get());

            return "admin/verification_detail";
        } else {
            model.addAttribute("errorMessage", "Verification request not found.");
            return "error/404";
        }
    }

    @PostMapping("/approve/{id}")
    public String approveVerification(@PathVariable("id") long id,
                                      @RequestParam(value = "remarks", required = false) String remarks,
                                      RedirectAttributes redirectAttributes) {
        Optional<Verification> verification = verificationService.getVerificationById(id);
        if (verification.isPresent()) {
            Verification v = verification.get();

            Admin admin = getSignedInAdmin();
            String adminId = String.valueOf(admin.getAdminId());
            verificationService.approveVerification(id, adminId, remarks);
            logService.saveLog(adminId, "Verification Approved",
                    "Admin " + admin.getName() + " approved the verification request with ID: " + id);

            redirectAttributes.addFlashAttribute("successMessage", "Verification request approved successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Verification request not found.");
        }
        return "redirect:/bisag/admin/approved-verifications";
    }

    @PostMapping("/reject/{id}")
    public String rejectVerification(@PathVariable("id") long id,
                                     @RequestParam(value = "remarks", required = false) String remarks,
                                     RedirectAttributes redirectAttributes) {
        Optional<Verification> verification = verificationService.getVerificationById(id);
        if (verification.isPresent()) {
            Verification v = verification.get();
            Admin admin = getSignedInAdmin();
            String adminId = String.valueOf(admin.getAdminId());
            verificationService.rejectVerification(id, adminId, remarks);
            logService.saveLog(adminId, "Verification Rejected",
                    "Admin " + admin.getName() + " rejected the verification request with ID: " + id);
            redirectAttributes.addFlashAttribute("successMessage", "Verification request rejected successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Verification request not found.");
        }
        return "redirect:/bisag/admin/rejected-verifications";
    }

    //Form for companies to submit verification requests
    @GetMapping("/verification_request_form")
    public ModelAndView verificationRequestForm() {
        Admin admin = getSignedInAdmin();
        if (admin == null) {
            return new ModelAndView("redirect:/bisag/admin/login");
        }

        String id = String.valueOf(admin.getAdminId());
        logService.saveLog(id, "Viewed Verification Request Form", "Admin " + admin.getName() + " accessed the verification request form.");

        ModelAndView modelAndView = new ModelAndView("admin/verification_request_form");
        modelAndView.addObject("admin", admin);

        return modelAndView;
    }


    //for verification module
    @GetMapping("/get-intern-details/{internId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getInternDetails(@PathVariable String internId) {
        Intern intern = internService.getInternById(internId);

        if (intern != null) {
            Map<String, String> internDetails = new HashMap<>();
            internDetails.put("internName", intern.getFirstName());
            internDetails.put("internContact", intern.getContactNo());

            Admin admin = getSignedInAdmin();

            if (admin != null) {
                logService.saveLog(String.valueOf(admin.getAdminId()), "Fetched Intern Details",
                        "Admin " + admin.getName() + " fetched details for Intern ID " + internId);
            }

            return ResponseEntity.ok(internDetails);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @GetMapping("/intern_verification_details/{id}")
    public ModelAndView internverificationDetails(@PathVariable("id") String id, Model model) {
        ModelAndView mv = new ModelAndView();

        Optional<Intern> intern = internService.getIntern(id);
        model = countNotifications(model);

        mv.addObject("intern", intern);

        List<College> colleges = fieldService.getColleges();
        List<Domain> domains = fieldService.getDomains();
//        List<Branch> branches = fieldService.getBranches();
        List<GroupEntity> groups = groupService.getGroups();

        mv.addObject("colleges", colleges);
        mv.addObject("domains", domains);
//        mv.addObject("branches", branches);
        mv.addObject("groups", groups);

        mv.setViewName("admin/intern_verification_details");

        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);
        if (admin != null && intern.isPresent()) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Intern Verification Details",
                    "Admin " + admin.getName() + " viewed the details of intern ID: " + id + ", Name: " + intern.get().getFirstName());
        } else {
            System.out.println("Error: Admin or Intern not found for logging!");
        }

        return mv;
    }

    //For Verification Module
    @GetMapping("/get-intern-details-by-name/{internName}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getInternDetailsByName(@PathVariable String internName) {
        Intern intern = internService.getInternByName(internName);

        if (intern != null) {
            Map<String, String> internDetails = new HashMap<>();
            internDetails.put("internId", intern.getInternId());
            internDetails.put("internContact", intern.getContactNo());

            Admin admin = getSignedInAdmin();

            if (admin != null) {
                logService.saveLog(String.valueOf(admin.getAdminId()), "Fetched Intern Details",
                        "Admin " + admin.getName() + " fetched details for Intern Name " + internName);
            }

            return ResponseEntity.ok(internDetails);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    //Handle company verification request submission
    @PostMapping("/submit_verification_request")
    public ModelAndView submitVerificationRequest(
            @RequestParam String internId,
            @RequestParam String internName,
            @RequestParam String internContact,
            @RequestParam String companyName,
            @RequestParam String contact,
            @RequestParam String email) {

        Verification verification = new Verification();
        verification.setInternId(internId);
        verification.setInternName(internName);
        verification.setInternContact(internContact);
        verification.setCompanyName(companyName);
        verification.setContact(contact);
        verification.setEmail(email);
        verificationService.createVerificationRequest(verification);
        Admin admin = getSignedInAdmin();
        String id = String.valueOf(admin.getAdminId());
        logService.saveLog(id, "Submitted Verification Request",
                "Admin " + admin.getName() + " submitted a verification request for Intern ID: " + internId);

        ModelAndView mv = new ModelAndView("admin/verification_requests");
        mv.addObject("success", true);
        mv.addObject("requests", verificationService.getPendingRequests());
        return mv;
    }

    // Success page after verification request submission
    @GetMapping("/verification_success")
    public ModelAndView verificationSuccess() {
        Admin admin = getSignedInAdmin();

        String id = String.valueOf(admin.getAdminId());

        logService.saveLog(id, "Viewed Verification Success Page",
                "Admin " + admin.getName() + " accessed the verification success page.");

        return new ModelAndView("admin/verification_success");
    }

    // Display Approved Verifications
    @GetMapping("/approved-verifications")
    public String showApprovedVerifications(Model model) {
        List<Verification> approvedVerifications = verificationService.getApprovedVerifications();
        model.addAttribute("verifications", approvedVerifications);

        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        String id = String.valueOf(admin.getAdminId());

        logService.saveLog(id, "Viewed Approved Verifications",
                "Admin " + admin.getName() + " viewed the list of approved verifications.");

        return "admin/approved_verifications";
    }

    // Display Rejected Verifications
    @GetMapping("/rejected-verifications")
    public String showRejectedVerifications(Model model) {
        List<Verification> rejectedVerifications = verificationService.getRejectedVerifications();
        model.addAttribute("verifications", rejectedVerifications);

        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        String id = String.valueOf(admin.getAdminId());

        logService.saveLog(id, "Viewed Rejected Verifications",
                "Admin " + admin.getName() + " viewed the list of rejected verifications.");

        return "admin/rejected_verifications";
    }

    //-------------------------------------Attendance Module-------------------------------------------
    @PostMapping("/upload_attendance")
    public String uploadAttendance(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a file to upload.");
            return "redirect:/admin/attendance";
        }

        try {
            attendanceService.processAttendanceFile(file);
            Admin admin = getSignedInAdmin();
            if (admin != null) {
                String id = String.valueOf(admin.getAdminId());

                logService.saveLog(id, "Uploaded Attendance Data",
                        "Admin " + admin.getName() + " uploaded a new attendance file.");
            }
            redirectAttributes.addFlashAttribute("successMessage", "Attendance data uploaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload attendance data.");
        }

        return "redirect:/bisag/admin/attendance";
    }

    @GetMapping("/attendance")
    public String getAllAttendance(Model model) {
        List<Attendance> attendances = attendanceRepo.findAll();

        Map<String, Float> internTotalAttendanceMap = new HashMap<>();
        for (Attendance attendance : attendances) {
            String internId = attendance.getInternId();
            if (!internTotalAttendanceMap.containsKey(internId)) {
                float totalAttendance = attendanceService.calculateTotalAttendance(internId);
                internTotalAttendanceMap.put(internId, totalAttendance);
            }
        }

        model.addAttribute("attendances", attendances);
        model.addAttribute("internTotalAttendanceMap", internTotalAttendanceMap);

        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        String id = String.valueOf(admin.getAdminId());
        logService.saveLog(id, "Viewed Attendance Data",
                "Admin " + admin.getName() + " accessed the attendance records.");

        return "admin/attendance";
    }

    // --------------------------------------Display all relieving records---------------------------------------------
    @GetMapping("/ask_records")
    public String showVerificationFilterPage() {
        return "admin/ask_records";
    }
//
//    // Display the form for adding a relieving record of cancelled interns
////    @GetMapping("/cancelled_interns_relieving_records")
////    public String viewCancelRelievingRecords(Model model) {
////        List<College> college = fieldService.getColleges();
////
//////        Optional<RRecord> record = recordService.getRecordById(1L);
////
////        Admin admin = getSignedInAdmin();
////        model.addAttribute("admin", admin);
////
////        logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Cancelled Relieving Records",
////                "Admin " + admin.getName() + " viewed the Cancelled Relieving Records page.");
////
////        List<Intern> cancelledInterns = internService.getCancelledInterns();
////        model.addAttribute("interns", cancelledInterns);
////        model.addAttribute("college", college);
////        model.addAttribute("records", record);
////        model.addAttribute("admin", adminName(session));
////
////        return "admin/cancelled_interns_relieving_records";
////    }
//    // Display the form for adding a relieving record
////    @GetMapping("/relieving_records")
////    public String viewRelievingRecords(Model model) {
////        List<Intern> interns = internService.getAllInterns();
////        List<College> college = fieldService.getColleges();
////
////        Optional<RRecord> record = recordService.getRecordById(1L);
////
////        Admin admin = getSignedInAdmin();
////        model.addAttribute("admin", admin);
////
////        logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Relieving Records",
////                "Admin " + admin.getName() + " viewed the Relieving Records page.");
////
////        model.addAttribute("interns", interns);
////        model.addAttribute("college", college);
////        model.addAttribute("records", record);
////        model.addAttribute("admin", adminName(session));
////
////        return "admin/relieving_records";
////    }
//
//    // Handle relieving record submission for internship completed
//    @PostMapping("/submit_relieving_record")
//    public String submitRelievingRecord(
//            @RequestParam String internId,
//            @RequestParam String FirstName,
//            @RequestParam String groupId,
//            @RequestParam String collegeName,
//            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate joiningDate,
//            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate plannedDate,
//            @RequestParam String password,
//            @RequestParam String media,
//            @RequestParam String status,
//            @RequestParam String project,
////            @RequestParam String thesis,
//            @RequestParam String others,
//            @RequestParam String books,
//            @RequestParam String subscription,
//            @RequestParam String accessRights,
//            @RequestParam String pendrives,
//            @RequestParam String unusedCd,
//            @RequestParam String backupProject,
//            @RequestParam String system,
//            @RequestParam String stipend,
//            @RequestParam String information,
//            @RequestParam String endInterview,
//            @RequestParam String weeklyReport,
//            @RequestParam String attendance,
//            @RequestParam String finalReport,
//            @RequestParam(required = false) String submissionTime,
//            RedirectAttributes redirectAttributes) {
//
//        // Parse submission time or use current time if not provided
//        LocalDateTime timestamp = (submissionTime != null && !submissionTime.isEmpty())
//                ? LocalDateTime.parse(submissionTime)
//                : LocalDateTime.now();
//        try {
//            List<RRecord> existingRecords = recordService.findByInternId(internId);
//            if (!existingRecords.isEmpty()) {
//                redirectAttributes.addFlashAttribute("errorMessage", "Intern with ID " + internId + " is already relieved!");
//                return "redirect:/bisag/admin/relieving_records_list";
//            }
//
//            java.sql.Date joiningDateConverted = java.sql.Date.valueOf(joiningDate);
//            java.sql.Date plannedDateConverted = java.sql.Date.valueOf(plannedDate);
//
//            RRecord record = new RRecord();
//            record.setInternId(internId);
//            record.setFirstName(FirstName);
//            record.setGroupId(groupId);
//            record.setCollegeName(collegeName);
//            record.setJoiningDate(joiningDateConverted);
//            record.setPlannedDate(plannedDateConverted);
//            record.setPassword(password);
//            record.setMedia(media);
//            record.setStatus(status);
//            record.setProject(project);
////            record.setThesis(thesis);
//            record.setOthers(others);
//            record.setBooks(books);
//            record.setSubscription(subscription);
//            record.setAccessRights(accessRights);
//            record.setPendrives(pendrives);
//            record.setUnusedCd(unusedCd);
//            record.setBackupProject(backupProject);
//            record.setSystem(system);
//            record.setStipend(stipend);
//            record.setInformation(information);
//            record.setEndInteriew(endInterview);
//            record.setWeeklyReport(weeklyReport);
//            record.setAttendance(attendance);
//            record.setFinalReport(finalReport);
//            record.setSubmissionTimestamp(timestamp);
//
//            recordService.saveRecord(record);
//
//            Admin admin = getSignedInAdmin();
//
//            if (admin != null) {
//                String id = String.valueOf(admin.getAdminId());
//                logService.saveLog(id, "Submitted Relieving Record",
//                        "Admin " + admin.getName() + " submitted a relieving record for Intern ID: " + internId);
//            }
//
//            redirectAttributes.addFlashAttribute("successMessage", "Relieving record for Intern ID " + internId + " submitted successfully!");
//            return "redirect:/bisag/admin/relieving_records_list";
//
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while submitting the relieving record. Please try again.");
//            return "redirect:/bisag/admin/relieving_records_list";
//        }
//    }
//
//    @GetMapping("/submit_relieving_record_cancelled")
//    public String showCancelledRelievingRecordForm(Model model) {
//        List<Intern> cancelledInterns = internService.getCancelledInterns();
//        model.addAttribute("interns", cancelledInterns);
//        return "redirect:/bisag/admin/cancelled_interns_relieving_records";
//    }
//    // Handle relieving record submission for internship cancelled
//    @PostMapping("/submit_relieving_record_cancelled")
//    public String submitCancelRelievingRecord(
//            @RequestParam String internId,
//            @RequestParam String FirstName,
//            @RequestParam String groupId,
//            @RequestParam String collegeName,
//            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate joiningDate,
//            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate plannedDate,
//            @RequestParam String password,
//            @RequestParam String media,
//            @RequestParam String status,
//            @RequestParam String project,
////            @RequestParam String thesis,
//            @RequestParam String others,
//            @RequestParam String books,
//            @RequestParam String subscription,
//            @RequestParam String accessRights,
//            @RequestParam String pendrives,
//            @RequestParam String unusedCd,
//            @RequestParam String backupProject,
//            @RequestParam String system,
//            @RequestParam String stipend,
//            @RequestParam String information,
//            @RequestParam String endInterview,
//            @RequestParam String weeklyReport,
//            @RequestParam String attendance,
//            RedirectAttributes redirectAttributes) {
//
//        try {
//            List<RRecord> existingRecords = recordService.findByInternId(internId);
//            if (!existingRecords.isEmpty()) {
//                redirectAttributes.addFlashAttribute("errorMessage", "Intern with ID " + internId + " is already relieved!");
//                return "redirect:/bisag/admin/relieving_records_list";
//            }
//
//            java.sql.Date joiningDateConverted = java.sql.Date.valueOf(joiningDate);
//            java.sql.Date plannedDateConverted = java.sql.Date.valueOf(plannedDate);
//
//            RRecord record = new RRecord();
//            record.setInternId(internId);
//            record.setFirstName(FirstName);
//            record.setGroupId(groupId);
//            record.setCollegeName(collegeName);
//            record.setJoiningDate(joiningDateConverted);
//            record.setPlannedDate(plannedDateConverted);
//            record.setPassword(password);
//            record.setMedia(media);
//            record.setStatus(status);
//            record.setProject(project);
////            record.setThesis(thesis);
//            record.setOthers(others);
//            record.setBooks(books);
//            record.setSubscription(subscription);
//            record.setAccessRights(accessRights);
//            record.setPendrives(pendrives);
//            record.setUnusedCd(unusedCd);
//            record.setBackupProject(backupProject);
//            record.setSystem(system);
//            record.setStipend(stipend);
//            record.setInformation(information);
//            record.setEndInteriew(endInterview);
//            record.setWeeklyReport(weeklyReport);
//            record.setAttendance(attendance);
//
//            recordService.saveRecord(record);
//            Admin admin = getSignedInAdmin();
//            if (admin != null) {
//                String id = String.valueOf(admin.getAdminId());
//                logService.saveLog(id, "Submitted Cancelled Relieving Record",
//                        "Admin " + admin.getName() + " submitted a relieving record for cancelled Intern ID: " + internId);
//            }
//
//            redirectAttributes.addFlashAttribute("successMessage", "Relieving record for Intern ID " + internId + " submitted successfully!");
//            return "redirect:/bisag/admin/relieving_records_list";
//
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while submitting the relieving record. Please try again.");
//            return "redirect:/bisag/admin/relieving_records_list";
//        }
//    }
//    @GetMapping("/relieving_records_list")
//    public String getAllRelievingRecords(Model model, RedirectAttributes redirectAttributes) {
//        try {
//            List<RRecord> records = recordService.getAllRecords();
//            model.addAttribute("records", records);
//
//            Admin admin = getSignedInAdmin();
//            model.addAttribute("admin", admin);
//
//            if (admin != null) {
//                String id = String.valueOf(admin.getAdminId());
//
//                logService.saveLog(id, "Viewed Relieving Records",
//                        "Admin " + admin.getName() + " accessed the relieving records list.");
//            }
//
//            if (!records.isEmpty()) {
//                redirectAttributes.addFlashAttribute("successMessage", "Relieving records loaded successfully!");
//            } else {
//                redirectAttributes.addFlashAttribute("errorMessage", "No relieving records found.");
//            }
//
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Error loading relieving records. Please try again.");
//        }
//
//        return "admin/relieving_records_list";
//    }
//
//    //Show records ID wise-------------------
//    @GetMapping("/relieving_records_detail/{id}")
//    public String getRecordsDetails(@PathVariable("id") String id, Model model) {
//        Optional<RRecord> record = recordService.getRecordById(Long.parseLong(id));
//        if (record.isPresent()) {
//            model.addAttribute("records", record);
//            Admin admin = getSignedInAdmin();
//            model.addAttribute("admin", admin);
//            logService.saveLog(id, "Relieving Records Details View",
//                    "Admin viewed the details of records with ID: " + id);
//            return "admin/relieving_records_detail";
//        } else {
//            return "error/404";
//        }
//    }

    // Automatically fetches the group id when the intern id is being selected
    @GetMapping("/getGroupId/{internId}")
    @ResponseBody
    public ResponseEntity<String> getGroupId(@PathVariable String internId) {
        Optional<Intern> intern = internService.findById(internId);

        if (intern.isPresent()) {
            return ResponseEntity.ok(String.valueOf(intern.get().getGroup().getGroupId()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group ID not found");
        }
    }

    @GetMapping("/getCollegeName/{internId}")
    @ResponseBody
    public ResponseEntity<String> getCollegeName(@PathVariable String internId) {
        Optional<Intern> intern = internService.findById(internId);

        if (intern.isPresent() && intern.get().getCollegeName() != null) {
            return ResponseEntity.ok(intern.get().getCollegeName());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("College not found");
        }
    }

    @GetMapping("/getJoiningDate/{internId}")
    @ResponseBody
    public String getJoiningDate(@PathVariable String internId) {
        Intern intern = internRepo.findByInternId(internId);
        if (intern != null && intern.getJoiningDate() != null) {
            LocalDate joiningDate = intern.getJoiningDate().toLocalDate();
            return joiningDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        return "";
    }
    @GetMapping("/getInternName/{internId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> fetchInternDetailsById(@PathVariable String internId) {
        Optional<Intern> internOptional = internService.findById(internId);

        if (internOptional.isPresent()) {
            Intern intern = internOptional.get();
            Map<String, String> response = new HashMap<>();

            String internName = (intern.getFirstName() != null) ? intern.getFirstName() : "N/A";
            response.put("internName", internName);

//            if (intern.getGuide() != null) {
//                Long guide = intern.getGuide().getGuideId();  // Assuming getGuideId() returns Long
//                response.put("guide", (guide != null) ? String.valueOf(guide) : "N/A");
//            } else {
//                response.put("guide", "N/A");
//            }

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/getAllGuides")
    @ResponseBody
    public ResponseEntity<List<Map<String, String>>> getAllGuides() {
        List<Guide> guides = guideService.findAllGuides();
        List<Map<String, String>> guideList = new ArrayList<>();

        for (Guide guide : guides) {
            Map<String, String> guideMap = new HashMap<>();
            guideMap.put("id", String.valueOf(guide.getGuideId()));
            guideMap.put("name", guide.getName());
            guideList.add(guideMap);
        }

        return ResponseEntity.ok(guideList);
    }

    // Fetch interns based on Group ID
    @GetMapping("/getInternsByGroup/{groupId}")
    public ResponseEntity<List<Intern>> getInternsByGroup(@PathVariable Long groupId) {
        List<Intern> interns = internService.getInternsByGroupId(groupId);
        return ResponseEntity.ok(interns);
    }

    //-------------------------------Leave application module-----------------------------------
    // Fetch pending leave applications for admin view
    @GetMapping("/pending_leaves")
    public String viewPendingLeaves(Model model, HttpSession session) {
        List<LeaveApplication> pendingLeaves = leaveApplicationRepo.findByStatus("Pending");
        String role = (String) session.getAttribute("role");
        model = countNotifications(model);
        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Pending Leave Applications",
                "Admin " + admin.getName() + " viewed pending leave applications.");

        model.addAttribute("pendingLeaves", pendingLeaves);
        model.addAttribute("role", role);

        return "admin/pending_leaves";
    }

    // Approve leave request
    @PostMapping("/approve_leave/{id}")
    public String approveLeave(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<LeaveApplication> optionalLeave = leaveApplicationRepo.findById(id);

        if (optionalLeave.isPresent()) {
            LeaveApplication leave = optionalLeave.get();
            leave.setAdminApproval(true);

            // If both admin and guide have approved, set status to "Approved"
            if (leave.isAdminApproval() && leave.isGuideApproval()) {
                leave.setStatus("Approved");
            }

            leaveApplicationRepo.save(leave);
            Admin admin = getSignedInAdmin();

            if (admin != null) {
                String adminId = String.valueOf(admin.getAdminId());
                logService.saveLog(adminId, "Approved Leave Application",
                        "Admin " + admin.getName() + " approved leave application for intern ID: " + leave.getInternId());
            }
            redirectAttributes.addFlashAttribute("successMessage", "Leave application approved successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Leave application not found!");
        }
        return "redirect:/bisag/admin/pending_leaves";
    }

    @PostMapping("/reject_leave/{id}")
    public String rejectLeave(@PathVariable Long id, @RequestParam("remarks") String remarks, RedirectAttributes redirectAttributes) {
        Optional<LeaveApplication> optionalLeave = leaveApplicationRepo.findById(id);
        if (optionalLeave.isPresent()) {
            LeaveApplication leave = optionalLeave.get();
            leave.setStatus("Rejected");
            leave.setAdminApproval(false);
            leave.setGuideApproval(false);
            leave.setRemarks(remarks);
            leaveApplicationRepo.save(leave);

            Admin admin = getSignedInAdmin();
            if (admin != null) {
                String adminId = String.valueOf(admin.getAdminId());
                logService.saveLog(adminId, "Rejected Leave Application",
                        "Admin " + admin.getName() + " rejected leave application for intern ID: " + leave.getInternId() + ". Remarks: " + remarks);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Leave application rejected successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Leave application not found!");
        }

        return "redirect:/bisag/admin/pending_leaves";
    }

    // View Leave History (Approved & Rejected)
    @GetMapping("/leave_history")
    public String viewLeaveHistory(Model model) {
        List<LeaveApplication> leaveHistory = leaveApplicationRepo.findByStatusIn(Arrays.asList("Approved", "Rejected"));
        model.addAttribute("leaveHistory", leaveHistory);

        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        if (admin != null) {
            String adminId = String.valueOf(admin.getAdminId());
            logService.saveLog(adminId, "Viewed Leave History",
                    "Admin " + admin.getName() + " viewed the leave history.");
        }

        return "admin/leave_history";
    }

    // View Leave Details
    @GetMapping("/leave_details/{id}")
    public String viewLeaveDetails(@PathVariable Long id, Model model) {
        LeaveApplication leave = leaveApplicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave Application Not Found"));
        model.addAttribute("leave", leave);

        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        if (admin != null) {
            String adminId = String.valueOf(admin.getAdminId());
            logService.saveLog(adminId, "Viewed Leave Details",
                    "Admin " + admin.getName() + " viewed details of leave application ID: " + id);
        }

        return "admin/leave_details";
    }

    @GetMapping("/half_leave/{id}")
    public String getHalfLeaveDetails(@PathVariable("id") String id, Model model) {
        Optional<LeaveApplication> leaveOptional = leaveApplicationService.getLeaveById(Long.parseLong(id));

        if (leaveOptional.isPresent()) {
            LeaveApplication leave = leaveOptional.get();
            Optional<Intern> internOptional = Optional.ofNullable(internService.getInternById(leave.getInternId()));

            if (internOptional.isPresent()) {
                Intern intern = internOptional.get();
                model.addAttribute("leave", leave);
                model.addAttribute("groupId", intern.getGroup() != null ? intern.getGroup().getGroupId() : "N/A");
                model.addAttribute("internName", intern.getFirstName());
            } else {
                model.addAttribute("groupId", "N/A");
                model.addAttribute("internName", "N/A");
            }

            Admin admin = getSignedInAdmin();
            model.addAttribute("admin", admin);
            model.addAttribute("adminName", admin.getName());
            logService.saveLog(String.valueOf(admin.getAdminId()), "View Half Day Leave",
                    "Admin " + admin.getName() + " viewed the details of half day leave with ID: " + id);
            return "admin/half_leave";
        } else {
            return "error/404";
        }
    }
    // ========================= Undertaking Form Management ==========================

    @GetMapping("/undertaking")
    public String showUndertakingForm(Model model) {
        System.out.println("Admin Undertaking page accessed");
        List<String> allInternIds = internRepo.findAllInternIds();
        List<Undertaking> acceptedForms = undertakingRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        Set<String> acceptedInternIds = acceptedForms.stream()
                .map(Undertaking::getIntern)
                .collect(Collectors.toSet());

        List<String> pendingInternIds = allInternIds.stream()
                .filter(id -> !acceptedInternIds.contains(id))
                .collect(Collectors.toList());

        model.addAttribute("acceptedForms", acceptedForms);
        model.addAttribute("pendingInternIds", pendingInternIds);

        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        if (admin != null) {
            String adminId = String.valueOf(admin.getAdminId());
            logService.saveLog(adminId, "Viewed Undertaking Forms",
                    "Admin " + admin.getName() + " accessed the Undertaking Forms page.");
        }

        return "admin/undertaking_form";
    }

    // Corrected POST method to add undertaking form
    @PostMapping("/add_undertaking")
    public String addUndertaking(@RequestParam String rules, RedirectAttributes redirectAttributes) {
//        try {
            Undertaking undertaking = new Undertaking();
            undertaking.setContent(rules);
            undertaking.setCreatedAt(LocalDateTime.now());
            undertakingRepo.save(undertaking);

//            Admin admin = getSignedInAdmin();
//            if (admin != null) {
//                String adminId = String.valueOf(admin.getAdminId());
//                logService.saveLog(adminId, "Added Undertaking Form",
//                        "Admin " + admin.getName() + " added a new undertaking form.");
//            }
//
//            redirectAttributes.addFlashAttribute("successMessage", "Undertaking form added successfully!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Error adding undertaking form. Please try again.");
//        }

        return "redirect:/bisag/admin/undertaking";
    }

    // Corrected method for updating the undertaking form
    @PostMapping("/update_undertaking/{id}")
    public String updateUndertaking(@PathVariable Long id, @RequestParam String content) {
        Undertaking undertaking = undertakingRepo.findById(id).orElseThrow(() -> new RuntimeException("Undertaking not found"));
        undertaking.setContent(content);
        undertakingRepo.save(undertaking);
        return "redirect:/bisag/admin/undertaking";
    }

    // Fetch the latest Undertaking Form content for Interns
    @GetMapping("/undertaking-content")
    @ResponseBody
    public String getUndertakingContent() {
        String latestContent = undertakingRepo.findLatestUndertakingContent();

        if (latestContent == null || latestContent.isEmpty()) {
            return "No undertaking content available.";
        }
        return latestContent;
    }

    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_Thesis Storage Module_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    private static final String STORAGE_PATH = "/Users/pateldeep/Desktop/Coding/Springboot_Intern_Management_System-master-main/src/main/resources/static/files/thesis-storage/";
    // Upload Thesis PDF
    @PostMapping("/upload-thesis")
    public String uploadThesis(@RequestParam("thesisTitle") String thesisTitle,
                               @RequestParam("file") MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "File upload failed! Please select a file.");
                return "redirect:/bisag/admin/thesis-storage";
            }
            File directory = new File(STORAGE_PATH);
            if (!directory.exists() && !directory.mkdirs()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to create storage directory!");
                return "redirect:/bisag/admin/thesis-storage";
            }
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            String filePath = STORAGE_PATH + fileName;
            file.transferTo(new File(filePath));
            ThesisStorage thesis = new ThesisStorage();
            thesis.setThesisTitle(thesisTitle);
            thesis.setFilePath(filePath);
            thesis.setUploadDate(new Date());
            thesisStorageRepo.save(thesis);
            Admin admin = getSignedInAdmin();
            if (admin != null) {
                String adminId = String.valueOf(admin.getAdminId());
                logService.saveLog(adminId, "Uploaded Thesis",
                        "Admin " + admin.getName() + " uploaded a thesis titled '" + thesisTitle + "'.");
            }
            redirectAttributes.addFlashAttribute("successMessage", "Thesis uploaded successfully!");
            return "redirect:/bisag/admin/thesis-storage";

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error while saving the file. Please try again.");
            return "redirect:/bisag/admin/thesis-storage";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            return "redirect:/bisag/admin/thesis-storage";
        }
    }
    // Fetch List of Uploaded Theses
    @GetMapping("/thesis-storage")
    public String viewThesisStorage(Model model) {
        List<ThesisStorage> thesisList = thesisStorageRepo.findAll();
        model.addAttribute("theses", thesisList);

        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        if (admin != null) {
            String adminId = String.valueOf(admin.getAdminId());
            logService.saveLog(adminId, "Viewed Thesis Storage",
                    "Admin " + admin.getName() + " viewed the list of uploaded theses.");
        }
        return "admin/thesis_storage";
    }

    // Generate Shareable Link
    @GetMapping("/generate-thesis-link/{id}")
    @ResponseBody
    public String generateThesisLink(@PathVariable Long id) {
        ThesisStorage thesis = thesisStorageRepo.findById(id).orElse(null);
        if (thesis == null) {
            return "Invalid thesis ID";
        }
        Admin admin = getSignedInAdmin();
        if (admin != null) {
            String adminId = String.valueOf(admin.getAdminId());
            logService.saveLog(adminId, "Generated Thesis Link",
                    "Admin " + admin.getName() + " generated a shareable link for Thesis ID: " + id);
        }
        return "localhost:8080/bisag/admin/view-thesis/" + id;
    }

    // Download/View Thesis PDF
    @GetMapping("/view-thesis/{id}")
    public ResponseEntity<Resource> viewThesis(@PathVariable Long id) {
        ThesisStorage thesis = thesisStorageRepo.findById(id).orElse(null);
        if (thesis == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            Path path = Paths.get(thesis.getFilePath());
            Resource resource = new UrlResource(path.toUri());
            Admin admin = getSignedInAdmin();
            if (admin != null) {
                String adminId = String.valueOf(admin.getAdminId());
                logService.saveLog(adminId, "Viewed Thesis PDF",
                        "Admin " + admin.getName() + " viewed Thesis ID: " + id);
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName().toString() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Update Allowed Intern ID for Thesis
    @PostMapping("/update-thesis-intern-id")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateThesisInternId(@RequestBody Map<String, Object> requestData) {
        Long thesisId = Long.parseLong(requestData.get("thesisId").toString());
        String newInternId = requestData.get("allowedInternId") != null ? requestData.get("allowedInternId").toString() : null;

        Optional<ThesisStorage> thesisOptional = thesisStorageRepo.findById(thesisId);
        if (thesisOptional.isPresent()) {
            ThesisStorage thesisStorage = thesisOptional.get();

            if (newInternId != null && !newInternId.isEmpty()) {
                thesisStorage.setAllowedInternId(newInternId);
            }
            thesisStorageRepo.save(thesisStorage);
            Admin admin = getSignedInAdmin();
            if (admin != null) {
                String adminId = String.valueOf(admin.getAdminId());
                logService.saveLog(adminId, "Updated Thesis Access",
                        "Admin " + admin.getName() + " updated allowed intern ID for Thesis ID: " + thesisId);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thesis Intern ID updated successfully.");
            return ResponseEntity.ok(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Thesis not found with the provided ID.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @PostMapping("/remove-thesis-intern-id")
    @ResponseBody
    public Map<String, Boolean> removeThesisInternId(@RequestBody Map<String, Long> request) {
        Long thesisId = request.get("thesisId");
        Map<String, Boolean> response = new HashMap<>();
        Optional<ThesisStorage> thesisOptional = thesisStorageRepo.findById(thesisId);
        if (thesisOptional.isPresent()) {
            ThesisStorage thesis = thesisOptional.get();
            thesis.setAllowedInternId(null);
            thesisStorageRepo.save(thesis);
            response.put("success", true);
        } else {
            response.put("success", false);
        }
        Admin admin = getSignedInAdmin();
        if (admin != null) {
            String adminId = String.valueOf(admin.getAdminId());
            logService.saveLog(adminId, "Removed Thesis Access",
                    "Admin " + admin.getName() + " removed access of intern for Thesis ID: " + thesisId);
        }
        return response;
    }
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-Messaging Module_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    // Load chat page
    @GetMapping("/chat")
    public String loadMessengerPage(Model model) {
        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);


        if (admin == null) {
            System.out.println("Admin not found or session expired.");
            return "redirect:/login";
        }
        model.addAttribute("loggedInAdmin", admin);
        List<Admin> admins = adminService.getAdmin();
        List<Guide> guides = guideService.getGuide();
        List<Intern> interns = internService.getAllInterns();
        List<GroupEntity> groups = groupService.getGroups();
        model.addAttribute("admins", admins);
        model.addAttribute("guides", guides);
        model.addAttribute("interns", interns);
        model.addAttribute("groups", groups);
        String adminId = String.valueOf(admin.getAdminId());
        logService.saveLog(adminId, "Accessed Chat Page",
                "Admin " + admin.getName() + " accessed the chat page.");

        return "admin/query_to_guide";
    }
    // Admin sends a message
    @PostMapping("/chat/send")
    public ResponseEntity<Message> sendMessageAsAdmin(
            @RequestParam String receiverId,
            @RequestParam String messageText) {
        Admin admin = getSignedInAdmin();
        String senderId = String.valueOf(admin.getAdminId());
        Message message = messageService.sendMessage(senderId, receiverId, messageText);
        logService.saveLog(senderId, "Sent a Message",
                "Admin " + admin.getName() + " sent a message to User ID: " + receiverId);
        return ResponseEntity.ok(message);
    }
    // Admin fetches chat history (both sent and received messages)
    @GetMapping("/chat/history")
    public ResponseEntity<List<Message>> getChatHistoryAsAdmin(@RequestParam String receiverId) {
        Admin admin = getSignedInAdmin();
        String senderId = String.valueOf(admin.getAdminId());

        List<Message> messages = messageService.getChatHistoryForBothUsers(senderId, receiverId);

        logService.saveLog(senderId, "Viewed Chat History",
                "Admin " + admin.getName() + " viewed chat history with User ID: " + receiverId);

        return ResponseEntity.ok(messages);
    }
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-Task Assignment Module_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    // View Task Assignments Page
    @GetMapping("/tasks_assignments")
    public String viewTaskAssignmentsPage(Model model) {
        List<TaskAssignment> tasks = taskAssignmentService.getAllTasks();
        List<Intern> interns = internService.getAllInterns();

        model.addAttribute("interns", interns);
        model.addAttribute("tasks", tasks);

        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Task Assignments",
                "Admin " + admin.getName() + " accessed the Task Assignments page.");

        return "admin/task_assignments";
    }

    // Assign a New Task
    @PostMapping("/tasks/assign")
    public String assignTask(
            @RequestParam("intern") String intern,
            @RequestParam("assignedById") String assignedById,
            @RequestParam("assignedByRole") String assignedByRole,
            @RequestParam("taskDescription") String taskDescription,
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr,
            RedirectAttributes redirectAttributes) {

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);
            if (endDate.before(startDate)) {
                redirectAttributes.addFlashAttribute("errorMessage", "End date cannot be before start date!");
                return "redirect:/bisag/admin/tasks_assignments";
            }
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
                logService.saveLog(assignedById, "Assigned a Task",
                        "User with ID " + assignedById + " assigned a task to Intern ID: " + intern);

                redirectAttributes.addFlashAttribute("successMessage", "Task assigned successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid Intern ID! Please select a valid intern.");
            }
        } catch (ParseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid date format! Please enter a valid date.");
            return "redirect:/bisag/admin/tasks_assignments";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            return "redirect:/bisag/admin/tasks_assignments";
        }
        return "redirect:/bisag/admin/tasks_assignments";
    }

    //View Task Details ID wise
    @GetMapping("/task_details/{id}")
    public String viewTaskDetails(@PathVariable Long id, Model model) {
        TaskAssignment tasks = taskAssignmentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Task Assignment Not Found"));
        model.addAttribute("tasks", tasks);
        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        if (admin != null) {
            String adminId = String.valueOf(admin.getAdminId());
            logService.saveLog(adminId, "Viewed Task Details",
                    "Admin " + admin.getName() + " viewed details of Task Assignment ID: " + id);
        }

        return "admin/task_details";
    }
    // Get Tasks Assigned by Admin/Guide
    @GetMapping("/tasks/assignedBy/{assignedById}")
    public ResponseEntity<List<TaskAssignment>> getTasksAssignedBy(@PathVariable("assignedById") String assignedById) {
        logService.saveLog(assignedById, "Viewed Assigned Tasks",
                "User with ID " + assignedById + " viewed tasks assigned by them.");

        return ResponseEntity.ok(taskAssignmentService.getTasksAssignedBy(assignedById));
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
            logService.saveLog(task.getAssignedById(), "Approved a Task",
                    "User with ID " + task.getAssignedById() + " approved Task ID: " + taskId);

            return ResponseEntity.ok("Task approved successfully.");
        }
        return ResponseEntity.badRequest().body("Task not found.");
    }

    // Fetch Proof Attachment (View in Browser)
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
                    "Admin with ID " + taskOpt.get().getAssignedById() + " viewed proof for Task ID: " + taskId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Update Task Status
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
            task.setStatus(newStatus);
            taskAssignmentService.saveTask(task);
            logService.saveLog(task.getAssignedById(), "Updated Task Status",
                    "User with ID " + task.getAssignedById() + " updated Task ID: " + id + " to status: " + newStatus);

            return ResponseEntity.ok(Map.of("success", true, "message", "Status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An error occurred: " + e.getMessage()));
        }
    }

    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_Feedback Module-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_
    @GetMapping("/feedback_form_list")
    public String getAdminFeedbackList(Model model) {
        List<Feedback> feedbacks = feedbackService.getFeedback();
        model.addAttribute("feedbacks", feedbacks);

        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Feedback List",
                "Admin " + admin.getName() + " accessed the Feedback List page.");

        return "admin/feedback_form_list";
    }

    // =============================Announcement Board=================================================
    @GetMapping("/announcement")
    public String getAllAnnouncements(Model model) {
        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Announcements",
                "Admin " + admin.getName() + " accessed the Announcements page.");
        model.addAttribute("announcements", announcementService.getAllAnnouncements());
        model.addAttribute("newAnnouncement", new Announcement());
        return "admin/announcement";
    }

    @PostMapping("/announcement")
    public String createAnnouncement(@ModelAttribute Announcement announcement, Model model) {
        Admin admin = getSignedInAdmin();
        logService.saveLog(String.valueOf(admin.getAdminId()), "Created Announcement",
                "Admin " + admin.getName() + " created a new announcement.");
        announcementService.createAnnouncement(announcement);
        return "redirect:/bisag/admin/announcement";
    }

    @PostMapping("/announcement/delete/{id}")
    public String deleteAnnouncement(@PathVariable("id") int announcementId, HttpSession session) {
        Admin admin = getSignedInAdmin();
        try {
            announcementRepo.deleteById(announcementId);
            logService.saveLog(String.valueOf(admin.getAdminId()), "Deleted Announcement",
                    "Admin " + admin.getName() + " deleted an announcement with ID: " + announcementId);
            session.setAttribute("msg", "Announcement deleted successfully.");
        } catch (Exception e) {
            session.setAttribute("msg", "Error deleting announcement: " + e.getMessage());
        }
        return "redirect:/bisag/admin/announcement";
    }
    @GetMapping("/update_project_def")
    public String showAssignProjectDefinitionForm(Model model) {
        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Assign Project Definition Form", "Admin " + admin.getName() + " accessed the Assign Project Definition Form page.");
        List<GroupEntity> groups = groupRepo.findAll();
        model.addAttribute("groups", groups);
        return "admin/update_project_def";
    }

    @PostMapping("/update_project_def")
    public String assignProjectDefinition(@RequestParam String groupId,
                                          @RequestParam String projectDefinition,
                                          @RequestParam String description,
                                          Model model) {
        Admin admin = getSignedInAdmin();
        logService.saveLog(String.valueOf(admin.getAdminId()), "Assigned Project Definition", "Admin " + admin.getName() + " assigned project definition to Group ID: " + groupId);
        GroupEntity group = groupRepo.getByGroupId(groupId);
        if (group == null) {
            model.addAttribute("error", "❌ Group not found.");
            return "admin/update_project_def";
        }
        group.setProjectDefinition(projectDefinition);
        group.setDescription(description);
        group.setProjectDefinitionStatus("gpending");
        groupRepo.save(group);
        List<Guide> guides = guideRepo.findByGroupId(group.getGroupId());
        if (guides == null || guides.isEmpty()) {
            model.addAttribute("error", "No guides found for the group.");
            return "admin/update_project_def";
        }
        for (Guide guide : guides) {
            guide.setDefinitionStatus("Pending");
            guideRepo.save(guide);
        }
        model.addAttribute("success", "Project Definition Assigned and Sent for Approval.");
        return "admin/update_project_def";
    }

    //----------approve or reject  definition which is updated by guide manually---------------------/////
    @GetMapping("/update_def_ans")
    public String showPendingProjectDefinitions(Model model) {
        // Fetch all groups where project definition status is "pending"
        List<GroupEntity> pendingGroups = groupRepo.findByProjectDefinitionStatus("gupending");

        // Add the fetched groups to the model
        model.addAttribute("groups", pendingGroups);

        return "admin/update_def_ans"; // Return Thymeleaf template
    }


    @PostMapping("/update_def_ans")
    public String updateProjectDefinitionFromAdmin(@PathVariable("groupId") String groupId,
                                                   @RequestParam("status") String status,
                                                   @RequestParam("projectDefinition") String projectDefinition,
                                                   @RequestParam("description") String description,
                                                   HttpSession session,
                                                   RedirectAttributes redirectAttributes) {
        GroupEntity group = groupService.getGroup(groupId);

        if (group == null) {
            redirectAttributes.addFlashAttribute("error", "Group not found.");
            return "redirect:/bisag/admin/update_def_ans";
        }

        group.setProjectDefinition(projectDefinition);
        group.setDescription(description);

        if ("approved".equalsIgnoreCase(status)) {
            group.setProjectDefinitionStatus("approved");

            List<Intern> interns = internService.getInternsByGroupId(group.getId());
            for (Intern intern : interns) {
                intern.setProjectDefinitionName(group.getProjectDefinition());
                internRepo.save(intern);
            }

            // Logging action
            String username = (String) session.getAttribute("username");
            Admin admin = adminService.getAdminByUsername(username);
            if (admin != null) {
                logService.saveLog(String.valueOf(admin.getAdminId()), "Project Definition Approved",
                        "Admin " + admin.getName() + " approved project definition for group ID: " + groupId);
            }

            redirectAttributes.addFlashAttribute("success", "Project Definition approved.");

        } else if ("rejected".equalsIgnoreCase(status)) {
            group.setProjectDefinitionStatus("rejected");

            // Optional: Add log for rejection
            String username = (String) session.getAttribute("username");
            Admin admin = adminService.getAdminByUsername(username);
            if (admin != null) {
                logService.saveLog(String.valueOf(admin.getAdminId()), "Project Definition Rejected",
                        "Admin " + admin.getName() + " rejected project definition for group ID: " + groupId);
            }

            redirectAttributes.addFlashAttribute("success", "Project Definition rejected.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid status.");
            return "redirect:/bisag/admin/update_def_ans";
        }

        groupRepo.save(group);
        return "redirect:/bisag/admin/update_def_ans";
    }


    @GetMapping("/generate_credentials")
    public String showGenerateCredentialsForm(Model model) {
        List<Intern> internList = internService.getAllInterns();
        List<MyUser> userList = userService.findAllInterns();

        Admin admin = getSignedInAdmin();
        model.addAttribute("admin", admin);

        logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Generate Credentials", "Admin " + admin.getName() + " accessed the Generate Credentials page.");
        model.addAttribute("internList", internList);
        model.addAttribute("userList", userList);

        return "admin/generate_credentials";
    }

    @PostMapping("/update_credentials")
    public String updateCredentials(@RequestParam("internId") String internId,
                                    @RequestParam("internEmail") String internEmail) {

        Optional<Intern> internOptional = internService.findById(internId);
        if (internOptional.isPresent()) {
            Intern intern = internOptional.get();
            System.out.println("Intern email set to: " + internEmail);
            intern.setEmail(internEmail);

            String encryptedPassword = encodePassword("Bisag@123");
            intern.setPassword(encryptedPassword);
            intern.setIsCredentialsGenerated(true);

            internService.save(intern);

            Optional<MyUser> userOptional = userRepo.findByUserId(intern.getInternId());
            if (userOptional.isPresent()) {
                MyUser user = userOptional.get();
                user.setUsername(internEmail);
                user.setPassword(encryptedPassword);
                userRepo.save(user);
            } else {
                MyUser user = new MyUser();
                user.setUsername(internEmail);
                user.setPassword(encryptedPassword);
                user.setEnabled(true);
                user.setUserId(intern.getInternId());
                user.setRole("UNDERPROCESSINTERN");
                userRepo.save(user);
            }

            logService.saveLog(internId, "Updated Credentials",
                    "Admin updated Intern login credentials for "
                            + intern.getFirstName());
        }

        return "redirect:/bisag/admin/intern_application/new_interns";
    }

    @GetMapping("/final_approval_by_admin")
    public ModelAndView finalapprovedInterns(Model model) {
        ModelAndView mv = new ModelAndView();

        // Get the logged-in guide's username from session
        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);

        if (admin != null) {
            long adminId = admin.getAdminId();

            List<InternApplication> intern = internService.getInternApplication();

            mv.addObject("intern", intern);
            session.setAttribute("id", adminId);

            logService.saveLog(String.valueOf(adminId),
                    "View Shortlisted Intern Applications",
                    "Guide " + admin.getName() + " accessed the shortlisted intern applications page.");
        } else {
            System.out.println("Error: admin not found for logging!");
            mv.addObject("interns", List.of());
        }

        mv.setViewName("admin/final_approval_by_admin");
        return mv;
    }



//    @PostMapping("/final_approval_by_admin/ans")
//    public String finalInternApplicationSubmission(@RequestParam String message, @RequestParam long id,
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
//        return "redirect:/bisag/admin/final_approval_by_admin";
//    }

    @PostMapping("/final_approval_by_admin/ans")
    public String finalInternApplicationSubmission(@RequestParam long id, @RequestParam String status) {
        Optional<InternApplication> internOpt = internService.getInternApplication(id);

        if (internOpt.isPresent()) {
            InternApplication intern = internOpt.get();
            intern.setAdminStatus(status);

            // Only update finalStatus if guide has already approved
            if ("passed".equals(status) && "passed".equals(intern.getGuideStatus())) {
                intern.setFinalStatus("passed");
            } else
            {
                intern.setFinalStatus("pending");
            }

            internService.addInternApplication(intern);
            logService.saveLog(String.valueOf(id), "Admin approved/rejected intern", "Status Change");
        }

        return "redirect:/bisag/admin/final_approval_by_admin";
    }



}
