package com.realworldmod.property;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** SQLite-backed persistence for land claims/deeds, one row per plot. */
public final class ClaimDatabase implements AutoCloseable {
    private final Path dbFile;
    private Connection connection;

    public ClaimDatabase(Path dbFile) {
        this.dbFile = dbFile;
    }

    public void open() {
        try {
            Files.createDirectories(dbFile.getParent());
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.toAbsolutePath());
            createSchema();
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Failed to open claim database at " + dbFile, e);
        }
    }

    private void createSchema() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS claims (
                    id TEXT PRIMARY KEY,
                    owner_id TEXT NOT NULL,
                    min_x INTEGER NOT NULL,
                    min_z INTEGER NOT NULL,
                    max_x INTEGER NOT NULL,
                    max_z INTEGER NOT NULL
                )
                """);
        }
    }

    public void insert(Claim claim) {
        String sql = """
            INSERT INTO claims (id, owner_id, min_x, min_z, max_x, max_z)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                owner_id = excluded.owner_id,
                min_x = excluded.min_x,
                min_z = excluded.min_z,
                max_x = excluded.max_x,
                max_z = excluded.max_z
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, claim.id());
            statement.setString(2, claim.ownerId().toString());
            statement.setInt(3, claim.minX());
            statement.setInt(4, claim.minZ());
            statement.setInt(5, claim.maxX());
            statement.setInt(6, claim.maxZ());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert claim " + claim.id(), e);
        }
    }

    public List<Claim> findAll() {
        List<Claim> results = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM claims")) {
            while (rs.next()) {
                results.add(new Claim(
                        rs.getString("id"),
                        UUID.fromString(rs.getString("owner_id")),
                        rs.getInt("min_x"),
                        rs.getInt("min_z"),
                        rs.getInt("max_x"),
                        rs.getInt("max_z")
                ));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load claims", e);
        }
        return results;
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to close claim database", e);
            }
        }
    }
}
