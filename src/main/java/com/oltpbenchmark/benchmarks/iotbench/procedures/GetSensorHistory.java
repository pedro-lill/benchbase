package com.oltpbenchmark.benchmarks.iotbench.procedures;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.iotbench.IotBenchConstants;

public class GetSensorHistory extends Procedure {

  private static final Logger LOG = LoggerFactory.getLogger(GetSensorHistory.class);

  public final SQLStmt selectStmt =
      new SQLStmt(
          String.format(
              "SELECT value, date FROM %s WHERE sensor_id = ? AND date >= ? AND date <= ? ORDER BY date DESC",
              IotBenchConstants.TABLENAME_SENSOR_LOG));

  public void run(Connection conn, int sensorId, Timestamp startDate, Timestamp endDate)
      throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, selectStmt)) {
      stmt.setInt(1, sensorId);
      stmt.setTimestamp(2, startDate);
      stmt.setTimestamp(3, endDate);
      try (ResultSet rs = stmt.executeQuery()) {
        List<String> history = new ArrayList<>();
        while (rs.next()) {
          double value = rs.getDouble("value");
          Timestamp timestamp = rs.getTimestamp("date");
          history.add(String.format("[%s] Valor: %.2f", timestamp, value));
        }
        LOG.debug("Histórico do sensor {}: {}", sensorId, history);
      }
    } catch (SQLException e) {
      LOG.error("Erro ao obter histórico do sensor {}: {}", sensorId, e.getMessage());
      throw e;
    }
  }
}
