package com.realworldmod.economy;

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

/** SQLite-backed persistence for player/NPC bank balances, one row per account holder. */
public final class BankDatabase implements AutoCloseable {
    private final Path dbFile;
    private Connection connection;

    public BankDatabase(Path dbFile) {
        this.dbFile = dbFile;
    }

    public void open() {
        try {
            Files.createDirectories(dbFile.getParent());
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.toAbsolutePath());
            createSchema();
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Failed to open bank database at " + dbFile, e);
        }
    }

    private void createSchema() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS accounts (
                    owner_id TEXT PRIMARY KEY,
                    balance_cents INTEGER NOT NULL DEFAULT 0
                )
                """);
        }
    }

    public void setBalance(UUID ownerId, long balanceCents) {
        String sql = """
            INSERT INTO accounts (owner_id, balance_cents)
            VALUES (?, ?)
            ON CONFLICT(owner_id) DO UPDATE SET balance_cents = excluded.balance_cents
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ownerId.toString());
            statement.setLong(2, balanceCents);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to set balance for " + ownerId, e);
        }
    }

    public Map<UUID, Long> findAll() {
        Map<UUID, Long> results = new LinkedHashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM accounts")) {
            while (rs.next()) {
                results.put(UUID.fromString(rs.getString("owner_id")), rs.getLong("balance_cents"));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load accounts", e);
        }
        return results;
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to close bank database", e);
            }
        }
    }
}
