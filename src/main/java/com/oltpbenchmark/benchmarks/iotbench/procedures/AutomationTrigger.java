package com.oltpbenchmark.benchmarks.iotbench.procedures;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.iotbench.IotBenchConstants;

public class AutomationTrigger extends Procedure {
  private static final Logger LOG = LoggerFactory.getLogger(AutomationTrigger.class);
  public final SQLStmt getSensorValueStmt =
      new SQLStmt(
          String.format(
              "SELECT value FROM %s WHERE sensor_id = ? ORDER BY date DESC LIMIT 1",
              IotBenchConstants.TABLENAME_SENSOR_LOG));

  public final SQLStmt getAutomationProfileStmt =
      new SQLStmt(
          String.format(
              "SELECT device_id, command FROM %s WHERE profile_id = ? AND status = 'Active'",
              IotBenchConstants.TABLENAME_AUTOMATION_PROFILE));

  public final SQLStmt updateDeviceStatusStmt =
      new SQLStmt(
          String.format(
              "UPDATE %s SET status = ? WHERE device_id = ?", IotBenchConstants.TABLENAME_DEVICE));

  public final SQLStmt insertActionLogStmt =
      new SQLStmt(
          String.format(
              "INSERT INTO %s (user_id, device_id, action, status, date) VALUES (?, ?, ?, ?, ?)",
              IotBenchConstants.TABLENAME_ACTION_LOGS));

  public void run(
      Connection conn, int profileId, int triggeringSensorId, double triggerValue, int userId)
      throws SQLException {
    double currentSensorValue = -1;
    try (PreparedStatement stmt = this.getPreparedStatement(conn, getSensorValueStmt)) {
      stmt.setInt(1, triggeringSensorId);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          currentSensorValue = rs.getDouble("value");
        }
      }
    } catch (SQLException e) {
      LOG.error("Erro ao obter valor do sensor {}: {}", triggeringSensorId, e.getMessage());
      throw e;
    }

    if (currentSensorValue != -1 && currentSensorValue > triggerValue) {
      try (PreparedStatement stmt = this.getPreparedStatement(conn, getAutomationProfileStmt)) {
        stmt.setInt(1, profileId);
        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) {
            int targetDeviceId = rs.getInt("device_id");
            String command = rs.getString("command");
            String newStatus = (command.equalsIgnoreCase("ON")) ? "Active" : "Inactive";

            try (PreparedStatement updateStmt =
                this.getPreparedStatement(conn, updateDeviceStatusStmt)) {
              updateStmt.setString(1, newStatus);
              updateStmt.setInt(2, targetDeviceId);
              updateStmt.executeUpdate();
              {LOG.info(
                  "Automação profile_id={} acionada. Dispositivo device_id={} alterado para status={}",
                  profileId,
                  targetDeviceId,
                  newStatus);
              }
              try (PreparedStatement insertLogStmt =
                  this.getPreparedStatement(conn, insertActionLogStmt)) {
                insertLogStmt.setInt(1, userId);
                insertLogStmt.setInt(2, targetDeviceId);
                insertLogStmt.setString(3, "Automation Triggered: " + command);
                insertLogStmt.setString(4, newStatus);
                insertLogStmt.setTimestamp(5, new Timestamp(new Date().getTime()));
                insertLogStmt.executeUpdate();
              } catch (SQLException e) {
                LOG.error("Erro ao inserir log de ação para automação: {}", e.getMessage());
              }
            } catch (SQLException e) {
              LOG.error(
                  "Erro ao atualizar status do dispositivo {} pela automação: {}",
                  targetDeviceId,
                  e.getMessage());
            }
          }
        }
      } catch (SQLException e) {
        LOG.error("Erro ao buscar perfil de automação {}: {}", profileId, e.getMessage());
        throw e;
      }
    }
  }
}
