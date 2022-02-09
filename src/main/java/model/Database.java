package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final Database instance = new Database();
    public StringProperty host = new SimpleStringProperty();
    public StringProperty database = new SimpleStringProperty();
    public StringProperty username = new SimpleStringProperty();
    public StringProperty password = new SimpleStringProperty();
    public Connection conn;

    private Database() {
    }

    public static Database getInstance() {
        return instance;
    }


    /**
     * Connects the user to the database.
     *
     * @return true if connection is established.
     */
    public boolean connect() {
        String url = "jdbc:mysql://";
        url += host.get();
        url += "/";
        url += database.get();
        url += "?useSSL=false";

        try {
            conn = DriverManager.getConnection(url, username.get(), password.get());
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Connection conn = Database.getInstance().conn;
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }));
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
