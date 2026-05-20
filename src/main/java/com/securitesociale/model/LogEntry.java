package com.securitesociale.model;

import java.time.LocalDateTime;

public class LogEntry {

    private int id;
    private String utilisateur;
    private String action;
    private String details;
    private String ipAddress;
    private LocalDateTime createdAt;

    public LogEntry() {}

    public int getId() { return id; }
    public void setId(int v) { this.id = v; }

    public String getUtilisateur() { return utilisateur; }
    public void setUtilisateur(String v) { this.utilisateur = v; }

    public String getAction() { return action; }
    public void setAction(String v) { this.action = v; }

    public String getDetails() { return details; }
    public void setDetails(String v) { this.details = v; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String v) { this.ipAddress = v; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
