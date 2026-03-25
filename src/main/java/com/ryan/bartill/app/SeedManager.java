package com.ryan.bartill.app;

import com.ryan.bartill.dao.StaffDao;
import com.ryan.bartill.model.Role;

public class SeedManager {
    public static void main(String[] args) throws Exception {
        StaffDao dao = new StaffDao();

        // Change these if you want
        String username = "manager";
        String password = "manager123";
        String pin = "01";

        try {
            dao.createStaff(username, password, Role.MANAGER, pin);
            System.out.println("Created manager: " + username + " / " + password);
        } catch (org.sqlite.SQLiteException e) {
            // If username already exists, SQLite throws constraint error
            System.out.println("Manager already exists (username taken): " + username);
        }
    }
}