package ch.concertticketwatcherengine.component.repository;

import ch.concertticketwatcherengine.component.model.User;
import ch.concertticketwatcherengine.core.generic.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserRepository extends Repository<User> {

    @Override
    protected User mapSqlRowToModel(ResultSet rs) throws SQLException {
        User user = new User();
        user.id       = rs.getString("id");
        user.username = rs.getString("username");
        user.email    = rs.getString("email");
        user.city     = rs.getString("city");
        return user;
    }

    public List<User> getAll() throws Exception {
        List<User> users = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery("SELECT * FROM users")) {
            while (rs.next()) users.add(mapSqlRowToModel(rs));
        }
        return users;
    }

    public User getById(UUID id) throws Exception {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapSqlRowToModel(rs);
            }
        }
        return null;
    }

    public User getByUsername(String username) throws Exception {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapSqlRowToModel(rs);
            }
        }
        return null;
    }

    public void save(User user) throws Exception {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO users (id, username, email, city) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, user.username);
            stmt.setString(3, user.email);
            stmt.setString(4, user.city);
            stmt.executeUpdate();
        }
    }

    public void deleteById(UUID id) throws Exception {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        }
    }

    public void updateById(User user, UUID id) throws Exception {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE users SET username = ?, email = ?, city = ? WHERE id = ?")) {
            stmt.setString(1, user.username);
            stmt.setString(2, user.email);
            stmt.setString(3, user.city);
            stmt.setObject(4, id);
            stmt.executeUpdate();
        }
    }
}