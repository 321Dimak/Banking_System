package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcConnector {

    private static final String url = "jdbc:postgresql://localhost:5432/banking_system";
    private static final String username = "postgres";
    private static final String password = "Mersedes1";

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

}