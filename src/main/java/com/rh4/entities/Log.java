package com.rh4.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "log")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "intern_id")
    private String internId;

    @Column(name = "actionType")
    private String actionType;

    @Column(name = "details")
    private String details;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    public Log() {
        this.timestamp = LocalDateTime.now();
    }

    public Log(String internId, String actionType, String details) {
        this.internId = internId;
        this.actionType = actionType;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInternId() { return internId; }
    public void setInternId(String internId) { this.internId = internId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

}