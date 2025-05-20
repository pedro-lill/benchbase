package com.oltpbenchmark.benchmarks.iotbench.procedures;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.iotbench.IotBenchConstants;

public class GetRoomOverview extends Procedure {

  private static final Logger LOG = LoggerFactory.getLogger(GetRoomOverview.class);

  public final SQLStmt selectDevicesStmt =
      new SQLStmt(
          String.format(
              "SELECT device_id, name, status FROM %s WHERE room_id = ?",
              IotBenchConstants.TABLENAME_DEVICE));

  public void run(Connection conn, int roomId) throws SQLException {
    Map<String, String> devices = new HashMap<>();
    try (PreparedStatement stmt = this.getPreparedStatement(conn, selectDevicesStmt)) {
      stmt.setInt(1, roomId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          int deviceId = rs.getInt("device_id");
          String deviceName = rs.getString("name");
          String deviceStatus = rs.getString("status");
          devices.put(deviceName, deviceStatus);
        }
        LOG.debug("Visão geral do quarto {}: {}", roomId, devices);
      }
    } catch (SQLException e) {
      LOG.error("Erro ao obter visão geral do quarto {}: {}", roomId, e.getMessage());
      throw e;
    }
  }
}
