package com.oltpbenchmark.benchmarks.iotbench.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.iotbench.IotBenchConstants;
import com.oltpbenchmark.util.SQLUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertSensorRecord extends Procedure {

  private static final Logger LOG = LoggerFactory.getLogger(InsertSensorRecord.class);

  private static final String SQL_INSERT =
      String.format(
          "INSERT INTO %s (sensor_id, name, type, value, device_id) VALUES (?, ?, ?, ?, ?)",
          IotBenchConstants.TABLENAME_SENSOR);

  public final SQLStmt insertStmt = new SQLStmt(SQL_INSERT);

  public void run(
      final Connection conn,
      final int sensorId,
      final String name,
      final int type,
      final double value,
      final int deviceId)
      throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, insertStmt)) {
      stmt.setInt(1, sensorId);
      stmt.setString(2, name);
      stmt.setInt(3, type);
      stmt.setDouble(4, value);
      stmt.setInt(5, deviceId);

      int result = stmt.executeUpdate();
      if (result > 0) {
        LOG.debug(
            "Sensor inserido com sucesso: sensor_id={}, name={}, device_id={}",
            sensorId,
            name,
            deviceId);
      }
    } catch (SQLException e) {
      if (SQLUtil.isDuplicateKeyException(e)) {
        LOG.warn("Tentativa de inserir sensor duplicado: sensor_id={}", sensorId);
        throw new SQLException("Já existe um sensor com sensor_id " + sensorId + ".", e);
      }
      LOG.error("Erro ao inserir sensor: sensor_id={}, name={}", sensorId, name, e);
      throw e;
    }
  }
}
