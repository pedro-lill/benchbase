package com.oltpbenchmark.benchmarks.iotbench.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.iotbench.IotBenchConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetDeviceStatus extends Procedure {

  private static final Logger LOG = LoggerFactory.getLogger(GetDeviceStatus.class);

  public final SQLStmt selectStmt =
      new SQLStmt(
          String.format(
              "SELECT device_id, name, status, device_type FROM %s WHERE device_id = ?",
              IotBenchConstants.TABLENAME_DEVICE));

  public void run(Connection conn, int deviceId) throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, selectStmt)) {
      stmt.setInt(1, deviceId);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          int id = rs.getInt("device_id");
          String name = rs.getString("name");
          String status = rs.getString("status");
          int type = rs.getInt("device_type");
          LOG.debug(
              "Status do dispositivo {}: nome={}, status={}, tipo={}", id, name, status, type);
        } else {
          LOG.warn("Nenhum dispositivo encontrado com device_id={}", deviceId);
        }
      }
    } catch (SQLException e) {
      LOG.error("Erro ao obter status do dispositivo {}: {}", deviceId, e.getMessage());
      throw e;
    }
  }
}
