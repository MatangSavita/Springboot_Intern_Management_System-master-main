package com.rh4.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
public class Announcement {

            @Id
            @GeneratedValue(strategy= GenerationType.IDENTITY)
            private int id;
            private String title;
            private String content;
            @Column(updatable = false)
            private LocalDateTime createdAt = LocalDateTime.now();
            private String type;
//            private boolean viewed = false;

            @Column(columnDefinition = "boolean default false")
            private boolean viewed;

            public Announcement() {}

            public Announcement(String title, String content, LocalDateTime createdAt , String type) {
                this.title = title;
                this.content = content;
                this.createdAt = createdAt;
                this.type = type;
            }



            public int getId() {
                return id;
            }
            public void setId(int id) {
                this.id = id;
            }
            public String getTitle() {
                return title;
            }
            public void setTitle(String title) {
                this.title = title;
            }
            public String getContent() {
                return content;
            }
            public void setContent(String content) {
                this.content = content;
            }
            public LocalDateTime getCreatedAt() {
                return createdAt;
            }
            public void setCreatedAt(LocalDateTime createdAt) {
                this.createdAt = createdAt;
            }

            public String getType() {
                return type;
            }
            public void setType(String type) {
                this.type = type;
            }

            public boolean isViewed() {
                return viewed;
            }

            public void setViewed(boolean viewed) {
                this.viewed = viewed;
            }

    }
