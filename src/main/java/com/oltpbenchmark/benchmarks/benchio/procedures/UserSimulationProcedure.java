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

public class UserSimulationProcedure extends BenchIOProcedure {

  private static final Logger LOG = LoggerFactory.getLogger(UserSimulationProcedure.class);

  public final SQLStmt getUserStmt =
      new SQLStmt(
          "SELECT user_id FROM " + BenchIOConstants.TABLENAME_USER + " ORDER BY RAND() LIMIT 1");

  public final SQLStmt getRandomDeviceStmt =
      new SQLStmt(
          "SELECT device_id, type FROM "
              + BenchIOConstants.TABLENAME_DEVICE
              + " WHERE room_id BETWEEN ? AND ? ORDER BY RAND() LIMIT 1");

  public final SQLStmt logUserActionStmt =
      new SQLStmt(
          "INSERT INTO "
              + BenchIOConstants.TABLENAME_ACTIONLOGS
              + " (user_id, device_id, action, timestamp) VALUES (?, ?, ?, ?)");

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
      // 1. Selecionar usuário aleatório
      int userId = getRandomUser(conn);
      if (userId == -1) {
        LOG.warn("Nenhum usuário encontrado no sistema");
        return;
      }

      // 2. Selecionar dispositivo aleatório na faixa de salas
      DeviceInfo device =
          getRandomDeviceInRoomRange(conn, terminalRoomLowerID, terminalRoomUpperID);
      if (device == null) {
        LOG.warn(
            "Nenhum dispositivo encontrado nas salas {}-{}",
            terminalRoomLowerID,
            terminalRoomUpperID);
        return;
      }

      // 3. Gerar ação do usuário
      String action = generateUserAction(gen, device.type);

      // 4. Registrar ação
      Timestamp now = new Timestamp(System.currentTimeMillis());
      try (PreparedStatement stmt = this.getPreparedStatement(conn, logUserActionStmt)) {
        stmt.setInt(1, userId);
        stmt.setInt(2, device.deviceId);
        stmt.setString(3, action);
        stmt.setTimestamp(4, now);
        stmt.executeUpdate();
      }

      LOG.debug(
          "Usuário {} interagiu com dispositivo {}: ação {}", userId, device.deviceId, action);

    } catch (SQLException e) {
      LOG.error("Erro durante simulação de usuário", e);
      throw e;
    }
  }

  private int getRandomUser(Connection conn) throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, getUserStmt);
        ResultSet rs = stmt.executeQuery()) {
      if (rs.next()) {
        return rs.getInt("user_id");
      }
    }
    return -1;
  }

  private DeviceInfo getRandomDeviceInRoomRange(Connection conn, int roomLower, int roomUpper)
      throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, getRandomDeviceStmt)) {
      stmt.setInt(1, roomLower);
      stmt.setInt(2, roomUpper);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return new DeviceInfo(rs.getInt("device_id"), rs.getInt("type"));
        }
      }
    }
    return null;
  }

  private String generateUserAction(Random gen, int deviceType) {
    switch (deviceType) {
      case BenchIOConfig.DEVICE_TYPE_LIGHT:
        return gen.nextBoolean() ? "TURN_ON" : "TURN_OFF";
      case BenchIOConfig.DEVICE_TYPE_THERMOSTAT:
        return "SET_TEMP_" + (18 + gen.nextInt(10));
      case BenchIOConfig.DEVICE_TYPE_SECURITY:
        return gen.nextBoolean() ? "ARM" : "DISARM";
      default:
        return "INTERACT";
    }
  }

  private static class DeviceInfo {
    public final int deviceId;
    public final int type;

    public DeviceInfo(int deviceId, int type) {
      this.deviceId = deviceId;
      this.type = type;
    }
  }
}
