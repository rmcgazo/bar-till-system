package com.ryan.bartill.app;

import com.ryan.bartill.model.Staff;

public final class Session {
    private static Staff currentUser;

    private Session() {}

    public static Staff getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Staff user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }
}