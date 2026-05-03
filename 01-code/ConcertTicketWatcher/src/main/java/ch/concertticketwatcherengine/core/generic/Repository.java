package ch.concertticketwatcherengine.core.generic;

import ch.concertticketwatcherengine.core.exception.DatabaseException;
import ch.concertticketwatcherengine.core.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public abstract class Repository<M extends Model> {

    private static String url;
    private static String user;
    private static String password;

    /**
     * Sets the database credentials and automatically runs init.sql to set up the schema.
     */
    public static void setDatabaseCredentials(String url, String user, String password) {
        Repository.url = url;
        Repository.user = user;
        Repository.password = password;
        runInitSql();
    }

    /**
     * Automatically runs init.sql from resources on startup.
     * Also creates tables if they don't exist yet.
     */
    private static void runInitSql() {
        try (InputStream is = Repository.class.getClassLoader().getResourceAsStream("init.sql")) {
            if (is == null) {
                throw new DatabaseException("runInitSql", "init.sql not found in resources");
            }
            String sql = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));

            try (Connection conn = DriverManager.getConnection(url, user, password);
                 Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                Log.success("Database schema initialized successfully.");
            }
        } catch (DatabaseException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(new DatabaseException("runInitSql", e.getMessage()));
        }
    }

    /**
     * Returns a connection to the PostgreSQL database.
     * Use this in your subclass methods with try-with-resources to auto-close it.
     */
    protected final Connection getConnection() throws DatabaseException {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            throw new DatabaseException("getConnection", e.getMessage());
        }
    }

    /**
     * Maps a single ResultSet row to your Model object.
     */
    protected abstract M mapSqlRowToModel(ResultSet resultSet) throws SQLException;
}