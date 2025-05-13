package com.oltpbenchmark.benchmarks.iotbench.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.iotbench.IotBenchConstants;
import com.oltpbenchmark.util.SQLUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertSensorLogRecord extends Procedure {

  private static final Logger LOG = LoggerFactory.getLogger(InsertSensorLogRecord.class);
  private final Random rand = new Random();

  public final SQLStmt getRandomSensorStmt =
      new SQLStmt(
          "SELECT sensorId FROM "
              + IotBenchConstants.TABLENAME_SENSOR
              + " ORDER BY RANDOM() LIMIT 1");

  public final SQLStmt insertLogStmt =
      new SQLStmt(
          "INSERT INTO "
              + IotBenchConstants.TABLENAME_SENSOR_LOG
              + " (sensor_id, value, date) VALUES (?, ?, NOW())");

  public void run(Connection conn, double sensorValue) throws SQLException {
    int sensorId = -1;

    try (PreparedStatement stmt = this.getPreparedStatement(conn, getRandomSensorStmt)) {
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          sensorId = rs.getInt("sensorId");
        } else {
          LOG.warn("Não foram encontrados sensores para inserir o log.");
          return; // Ou lançar uma exceção, dependendo do comportamento desejado
        }
      }
    } catch (SQLException e) {
      LOG.error("Erro ao buscar um sensor aleatório: ", e);
      throw e;
    }

    if (sensorId != -1) {
      try (PreparedStatement stmt = this.getPreparedStatement(conn, this.insertLogStmt)) {
        stmt.setInt(1, sensorId);
        stmt.setDouble(2, sensorValue);
        stmt.executeUpdate();
      } catch (SQLException e) {
        if (SQLUtil.isDuplicateKeyException(e)) {
          LOG.debug("Registro duplicado para sensor_id: " + sensorId, e);
          throw new SQLException("Log de sensor já existe para sensor_id " + sensorId, e);
        } else {
          LOG.error("Erro ao inserir registro de log do sensor para sensor_id {}: ", sensorId, e);
          throw e;
        }
      }
    }
  }
}
