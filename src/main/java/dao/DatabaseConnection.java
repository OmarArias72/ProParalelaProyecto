package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static volatile DatabaseConnection instance;
    private Connection connection;

    private String URL="jdbc:mysql://localhost:3306/quizz";
    private String USER="root";
    private String PASSWORD="48v01jL3/7)32&";
    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(URL,USER,PASSWORD);
            System.out.println("Conexión a la base de datos establecida.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static DatabaseConnection getInstance() {
        if(instance==null) {
            synchronized (DatabaseConnection.class) {
                if(instance==null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
