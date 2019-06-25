package com.xingtb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConn {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        String JDBC_DRIVER = "com.mysql.jdbc.Driver";
        String DB_URL = "jdbc:mysql://model-mysql.internal.gridx.com:3306/test?useUnicode=true&characterEncoding=UTF-8";
        String USER = "xiang";
        String PASS = "uAHgYONuG4";
        Class.forName(JDBC_DRIVER);
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        System.out.println(conn);
    }
}
