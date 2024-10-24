package com.oltpbenchmark.benchmarks.iotbench.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.iotbench.IotBenchConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertSensorLogRecord extends Procedure {

  public final SQLStmt insertLogStmt =
      new SQLStmt(
          "INSERT INTO "
              + IotBenchConstants.TABLENAME_SENSOR_LOG
              + " (sensor_id, value, date) VALUES (?, ?, NOW())");

  public void run(Connection conn, int sensorId, double sensorValue) throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, this.insertLogStmt)) {
      stmt.setInt(1, sensorId);
      stmt.setDouble(2, sensorValue);
      stmt.executeUpdate();
    } catch (SQLException e) {
      System.err.println("Error inserting sensor log: " + e.getMessage());
      throw e;
    }
  }
}
