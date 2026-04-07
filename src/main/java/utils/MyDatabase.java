package utils;

import tn.smartfight.database.DBConnection;
import java.sql.Connection;

/**
 * Shim for backward-compatibility with the login module's utils.MyDatabase.
 * Delegates to M5's singleton DBConnection (connects to smartfight DB).
 */
public class MyDatabase {

    private static MyDatabase instance;

    private MyDatabase() {}

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }
}
