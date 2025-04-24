package com.oltpbenchmark.benchmarks.benchio.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.benchio.BenchIOConstants;
import com.oltpbenchmark.types.TransactionStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutomationTriggerProcedure extends Procedure {

  private static final Logger LOG = LoggerFactory.getLogger(AutomationTriggerProcedure.class);

  // SQL Statements
  public final SQLStmt getDevicesInRoomStmt =
      new SQLStmt(
          "SELECT device_id FROM "
              + BenchIOConstants.TABLENAME_DEVICE
              + " WHERE room_id BETWEEN ? AND ? AND status = 'ACTIVE'");

  public final SQLStmt getAutomationRules =
      new SQLStmt(
          "SELECT rule_id, trigger_type, threshold_value, action_command "
              + "FROM "
              + BenchIOConstants.TABLENAME_AUTOMATIONPROFILE
              + " WHERE device_id = ? AND status = 'ACTIVE'");

  public final SQLStmt getCurrentSensorValue =
      new SQLStmt(
          "SELECT s.value FROM "
              + BenchIOConstants.TABLENAME_SENSOR
              + " s "
              + "JOIN "
              + BenchIOConstants.TABLENAME_DEVICE
              + " d ON s.device_id = d.device_id "
              + "WHERE s.device_id = ? AND s.type = ?");

  public final SQLStmt executeDeviceAction =
      new SQLStmt(
          "UPDATE "
              + BenchIOConstants.TABLENAME_DEVICE
              + " SET status = ?, last_updated = NOW() WHERE device_id = ?");

  public final SQLStmt logAutomationAction =
      new SQLStmt(
          "INSERT INTO "
              + BenchIOConstants.TABLENAME_ACTIONLOGS
              + " (device_id, action, status, date) VALUES (?, ?, ?, NOW())");

  public TransactionStatus run(
      Connection conn,
      Random gen,
      int terminalHubID,
      int terminalRoomLowerID,
      int terminalRoomUpperID)
      throws SQLException {

    try {
      // 1. Obter dispositivos ativos na faixa de salas
      List<Integer> devices = getDevicesInRoomRange(conn, terminalRoomLowerID, terminalRoomUpperID);
      if (devices.isEmpty()) {
        LOG.debug("No active devices in rooms {}-{}", terminalRoomLowerID, terminalRoomUpperID);
        return TransactionStatus.SUCCESS;
      }

      // 2. Verificar e executar automações
      boolean triggered = false;
      for (int deviceId : devices) {
        if (checkAndTriggerAutomation(conn, deviceId)) {
          triggered = true;
        }
      }

      return triggered ? TransactionStatus.SUCCESS : TransactionStatus.RETRY;

    } catch (SQLException e) {
      LOG.error("Error during automation trigger", e);
      throw e;
    }
  }

  private List<Integer> getDevicesInRoomRange(Connection conn, int roomLower, int roomUpper)
      throws SQLException {
    List<Integer> devices = new ArrayList<>();

    try (PreparedStatement stmt = this.getPreparedStatement(conn, getDevicesInRoomStmt)) {
      stmt.setInt(1, roomLower);
      stmt.setInt(2, roomUpper);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          devices.add(rs.getInt("device_id"));
        }
      }
    }
    return devices;
  }

  private boolean checkAndTriggerAutomation(Connection conn, int deviceId) throws SQLException {
    boolean triggered = false;

    try (PreparedStatement stmt = this.getPreparedStatement(conn, getAutomationRules)) {
      stmt.setInt(1, deviceId);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          int triggerType = rs.getInt("trigger_type");
          double threshold = rs.getDouble("threshold_value");
          String action = rs.getString("action_command");
          int ruleId = rs.getInt("rule_id");

          if (shouldTrigger(conn, deviceId, triggerType, threshold)) {
            executeAutomationAction(conn, deviceId, action);
            logAutomationEvent(conn, deviceId, "Rule " + ruleId + ": " + action);
            triggered = true;
          }
        }
      }
    }
    return triggered;
  }

  private boolean shouldTrigger(Connection conn, int deviceId, int triggerType, double threshold)
      throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, getCurrentSensorValue)) {
      stmt.setInt(1, deviceId);
      stmt.setInt(2, triggerType);

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          double currentValue = rs.getDouble(1);
          return currentValue >= threshold;
        }
      }
    }
    return false;
  }

  private void executeAutomationAction(Connection conn, int deviceId, String action)
      throws SQLException {
    String deviceStatus = convertActionToStatus(action);

    try (PreparedStatement stmt = this.getPreparedStatement(conn, executeDeviceAction)) {
      stmt.setString(1, deviceStatus);
      stmt.setInt(2, deviceId);
      stmt.executeUpdate();
    }
  }

  private void logAutomationEvent(Connection conn, int deviceId, String action)
      throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, logAutomationAction)) {
      stmt.setInt(1, deviceId);
      stmt.setString(2, action);
      stmt.setString(3, "EXECUTED");
      stmt.executeUpdate();
    }
  }

  private String convertActionToStatus(String action) {
    if (action == null || action.isEmpty()) {
      return "UNKNOWN";
    }
    action = action.toUpperCase();

    if (action.contains("ON")) return "ON";
    if (action.contains("OFF")) return "OFF";
    if (action.contains("TEMP_")) return action;
    if (action.contains("LOCK")) return action.contains("UNLOCK") ? "UNLOCKED" : "LOCKED";

    return action;
  }
}
