package com.oltpbenchmark.benchmarks.iotbench.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.iotbench.IotBenchConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertActionLogRecord extends Procedure {

  public final SQLStmt insertStmt =
      new SQLStmt(
          "INSERT INTO "
              + IotBenchConstants.TABLENAME_ACTION_LOGS
              + " (log_id, user_id, device_id, action, status, date) VALUES (?, ?, ?, ?, ?, now())");

  public void run(
      Connection conn, int logId, int userId, int deviceId, String action, String status)
      throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, this.insertStmt)) {
      stmt.setInt(1, logId);
      stmt.setInt(2, userId);
      stmt.setInt(3, deviceId);
      stmt.setString(4, action);
      stmt.setString(5, status);
      stmt.executeUpdate();
    } catch (SQLException e) {
      System.err.println("Error inserting action log record: " + e.getMessage());
      throw e;
    }
  }
}
