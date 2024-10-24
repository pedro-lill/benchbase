package com.oltpbenchmark.benchmarks.iotbench.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.iotbench.IotBenchConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetActiveSensorsPerRoom extends Procedure {

  public final SQLStmt selectStmt =
      new SQLStmt(
          "SELECT R.name AS room_name, COUNT(S.sensorId) AS active_sensors "
              + "FROM "
              + IotBenchConstants.TABLENAME_SENSOR
              + " S "
              + "JOIN "
              + IotBenchConstants.TABLENAME_DEVICE
              + " D ON S.deviceId = D.deviceId "
              + "JOIN "
              + IotBenchConstants.TABLENAME_ROOM
              + " R ON D.room_id = R.roomId "
              + "WHERE D.status = 'Active' "
              + "GROUP BY R.name");

  public void run(Connection conn) throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, this.selectStmt)) {
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          String roomName = rs.getString("room_name");
          int activeSensors = rs.getInt("active_sensors");
        }
      }
    } catch (SQLException e) {
      System.err.println("Error retrieving active sensors per room: " + e.getMessage());
      throw e;
    }
  }
}
