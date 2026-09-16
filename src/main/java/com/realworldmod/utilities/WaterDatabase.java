package com.realworldmod.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** SQLite-backed persistence for water hookup state, one row per account holder — the water counterpart to {@link UtilityDatabase}. */
public final class WaterDatabase implements AutoCloseable {
    private final Path dbFile;
    private Connection connection;

    public WaterDatabase(Path dbFile) {
        this.dbFile = dbFile;
    }

    public void open() {
        try {
            Files.createDirectories(dbFile.getParent());
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.toAbsolutePath());
            createSchema();
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Failed to open water database at " + dbFile, e);
        }
    }

    private void createSchema() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS water_accounts (
                    owner_id TEXT PRIMARY KEY,
                    connected INTEGER NOT NULL DEFAULT 1,
                    unpaid_cents INTEGER NOT NULL DEFAULT 0
                )
                """);
        }
    }

    public void setState(UUID ownerId, WaterState state) {
        String sql = """
            INSERT INTO water_accounts (owner_id, connected, unpaid_cents)
            VALUES (?, ?, ?)
            ON CONFLICT(owner_id) DO UPDATE SET
                connected = excluded.connected,
                unpaid_cents = excluded.unpaid_cents
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ownerId.toString());
            statement.setInt(2, state.connected() ? 1 : 0);
            statement.setLong(3, state.unpaidCents());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to set water state for " + ownerId, e);
        }
    }

    public Map<UUID, WaterState> findAll() {
        Map<UUID, WaterState> results = new LinkedHashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM water_accounts")) {
            while (rs.next()) {
                results.put(UUID.fromString(rs.getString("owner_id")),
                        new WaterState(rs.getInt("connected") != 0, rs.getLong("unpaid_cents")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load water accounts", e);
        }
        return results;
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to close water database", e);
            }
        }
    }
}
