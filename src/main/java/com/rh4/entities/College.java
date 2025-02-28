package com.rh4.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "college")
public class College {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "college_id")
    private long collegeId;

    @Column(name = "name")
    private String name;

    @Column(name = "location")
    private String location;

	@Column(name = "contact")
    private String contact;

	@Column(name = "email" ,unique = true)
    private String email;

	public College() {
		super();
	}

	public long getCollegeId() {
		return collegeId;
	}

	public void setCollegeId(long collegeId) {
		this.collegeId = collegeId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getContact() {return contact;}
	public void setContact(String contact) {this.contact = contact;}

	public String getEmail() {return email;}
	public void setEmail(String email) {this.email = email;}

    
}