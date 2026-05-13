package org.example.entities;

public class User {

    public enum Role { ADMIN, USER }

    private int    id;
    private String username;
    private String email;
    private String password;   // plain-text for simplicity
    private Role   role;

    public User() {}

    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email    = email;
        this.password = password;
        this.role     = role;
    }

    public User(int id, String username, String email, String password, Role role) {
        this.id       = id;
        this.username = username;
        this.email    = email;
        this.password = password;
        this.role     = role;
    }

    public int    getId()       { return id; }
    public void   setId(int id) { this.id = id; }

    public String getUsername()            { return username; }
    public void   setUsername(String u)    { this.username = u; }

    public String getEmail()               { return email; }
    public void   setEmail(String e)       { this.email = e; }

    public String getPassword()            { return password; }
    public void   setPassword(String p)    { this.password = p; }

    public Role   getRole()                { return role; }
    public void   setRole(Role r)          { this.role = r; }

    @Override
    public String toString() { return username + " [" + role + "]"; }
}
