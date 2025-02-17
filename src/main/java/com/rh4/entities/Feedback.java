package com.rh4.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "intern_feedback")
public class Feedback {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String internId;
        private String firstName;
        private String lastName;
        private String environment;
        private String facilities;
        private String interaction;
        private String materials;
        private String response;
        private String experience;
        private String researchAreas;
        private String topics;
        private String magazine;
        private LocalDate feedbackDate;
        private String studentSign;
        private String message;

//        @ManyToOne
//        @JoinColumn(name = "intern_id", nullable = false)
//        private Intern intern;

        public Feedback() {

        }

        public Feedback(String environment, String facilities, String interaction,String materials,
                        String response,String experience, String researchAreas, String topics, String magazine,
                        LocalDate feedbackDate , Intern intern,String message)
        {
            this.environment = environment;
            this.facilities = facilities;
            this.interaction = interaction;
            this.materials = materials;
            this.response = response;
            this.experience = experience;
            this.researchAreas = researchAreas;
            this.topics = topics;
            this.magazine = magazine;
            this.feedbackDate = feedbackDate;
            this.message = message;



            this.internId = internId;
            this.firstName = firstName;
            this.lastName = lastName;

        }


    public Long getId() {
            return id;
    }
    public void setId(Long id) {
            this.id = id;
    }

    public String getInternId() {
        return internId;
    }

    public void setInternId(String internId) {
        this.internId = internId;
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEnvironment() {
            return environment;
    }

    public void setEnvironment(String environment) {
            this.environment = environment;
    }
    public String getFacilities() {
            return facilities;
    }
    public void setFacilities(String facilities) {
            this.facilities = facilities;

    }

    public String getInteraction() {
            return interaction;

    }
    public void setInteraction(String interaction) {
            this.interaction = interaction;
    }
    public String getMaterials() {
            return materials;
        }
     public void setMaterials(String materials) {
        this.materials = materials;
   }
   public String getResponse() {
            return response;
   }
   public void setResponse(String response) {
            this.response = response;
   }
   public String getExperience() {
            return experience;
   }
   public void setExperience(String experience) {
            this.experience = experience;
   }
   public String getResearchAreas() {
            return researchAreas;
   }
   public void setResearchAreas(String researchAreas) {
            this.researchAreas = researchAreas;

   }
   public String getTopics() {
            return topics;
   }
   public void setTopics(String topics) {
            this.topics = topics;
   }
   public String getMagazine() {
            return magazine;

   }
   public void setMagazine(String magazine) {
            this.magazine = magazine;
   }
   public LocalDate getFeedbackDate() {
            return feedbackDate;
   }
   public void setFeedbackDate(LocalDate feedbackDate) {
            this.feedbackDate = feedbackDate;
   }
   public String getStudentSign() {
            return studentSign;

   }
   public void setStudentSign(String studentSign) {
            this.studentSign = studentSign;
   }

   public void setMessage(String message) {
            this.message = message;
   }
   public String getMessage() {
            return message;
   }

}
