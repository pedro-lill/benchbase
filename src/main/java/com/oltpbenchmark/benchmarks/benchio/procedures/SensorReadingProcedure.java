/*
 * Copyright 2020 by OLTPBenchmark Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.oltpbenchmark.benchmarks.benchio.procedures;

import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.benchio.BenchIOConfig;
import com.oltpbenchmark.benchmarks.benchio.BenchIOConstants;
import com.oltpbenchmark.benchmarks.benchio.BenchIOWorker;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorReadingProcedure extends BenchIOProcedure {

  private static final Logger LOG = LoggerFactory.getLogger(SensorReadingProcedure.class);

  // SQL Statements
  public final SQLStmt getSensorInfo =
      new SQLStmt(
          "SELECT sensor_id, type, device_id, room_id FROM "
              + BenchIOConstants.TABLENAME_SENSOR
              + " WHERE room_id BETWEEN ? AND ? ORDER BY RAND() LIMIT 1");

  public final SQLStmt insertSensorLog =
      new SQLStmt(
          "INSERT INTO "
              + BenchIOConstants.TABLENAME_SENSORLOG
              + " (sensor_id, value, timestamp) VALUES (?, ?, ?)");

  public final SQLStmt updateCurrentValue =
      new SQLStmt(
          "UPDATE " + BenchIOConstants.TABLENAME_SENSOR + " SET value = ? WHERE sensor_id = ?");

  @Override
  public void run(
      Connection conn,
      Random gen,
      int terminalHubID,
      int numHubs,
      int terminalRoomLowerID,
      int terminalRoomUpperID,
      BenchIOWorker worker)
      throws SQLException {

    try {
      // 1. Obter um sensor aleatório dentro da faixa de salas
      SensorInfo sensor =
          getRandomSensorInRoomRange(conn, terminalRoomLowerID, terminalRoomUpperID);
      if (sensor == null) {
        LOG.warn(
            "Nenhum sensor encontrado para as salas {}-{}",
            terminalRoomLowerID,
            terminalRoomUpperID);
        return;
      }

      // 2. Gerar valor simulado baseado no tipo
      double value = generateSensorValue(gen, sensor.type);

      // 3. Registrar leitura
      Timestamp now = new Timestamp(System.currentTimeMillis());
      try (PreparedStatement stmt = this.getPreparedStatement(conn, insertSensorLog)) {
        stmt.setInt(1, sensor.sensorId);
        stmt.setDouble(2, value);
        stmt.setTimestamp(3, now);
        stmt.executeUpdate();
      }

      // 4. Atualizar valor atual do sensor
      try (PreparedStatement stmt = this.getPreparedStatement(conn, updateCurrentValue)) {
        stmt.setDouble(1, value);
        stmt.setInt(2, sensor.sensorId);
        stmt.executeUpdate();
      }

      LOG.debug("Leitura registrada - Sensor: {}, Valor: {:.2f}", sensor.sensorId, value);

    } catch (SQLException e) {
      LOG.error("Erro durante leitura do sensor", e);
      throw e;
    }
  }

  private SensorInfo getRandomSensorInRoomRange(Connection conn, int roomLower, int roomUpper)
      throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, getSensorInfo)) {
      stmt.setInt(1, roomLower);
      stmt.setInt(2, roomUpper);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return new SensorInfo(
              rs.getInt("sensor_id"),
              rs.getInt("type"),
              rs.getInt("device_id"),
              rs.getInt("room_id"));
        }
      }
    }
    return null;
  }

  private double generateSensorValue(Random gen, int sensorType) {
    switch (sensorType) {
      case BenchIOConfig.SENSOR_TYPE_TEMPERATURE:
        return 18 + gen.nextDouble() * 15; // 18-33°C
      case BenchIOConfig.SENSOR_TYPE_HUMIDITY:
        return 30 + gen.nextDouble() * 50; // 30-80%
      case BenchIOConfig.SENSOR_TYPE_MOTION:
        return gen.nextDouble() > 0.85 ? 1 : 0; // 15% chance de movimento
      default:
        return gen.nextDouble() * 100;
    }
  }

  private static class SensorInfo {
    public final int sensorId;
    public final int type;
    public final int deviceId;
    public final int roomId;

    public SensorInfo(int sensorId, int type, int deviceId, int roomId) {
      this.sensorId = sensorId;
      this.type = type;
      this.deviceId = deviceId;
      this.roomId = roomId;
    }
  }
}
