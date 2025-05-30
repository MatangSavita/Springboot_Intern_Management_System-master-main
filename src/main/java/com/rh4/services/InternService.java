package com.rh4.services;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rh4.entities.*;
import com.rh4.repositories.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.*;
@Service
public class InternService {

	@Autowired
	private InternRepo internRepo;
	@Autowired
	private InternApplicationRepo internApplicationRepo;
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private CancelledRepo cancelledRepo;
	@Autowired
	private Cancelled cancelled;
	@Autowired
	private MyUserService myUserService;

	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	public void registerIntern(Intern intern)
	{
		internRepo.save(intern);
	}

	public String getMostRecentInternId()
	{
		Intern mostRecentIntern = internRepo.findTopByOrderByInternIdDesc();
		return mostRecentIntern != null ? mostRecentIntern.getInternId() : null;
	}
	public List<InternApplication> getInternApplication()
	{
		return internApplicationRepo.findAll();
	}
	public Optional<InternApplication> getInternApplication(long id)
	{
		return internApplicationRepo.findById(id);
	}
	public InternApplication save(InternApplication internApplication) {
		return internApplicationRepo.save(internApplication);
	}
	public Intern save(Intern intern) {
		return internRepo.save(intern);
	}
	public void addInternApplication(InternApplication intern)
	{
		internApplicationRepo.save(intern);
	}
	public List<InternApplication> getApprovedInterns()
	{

		return internApplicationRepo.getInternApprovedStatus();
	}

	// Update the return type to List<InternApplication> or another appropriate entity
	public List<InternApplication> getRejectedInterns() {
		return internRepo.getInternRejectedStatus(); // Fetch rejected interns
	}
	public List<Intern> getInterns()
	{
		return internRepo.findAll();
	}

	//it will fetch the intern IDs
	public List<Intern> getAllInterns() {
		List<Intern> interns = internRepo.findAll();
		System.out.println("Fetched interns: " + interns.size()); // Print size to verify data
		return interns;
	}

	// Fetch distinct project definitions from the Intern table
	public List<String> getDistinctProjectDefinitions() {
		return internRepo.findDistinctProjectDefinitionNames(); // Custom query to fetch distinct values
	}

	//	public void addIntern(Intern intern)
//	{
//		String encryptedPassword = passwordEncoder().encode(intern.getPassword());
//		intern.setPassword(encryptedPassword);
//		internRepo.save(intern);
//		//save to user table
//		String email = intern.getEmail();
//		String role = "UNDERPROCESSINTERN";
//		userRepo.deleteByUsername(email, role);
//		MyUser user = new MyUser();
//		user.setUsername(intern.getEmail());
//		//encrypt password
//		user.setPassword(encryptedPassword);
//		user.setRole("INTERN");
//		user.setEnabled(true);
//		//from long to string
//		String userId = intern.getInternId();
//		user.setUserId(userId);
//		userRepo.save(user);
//	}
	public void addIntern(Intern intern)
	{
		String encryptedPassword = passwordEncoder().encode(intern.getPassword());
		intern.setPassword(encryptedPassword);
		internRepo.save(intern);
		//save to user table
		String email = intern.getEmail();
		String role = "UNDERPROCESSINTERN";
		userRepo.deleteByUsername(email, role);
		MyUser user = new MyUser();
		user.setUsername(intern.getEmail());
		//encrypt password
		user.setPassword(encryptedPassword);
		user.setRole("INTERN");
		user.setEnabled(true);
		//from long to string
		String userId = intern.getInternId();
		user.setUserId(userId);
		// Set security pin from internapplication
		InternApplication internApplication = internApplicationRepo.findByEmail(intern.getEmail());
		if (internApplication != null) {
			user.setSecurityPin(internApplication.getSecurityPin());
		}
		userRepo.save(user);
	}


	public void updateCancellationStatus(Intern intern) {
		internRepo.save(intern);
	}

	public long approveForInterviewApplicationsCount() {
		return internApplicationRepo.countByStatus("pending");
	}
	public long countPendingInterviewApplications() {
		return internApplicationRepo.countPendingInterviewApplications();
		//return internApplicationRepo.countByFinalStatus("pending");
	}
	public long countInterns() {
		return internRepo.count();
	}

	public Optional<Intern> getIntern(String id) {
		return internRepo.findById(id);
	}

	public Intern getInternByUsername(String username) {
		return internRepo.findByEmail(username);
	}

	public InternApplication getInternApplicationByUsername(String username) {
		return internApplicationRepo.findByEmail(username);
	}

	public void cancelInternApplication(Optional<InternApplication> intern) {

		intern.get().setIsActive(false);
		cancelled.setCancelId(Long.toString(intern.get().getId()));
		cancelled.setTableName("internapplication");
		cancelledRepo.save(cancelled);

	}
	public List<Intern> getInternsByGroupId(Long groupId) {
		return internRepo.findByGroupId(groupId);
	}

	public List<Intern> getInternsByCancellationStatus(String cancellationStatus) {
		return internRepo.findByCancellationStatus(cancellationStatus);
	}

	public long countRequestedCancellations() {
		return internRepo.countByCancellationStatus("requested");
	}

//	public List<Intern> getCancelledInterns() {
//		return internRepo.getCancelledIntern();
//	}

	public List<Intern> getCancelledInterns() {
		return internRepo.findByCancellationStatus("Cancelled");
	}
	public List<Intern> getCurrentInterns() {
		return internRepo.getCurrentInterns();
	}

	public List<String> getDistinctGenders() {
		return internRepo.findDistinctGenders();
	}

	public List<Intern> getFilteredInterns(String college, Optional<Guide> guide, String domain,
										   String cancelled, Date startDate, Date endDate, String cancelledStatus) {
		if(cancelledStatus.equals("current"))
		{
			boolean isCancelled = true;
			return internRepo.getFilteredInterns(college,guide,domain,startDate,endDate,isCancelled);
		}
		else if(cancelledStatus.equals("cancelled"))
		{
			boolean isCancelled = false;
			return internRepo.getFilteredInterns(college,guide,domain,startDate,endDate,isCancelled);
		}
		else
		{
			return internRepo.getPendingInternsFilter(college,guide,domain,startDate,endDate);
		}
	}

	public Intern getInternById(String id) {
		return internRepo.findById(id).orElse(null);
	}
	public long pendingGroupCreationCount() {
		return internApplicationRepo.countByGroupCreated();
	}

	public void changePassword(Intern intern, String newPassword) {
		String encryptedPassword = passwordEncoder().encode(newPassword);
		intern.setPassword(encryptedPassword);
		internRepo.save(intern);
		// save to user table
		MyUser user = myUserService.getUserByUsername(intern.getEmail());
		user.setPassword(encryptedPassword);
		userRepo.save(user);

	}

	@Transactional
	public void updateInternProfilePicture(String internId, byte[] profilePicture) {
		internRepo.updateProfilePicture(internId, profilePicture);
	}

	public byte[] getImageData(String id) {
		Intern intern = internRepo.findById(id).orElseThrow(() -> new RuntimeException("Image not found"));
		return intern.getProfilePicture();
	}

	public Optional<Intern> findById(String internId) {
		return internRepo.findById(internId);
	}

	public String saveFile(MultipartFile file) {
		try {
			String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
			Path filePath = Paths.get("uploads/" + fileName);
			Files.write(filePath, file.getBytes());
			return fileName;
		} catch (IOException e) {
			throw new RuntimeException("⚠️ Error saving file", e);
		}
	}

	public String getInternNameById(Long internId) {
		return internRepo.findById(String.valueOf(internId))
				.map(Intern::getFirstName)
				.orElse("Intern Not Found");
	}

	public Intern getInternByName(String internName) {
		return internRepo.findByFirstName(internName);
	}
	public Intern findByInternId(String internId) {
		return internRepo.findByInternId(internId);
	}

	public List<Intern> findInternsByGroupId(String groupId) {
		return internRepo.findInternsByGroupId(groupId);
	}

	public List<Intern> getInternsByCancellationStatusList(List<String> statuses) {
		return internRepo.findByCancellationStatusIn(statuses);
	}


}
