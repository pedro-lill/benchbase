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
      final int sensor_id,
      final String name,
      final int type,
      final double value,
      final int device_id)
      throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, insertStmt)) {
      stmt.setInt(1, sensor_id);
      stmt.setString(2, name);
      stmt.setInt(3, type);
      stmt.setDouble(4, value);
      stmt.setInt(5, device_id);

      int result = stmt.executeUpdate();
      if (result > 0) {
        LOG.debug(
            "Sensor inserido com sucesso: sensor_id={}, name={}, device_id={}",
            sensor_id,
            name,
            device_id);
      }
    } catch (SQLException e) {
      if (SQLUtil.isDuplicateKeyException(e)) {
        LOG.warn("Tentativa de inserir sensor duplicado: sensor_id={}", sensor_id);
        throw new SQLException("Já existe um sensor com sensor_id " + sensor_id + ".", e);
      }
      LOG.error("Erro ao inserir sensor: sensor_id={}, name={}", sensor_id, name, e);
      throw e;
    }
  }
}
