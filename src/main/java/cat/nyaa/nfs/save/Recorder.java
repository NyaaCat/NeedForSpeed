package cat.nyaa.nfs.save;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Recorder {
    private final List<String> initializeSQLStatements = List.of(
            """
                    CREATE TABLE IF NOT EXIST "record" (
                    	"id"	INTEGER NOT NULL UNIQUE,
                    	"created_time"	INTEGER,
                    	"objective_uuid"	TEXT,
                    	"player_uuid"	TEXT,
                    	"time_in_millisecond"	INTEGER,
                    	"record_detail"	TEXT,
                    	PRIMARY KEY("id" AUTOINCREMENT)
                    );
                    """,
            """
                    CREATE INDEX IF NOT EXIST "player_objective_index" ON "record" (
                    	"player_uuid",
                    	"objective_uuid"
                    );
                    """,
            """
                    CREATE INDEX IF NOT EXIST "objective_index" ON "record" (
                    	"objective_uuid"
                    );
                    """,
            """
                    CREATE INDEX IF NOT EXIST "player_index" ON "record" (
                    	"player_uuid"
                    );
                    """
    );

    private Connection connection = getConnection();
    private final File sqlFile;

    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + sqlFile.getAbsolutePath());
        }
        return connection;
    }


    public Recorder(File sqlFile) throws SQLException, IOException {
        this.sqlFile = sqlFile;
        if (!sqlFile.exists()) {
            sqlFile.createNewFile();
        }
        Statement statement = getConnection().createStatement();
        for (String sql : initializeSQLStatements) {
            statement.execute(sql);
        }
    }

    public void record(PlayerRecord playerRecord) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement("INSERT INTO record (created_time, objective_uuid, player_uuid, time_in_millisecond, record_detail) VALUES (?, ?, ?, ?, ?)")) {
            statement.setLong(1, playerRecord.createdTime());
            statement.setString(2, playerRecord.objectiveUUID().toString());
            statement.setString(3, playerRecord.playerUUID().toString());
            statement.setLong(4, playerRecord.timeInMillisecond());
            statement.setString(5, playerRecord.recordDetail());
            statement.execute();
        }
    }

    public PlayerRecord getBestPlayerRecord(UUID playerUUID, UUID objectiveUUID) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM record WHERE player_uuid = ? AND objective_uuid = ? ORDER BY time_in_millisecond ASC LIMIT 1")) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, objectiveUUID.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new PlayerRecord(
                        resultSet.getInt("id"),
                        resultSet.getLong("created_time"),
                        UUID.fromString(resultSet.getString("objective_uuid")),
                        UUID.fromString(resultSet.getString("player_uuid")),
                        resultSet.getLong("time_in_millisecond"),
                        resultSet.getString("record_detail")
                );
            }
        }
        return null;
    }

    public List<PlayerRecord> getBestNumberOfRecordsOfObjective(UUID objectiveUUID, int numberOfRecords) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM record WHERE objective_uuid = ? ORDER BY time_in_millisecond ASC LIMIT ?")) {
            statement.setString(1, objectiveUUID.toString());
            statement.setInt(2, numberOfRecords);
            ResultSet resultSet = statement.executeQuery();
            List<PlayerRecord> records = new ArrayList<>();
            while (resultSet.next()) {
                records.add(new PlayerRecord(
                        resultSet.getInt("id"),
                        resultSet.getLong("created_time"),
                        UUID.fromString(resultSet.getString("objective_uuid")),
                        UUID.fromString(resultSet.getString("player_uuid")),
                        resultSet.getLong("time_in_millisecond"),
                        resultSet.getString("record_detail")
                ));
            }
            return records;
        }
    }

    public void shutdown() throws SQLException {
        connection.close();
    }


}
