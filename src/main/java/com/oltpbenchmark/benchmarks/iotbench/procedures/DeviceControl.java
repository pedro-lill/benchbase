package com.oltpbenchmark.benchmarks.iotbench.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.iotbench.IotBenchConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceControl extends Procedure {

  private static final Logger LOG = LoggerFactory.getLogger(DeviceControl.class);

  public final SQLStmt updateDeviceStmt =
      new SQLStmt(
          String.format(
              "UPDATE %s SET status = ? WHERE device_id = ?", IotBenchConstants.TABLENAME_DEVICE));

  public final SQLStmt insertActionLogStmt =
      new SQLStmt(
          String.format(
              "INSERT INTO %s (user_id, device_id, action, status, date) VALUES (?, ?, ?, ?, ?)",
              IotBenchConstants.TABLENAME_ACTION_LOGS));

  public void run(Connection conn, int userId, int deviceId, String action, String newStatus)
      throws SQLException {
    try (PreparedStatement updateStmt = this.getPreparedStatement(conn, updateDeviceStmt)) {
      updateStmt.setString(1, newStatus);
      updateStmt.setInt(2, deviceId);
      int rowsAffected = updateStmt.executeUpdate();
      if (rowsAffected > 0) {
        LOG.debug("Dispositivo device_id={} atualizado para status={}", deviceId, newStatus);
      } else {
        LOG.warn("Nenhum dispositivo encontrado com device_id={}", deviceId);
        return;
      }
    } catch (SQLException e) {
      LOG.error(
          "Erro ao atualizar status do dispositivo device_id={}: {}", deviceId, e.getMessage());
      throw e;
    }

    try (PreparedStatement insertLogStmt = this.getPreparedStatement(conn, insertActionLogStmt)) {
      insertLogStmt.setInt(1, userId);
      insertLogStmt.setInt(2, deviceId);
      insertLogStmt.setString(3, action);
      insertLogStmt.setString(4, newStatus);
      insertLogStmt.setTimestamp(5, new Timestamp(new Date().getTime()));
      insertLogStmt.executeUpdate();
      LOG.debug(
          "Log de ação inserido para device_id={} com ação={} e status={}",
          deviceId,
          action,
          newStatus);
    } catch (SQLException e) {
      LOG.error("Erro ao inserir log de ação para device_id={}: {}", deviceId, e.getMessage());
      throw e;
    }
  }
}
