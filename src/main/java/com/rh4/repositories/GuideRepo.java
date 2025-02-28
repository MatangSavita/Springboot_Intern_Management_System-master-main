package com.rh4.repositories;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rh4.entities.Guide;
@Repository
public interface GuideRepo extends JpaRepository<Guide, Long>{

	Guide findByEmailId(String emailId);
	public Optional<Guide> findByName(String name);

//	List<Guide> findByGroupsContaining(String group);


	@Query("SELECT g FROM Guide g JOIN g.groups gr WHERE gr.groupId = :groupId")
	List<Guide> findByGroupId(@Param("groupId") String groupId);

//	@Query("SELECT g FROM Guide g WHERE g.guideId = :id")
//	Guide findByGuideId(@Param("id") String id);


//	List<Guide> findByEmailId(@Email String emailId);

}
