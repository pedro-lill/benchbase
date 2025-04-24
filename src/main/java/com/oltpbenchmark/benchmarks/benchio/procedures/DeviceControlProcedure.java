package com.oltpbenchmark.benchmarks.benchio.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.benchio.BenchIOConfig;
import com.oltpbenchmark.benchmarks.benchio.BenchIOConstants;
import com.oltpbenchmark.types.TransactionStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceControlProcedure extends Procedure {

  private static final Logger LOG = LoggerFactory.getLogger(DeviceControlProcedure.class);

  // SQL Statements
  public final SQLStmt getRandomDeviceStmt =
      new SQLStmt(
          "SELECT device_id, type, status FROM "
              + BenchIOConstants.TABLENAME_DEVICE
              + " WHERE room_id BETWEEN ? AND ? ORDER BY RAND() LIMIT 1");

  public final SQLStmt updateDeviceStmt =
      new SQLStmt(
          "UPDATE "
              + BenchIOConstants.TABLENAME_DEVICE
              + " SET status = ?, last_updated = NOW() WHERE device_id = ?");

  public final SQLStmt logActionStmt =
      new SQLStmt(
          "INSERT INTO "
              + BenchIOConstants.TABLENAME_ACTIONLOGS
              + " (user_id, device_id, action, status, date) VALUES (?, ?, ?, ?, ?)");

  public TransactionStatus run(
      Connection conn,
      Random gen,
      int terminalHubID,
      int terminalRoomLowerID,
      int terminalRoomUpperID)
      throws SQLException {

    try {
      // 1. Selecionar dispositivo aleatório na faixa de salas
      DeviceInfo device = getRandomDevice(conn, terminalRoomLowerID, terminalRoomUpperID);
      if (device == null) {
        LOG.warn("No devices found in rooms {}-{}", terminalRoomLowerID, terminalRoomUpperID);
        return TransactionStatus.RETRY;
      }

      // 2. Gerar ação baseada no tipo de dispositivo
      DeviceAction action = generateDeviceAction(gen, device.type);
      if (action == null || "NO_ACTION".equals(action.command)) {
        return TransactionStatus.SUCCESS;
      }

      // 3. Executar ação
      updateDeviceStatus(conn, device.deviceId, action.newStatus);

      // 4. Registrar ação
      logDeviceAction(
          conn, gen.nextInt(BenchIOConfig.configUserCount) + 1, device.deviceId, action);

      if (LOG.isDebugEnabled()) {
        LOG.debug("Device {} set to {} via {}", device.deviceId, action.newStatus, action.command);
      }

      return TransactionStatus.SUCCESS;

    } catch (SQLException e) {
      LOG.error("Error controlling device", e);
      throw e;
    }
  }

  private DeviceInfo getRandomDevice(Connection conn, int roomLower, int roomUpper)
      throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, getRandomDeviceStmt)) {
      stmt.setInt(1, roomLower);
      stmt.setInt(2, roomUpper);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return new DeviceInfo(rs.getInt("device_id"), rs.getInt("type"), rs.getString("status"));
        }
      }
    }
    return null;
  }

  private void updateDeviceStatus(Connection conn, int deviceId, String newStatus)
      throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, updateDeviceStmt)) {
      stmt.setString(1, newStatus);
      stmt.setInt(2, deviceId);
      stmt.executeUpdate();
    }
  }

  private void logDeviceAction(Connection conn, int userId, int deviceId, DeviceAction action)
      throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, logActionStmt)) {
      stmt.setInt(1, userId);
      stmt.setInt(2, deviceId);
      stmt.setString(3, action.command);
      stmt.setString(4, action.newStatus);
      stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
      stmt.executeUpdate();
    }
  }

  private DeviceAction generateDeviceAction(Random gen, int deviceType) {
    double rand = gen.nextDouble();

    switch (deviceType) {
      case BenchIOConfig.DEVICE_TYPE_LIGHT:
        if (rand > 0.7) {
          return new DeviceAction("TOGGLE", rand > 0.85 ? "ON" : "OFF");
        }
        break;

      case BenchIOConfig.DEVICE_TYPE_THERMOSTAT:
        if (rand > 0.8) {
          return new DeviceAction("SET_TEMP", String.valueOf(18 + gen.nextInt(10)));
        }
        break;

      case BenchIOConfig.DEVICE_TYPE_LOCK:
        if (rand > 0.9) {
          return new DeviceAction(
              rand > 0.5 ? "LOCK" : "UNLOCK", rand > 0.5 ? "LOCKED" : "UNLOCKED");
        }
        break;
    }
    return new DeviceAction("NO_ACTION", null);
  }

  // Classes auxiliares
  private static class DeviceInfo {
    final int deviceId;
    final int type;
    final String status;

    DeviceInfo(int deviceId, int type, String status) {
      this.deviceId = deviceId;
      this.type = type;
      this.status = status;
    }
  }

  private static class DeviceAction {
    final String command;
    final String newStatus;

    DeviceAction(String command, String newStatus) {
      this.command = command;
      this.newStatus = newStatus;
    }
  }
}
