package com.oltpbenchmark.benchmarks.benchio.procedures;

import com.oltpbenchmark.benchmarks.benchio.BenchIOConstants;
import com.oltpbenchmark.benchmarks.benchio.BenchIOWorker;
import com.oltpbenchmark.types.TransactionStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class LogSensorData extends BenchIOProcedure {

  @Override
  public TransactionStatus run(
      Connection conn,
      Random random,
      int terminalHubID,
      int numHubs,
      int terminalRoomLowerID,
      int terminalRoomUpperID,
      BenchIOWorker worker)
      throws SQLException {

    try (PreparedStatement stmt =
        conn.prepareStatement(
            "INSERT INTO "
                + BenchIOConstants.TABLENAME_SENSORLOG
                + " (sensor_id, value, date) VALUES (?, ?, NOW())")) {

      // Seleciona um sensor aleatório dentro da partição do worker
      int sensorId =
          terminalRoomLowerID + random.nextInt(terminalRoomUpperID - terminalRoomLowerID);
      double sensorValue = random.nextDouble() * 100; // Valor entre 0 e 100

      stmt.setInt(1, sensorId);
      stmt.setDouble(2, sensorValue);
      stmt.executeUpdate();

      return TransactionStatus.SUCCESS;
    } catch (SQLException e) {
      throw e;
    }
  }
}
