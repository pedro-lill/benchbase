package com.oltpbenchmark.benchmarks.iotbench.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.iotbench.IotBenchConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetSensorsAndDevicesFromRoom extends Procedure {

  public final SQLStmt getDevicesFromRoom =
      new SQLStmt(
          "SELECT deviceId FROM " + IotBenchConstants.TABLENAME_DEVICE + " WHERE room_id = ?");

  public final SQLStmt getSensorsFromDevices =
      new SQLStmt("SELECT * FROM " + IotBenchConstants.TABLENAME_SENSOR + "WHERE deviceId IN (??)");

  public void run(Connection conn, int roomId) throws SQLException {

    try (PreparedStatement getDevicesStmt = this.getPreparedStatement(conn, getDevicesFromRoom)) {
      getDevicesStmt.setInt(1, roomId);
      try (ResultSet devicesResult = getDevicesStmt.executeQuery()) {

        try (PreparedStatement getSensorsStmt =
            this.getPreparedStatement(conn, getSensorsFromDevices)) {
          int ctr = 0;
          long lastDeviceId = -1;

          while (devicesResult.next() && ctr++ < IotBenchConstants.LIMIT_DEVICES) {
            lastDeviceId = devicesResult.getLong("deviceId");
            getSensorsStmt.setLong(ctr, lastDeviceId);
          }

          if (ctr > 0) {
            while (ctr++ < IotBenchConstants.LIMIT_DEVICES) {
              getSensorsStmt.setLong(ctr, lastDeviceId);
            }

            try (ResultSet sensorsResult = getSensorsStmt.executeQuery()) {
              while (sensorsResult.next()) {
                int sensorId = sensorsResult.getInt("sensorId");
                String sensorName = sensorsResult.getString("name");
                int sensorType = sensorsResult.getInt("type");
                double sensorValue = sensorsResult.getDouble("value");
              }
            }
          } else {

          }
        }
      }
    }
  }
}
