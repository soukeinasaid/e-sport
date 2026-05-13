package org.example.utils;

import org.example.entities.User;

/**
 * Simple in-memory session holder — stores the currently logged-in user.
 */
public class SessionManager {

    private static User currentUser = null;

    private SessionManager() {}

    public static void login(User user)  { currentUser = user; }
    public static void logout()          { currentUser = null; }
    public static User getCurrentUser()  { return currentUser; }
    public static boolean isLoggedIn()   { return currentUser != null; }
    public static boolean isAdmin()      {
        return currentUser != null && currentUser.getRole() == User.Role.ADMIN;
    }
}
