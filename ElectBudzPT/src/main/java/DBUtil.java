package com.mycompany.electbudzpt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    // Utility class to manage MySQL connection
    private static final String URL = "jdbc:mysql://localhost:3306/elect_budz";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
