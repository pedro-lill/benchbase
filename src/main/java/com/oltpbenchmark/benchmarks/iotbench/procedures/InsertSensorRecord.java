package com.oltpbenchmark.benchmarks.iotbench.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.iotbench.IotBenchConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertSensorRecord extends Procedure {
  // SQL Statement para inserir registros na tabela Sensor
  public final SQLStmt insertStmt =
      new SQLStmt(
          "INSERT INTO "
              + IotBenchConstants.TABLENAME_SENSOR
              + " (sensorId, name, type, value, deviceId) VALUES (?, ?, ?, ?, ?)");

  public void run(
      Connection conn,
      int sensorId,
      String name,
      int type,
      double value,
      int deviceId) // Alterado type para int
      throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, this.insertStmt)) {
      stmt.setInt(1, sensorId);
      stmt.setString(2, name);
      stmt.setInt(3, type); // Usando setInt para o tipo
      stmt.setDouble(4, value);
      stmt.setInt(5, deviceId);
      stmt.executeUpdate();
    } catch (SQLException e) {
      System.err.println("Error inserting sensor record: " + e.getMessage());
      throw e; // Re-throwing the exception after logging it
    }
  }
}
