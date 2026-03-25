package com.ryan.bartill.model;

public class Staff {
    private final int id;
    private final String username;
    private final String passwordHash;
    private final Role role;
    private final boolean active;

    public Staff(int id, String username, String passwordHash, Role role, boolean active) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }
}