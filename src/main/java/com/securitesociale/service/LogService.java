package com.securitesociale.service;

import com.securitesociale.dao.LogDAO;
import com.securitesociale.model.LogEntry;

import java.util.List;

public class LogService {

    private final LogDAO logDAO = new LogDAO();

    public List<LogEntry> findAll() {
        return logDAO.findAll();
    }

    public int count() {
        return logDAO.count();
    }
}
