package org.example.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class mydatabase {
    private static final String URL = "jdbc:mysql://localhost:3306/esport_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = ""; // empty by default on XAMPP

    private Connection connection;
    private static mydatabase instance;

    private mydatabase() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connection established to tournoi");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static mydatabase getInstance() {
        if (instance == null) {
            instance = new mydatabase();
        }
        return instance;
    }

    public Connection getCon() {
        return connection;
    }
}