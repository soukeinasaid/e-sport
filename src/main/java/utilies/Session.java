package utilies;

import entity.User;

public class Session {

    private static User currentUser;

    public static void setUser(User user) {
        currentUser = user;
    }

    public static User getUser() {
        return currentUser;
    }

    public static int getUserId() {
        return currentUser != null ? currentUser.getIdUser() : -1;
    }
}