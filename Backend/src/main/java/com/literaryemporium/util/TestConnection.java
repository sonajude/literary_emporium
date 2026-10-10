package com.literaryemporium.util;

import java.sql.Connection;

public class TestConnection {

    public static void main(String[] args) {

        Connection connection = DBConnection.getConnection();

        if (connection != null) {
            System.out.println("Database Connected Successfully!");
        } else {
            System.out.println("Database Connection Failed!");
        }
    }
}