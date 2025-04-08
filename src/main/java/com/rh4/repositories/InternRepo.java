package com.rh4.repositories;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.rh4.entities.*;

@Repository
public interface InternRepo extends JpaRepository<Intern, String> {

	@Query("SELECT i.internId FROM Intern i") // Assuming 'id' is the field name for intern ID
	List<String> findAllInternIds();

	Intern findTopByOrderByInternIdDesc();

	Intern findByEmail(String username);

	List<Intern> findByGroupId(long groupId);

	Optional<Intern> findById(String id);

	List<Intern> findByCancellationStatus(String cancellationStatus);

	long countByCancellationStatus(String cancellationStatus);

	@Query("SELECT i FROM Intern i WHERE i.isActive = false")
	List<Intern> getCancelledIntern();

	@Query("SELECT i FROM Intern i WHERE i.isActive = true")
	List<Intern> getCurrentInterns();

	@Query("SELECT i FROM Intern i " + "WHERE (:college IS NULL OR i.collegeName = :college) "
			+  "AND (:guide IS NULL OR i.guide = :guide) "
			+ "AND (:domain IS NULL OR i.domain = :domain) "
			+ "AND (:startDate IS NULL OR i.joiningDate >= :startDate) "
			+ "AND (:endDate IS NULL OR i.completionDate <= :endDate)" + "AND (i.isActive =:cancelled)")
	List<Intern> getFilteredInterns(@Param("college") String college,
									@Param("guide") Optional<Guide> guide, @Param("domain") String domain, @Param("startDate") Date startDate,
									@Param("endDate") Date endDate, @Param("cancelled") boolean cancelled);

	@Query("SELECT i FROM Intern i " + "INNER JOIN i.group g "
			+ "WHERE (:college IS NULL OR i.collegeName = :college) "
			+ "AND (:guide IS NULL OR g.guide = :guide) " + "AND (:domain IS NULL OR i.domain = :domain) "
			+ "AND (:startDate IS NULL OR i.joiningDate >= :startDate) "
			+ "AND (:endDate IS NULL OR i.completionDate <= :endDate)" + "AND g.finalReportStatus = 'pending'")
	List<Intern> getPendingInternsFilter(String college, Optional<Guide> guide, String domain,
										 Date startDate, Date endDate);

//	@Query("from InternApplication where status = 'rejected'")
//	public List<InternApplication> getInternRejectedStatus();

	@Query("SELECT i FROM InternApplication i WHERE i.status = 'rejected'")
	List<InternApplication> getInternRejectedStatus();

	@Modifying
	@Query("UPDATE Intern i SET i.profilePicture = :profilePicture WHERE i.internId = :internId")
	void updateProfilePicture(@Param("internId") String internId, @Param("profilePicture") byte[] profilePicture);

	@Query("SELECT DISTINCT i.projectDefinitionName FROM Intern i")
	List<String> findDistinctProjectDefinitionNames();

	@Query("SELECT DISTINCT i.gender FROM Intern i")
	List<String> findDistinctGenders();

	@Query("SELECT i FROM Intern i WHERE i.group.id = :groupId")
	List<Intern> findByGroupId(@Param("groupId") Long groupId);

	@Query("SELECT i FROM Intern i WHERE i.firstName = :internName")
	Intern findByFirstName(@Param("internName") String internName);
	Intern findByInternId(String internId); // Custom query method

	List<Intern> findByFirstNameStartingWithIgnoreCase(String firstName);

	@Query("SELECT i FROM Intern i WHERE i.group = :groupId")
	List<Intern> findByGroup_GroupId(String groupId); // groupId should be a String, not Long
	@Query("SELECT i FROM Intern i WHERE i.group.groupId = :groupId")
	List<Intern> findInternsByGroupId(@Param("groupId") String groupId);

	List<Intern> findByCancellationStatusIn(List<String> statuses);

}
