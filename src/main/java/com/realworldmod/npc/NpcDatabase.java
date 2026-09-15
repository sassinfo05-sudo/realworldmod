package com.realworldmod.npc;

import com.realworldmod.npc.goap.DailyState;

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
import java.util.Optional;
import java.util.UUID;

/**
 * Thin, dependency-free wrapper around a per-world SQLite database holding
 * the persistent citizen population (Section 2 of the design doc: "Persistent
 * Local SQLite Citizen Database").
 *
 * <p>Not thread-safe by itself; callers on the server tick thread should be
 * the only writers. Intentionally uses plain JDBC rather than an ORM to keep
 * the mod's runtime dependency footprint (and jar size) small.
 */
public final class NpcDatabase implements AutoCloseable {
    private final Path dbFile;
    private Connection connection;

    public NpcDatabase(Path dbFile) {
        this.dbFile = dbFile;
    }

    public void open() {
        try {
            Files.createDirectories(dbFile.getParent());
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.toAbsolutePath());
            createSchema();
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Failed to open citizen database at " + dbFile, e);
        }
    }

    private void createSchema() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS citizens (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    home_address TEXT,
                    workplace_address TEXT,
                    income_cents_per_pay_period INTEGER NOT NULL DEFAULT 0,
                    work_start_hour INTEGER NOT NULL DEFAULT 9,
                    work_end_hour INTEGER NOT NULL DEFAULT 17,
                    current_state TEXT NOT NULL DEFAULT 'SLEEPING'
                )
                """);
        }
    }

    public void upsert(NpcProfile profile) {
        String sql = """
            INSERT INTO citizens (id, name, home_address, workplace_address,
                income_cents_per_pay_period, work_start_hour, work_end_hour, current_state)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                name = excluded.name,
                home_address = excluded.home_address,
                workplace_address = excluded.workplace_address,
                income_cents_per_pay_period = excluded.income_cents_per_pay_period,
                work_start_hour = excluded.work_start_hour,
                work_end_hour = excluded.work_end_hour,
                current_state = excluded.current_state
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, profile.id().toString());
            statement.setString(2, profile.name());
            statement.setString(3, profile.homeAddress());
            statement.setString(4, profile.workplaceAddress());
            statement.setLong(5, profile.incomeCentsPerPayPeriod());
            statement.setInt(6, profile.workStartHour());
            statement.setInt(7, profile.workEndHour());
            statement.setString(8, profile.currentState().name());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to upsert citizen " + profile.id(), e);
        }
    }

    public void updateState(UUID citizenId, DailyState newState) {
        try (PreparedStatement statement =
                     connection.prepareStatement("UPDATE citizens SET current_state = ? WHERE id = ?")) {
            statement.setString(1, newState.name());
            statement.setString(2, citizenId.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update state for citizen " + citizenId, e);
        }
    }

    public Optional<NpcProfile> findById(UUID citizenId) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM citizens WHERE id = ?")) {
            statement.setString(1, citizenId.toString());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(readRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load citizen " + citizenId, e);
        }
    }

    public List<NpcProfile> findAll() {
        List<NpcProfile> results = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM citizens")) {
            while (rs.next()) {
                results.add(readRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load citizens", e);
        }
        return results;
    }

    private NpcProfile readRow(ResultSet rs) throws SQLException {
        return new NpcProfile(
                UUID.fromString(rs.getString("id")),
                rs.getString("name"),
                rs.getString("home_address"),
                rs.getString("workplace_address"),
                rs.getLong("income_cents_per_pay_period"),
                rs.getInt("work_start_hour"),
                rs.getInt("work_end_hour"),
                DailyState.valueOf(rs.getString("current_state"))
        );
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to close citizen database", e);
            }
        }
    }
}
