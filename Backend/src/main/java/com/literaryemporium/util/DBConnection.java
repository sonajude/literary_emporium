package com.literaryemporium.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/literary_emporium";

    private static final String DEFAULT_USER = "root";

    private static final String DEFAULT_PASSWORD = "";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : DEFAULT_URL;
            String user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : DEFAULT_USER;
            String password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : DEFAULT_PASSWORD;

            return DriverManager.getConnection(url, user, password);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}