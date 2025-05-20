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

public class SensorReading extends Procedure {

  private static final Logger LOG = LoggerFactory.getLogger(SensorReading.class);

  public final SQLStmt insertStmt =
      new SQLStmt(
          String.format(
              "INSERT INTO %s (sensor_id, value, date) VALUES (?, ?, ?)",
              IotBenchConstants.TABLENAME_SENSOR_LOG));

  public void run(Connection conn, int sensorId, double value) throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, insertStmt)) {
      stmt.setInt(1, sensorId);
      stmt.setDouble(2, value);
      stmt.setTimestamp(3, new Timestamp(new Date().getTime()));
      stmt.executeUpdate();
    } catch (SQLException e) {
      LOG.error(
          "Erro ao inserir leitura do sensor para sensor_id={}: {}", sensorId, e.getMessage());
      throw e;
    }
  }
}
