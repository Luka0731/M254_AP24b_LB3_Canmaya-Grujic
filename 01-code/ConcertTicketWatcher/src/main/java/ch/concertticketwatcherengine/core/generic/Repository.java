package ch.concertticketwatcherengine.core.generic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Repository<M extends Model> {

    private static String url;
    private static String user;
    private static String password;

    public static void setDatabaseCredentials(String url, String user, String password) {
        Repository.url = url;
        Repository.user = user;
        Repository.password = password;
    }

    /**
     * Returns a connection to the PostgreSQL database.
     * Use this in your subclass methods with try-with-resources to auto-close it.
     *
     * @return a live JDBC Connection
     */
    protected final Connection getConnection() throws Exception {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Maps a single ResultSet row to your Model object.
     * Called internally by your query methods
     *
     * @param resultSet the current row from the ResultSet
     * @return the mapped Model object
     */
    protected abstract M mapSqlRowToModel(ResultSet resultSet) throws SQLException;
}