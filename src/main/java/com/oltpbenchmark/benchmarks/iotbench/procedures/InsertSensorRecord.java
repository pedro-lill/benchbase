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

  public final SQLStmt insertStmt =
      new SQLStmt(
          "INSERT INTO "
              + IotBenchConstants.TABLENAME_SENSOR
              + " (sensorId, name, type, value, deviceId) VALUES (?, ?, ?, ?, ?)");

  public void run(Connection conn, int sensorId, String name, int type, double value, int deviceId)
      throws SQLException {

    try (PreparedStatement stmt = this.getPreparedStatement(conn, this.insertStmt)) {
      stmt.setInt(1, sensorId);
      stmt.setString(2, name);
      stmt.setInt(3, type);
      stmt.setDouble(4, value);
      stmt.setInt(5, deviceId);

      stmt.executeUpdate();
    } catch (SQLException e) {
      if (SQLUtil.isDuplicateKeyException(e)) {
        LOG.debug("Registro duplicado para sensorId: " + sensorId, e);
        throw new SQLException("O sensor com sensorId " + sensorId + " já existe.", e);
      } else {
        LOG.error("Erro ao inserir registro do sensor: ", e);
        throw e;
      }
    }
  }
}
