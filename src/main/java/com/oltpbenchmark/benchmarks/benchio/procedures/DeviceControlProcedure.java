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

public class DeviceControlProcedure extends BenchIOProcedure {

  private static final Logger LOG = LoggerFactory.getLogger(DeviceControlProcedure.class);

  public final SQLStmt getRandomDeviceStmt =
      new SQLStmt(
          "SELECT device_id, type, status FROM "
              + BenchIOConstants.TABLENAME_DEVICE
              + " WHERE room_id = ? ORDER BY RAND() LIMIT 1");

  public final SQLStmt updateDeviceStmt =
      new SQLStmt(
          "UPDATE " + BenchIOConstants.TABLENAME_DEVICE + " SET status = ? WHERE device_id = ?");

  public final SQLStmt logActionStmt =
      new SQLStmt(
          "INSERT INTO "
              + BenchIOConstants.TABLENAME_ACTIONLOGS
              + " (device_id, action, status, timestamp) VALUES (?, ?, ?, ?)");

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
      // 1. Selecionar dispositivo aleatório na sala
      DeviceInfo device = getRandomDeviceInRoom(conn, terminalRoomLowerID);
      if (device == null) {
        LOG.warn("Nenhum dispositivo encontrado na sala {}", terminalRoomLowerID);
        return;
      }

      // 2. Decidir ação baseada no tipo de dispositivo
      DeviceAction action = generateDeviceAction(gen, device.type);

      // 3. Executar ação se não for NO_ACTION
      if (!"NO_ACTION".equals(action.command)) {
        try (PreparedStatement stmt = this.getPreparedStatement(conn, updateDeviceStmt)) {
          stmt.setString(1, action.newStatus);
          stmt.setInt(2, device.deviceId);
          stmt.executeUpdate();
        }

        // 4. Registrar ação
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try (PreparedStatement stmt = this.getPreparedStatement(conn, logActionStmt)) {
          stmt.setInt(1, device.deviceId);
          stmt.setString(2, action.command);
          stmt.setString(3, action.newStatus);
          stmt.setTimestamp(4, now);
          stmt.executeUpdate();
        }

        LOG.debug(
            "Dispositivo {} alterado para {} via comando {}",
            device.deviceId,
            action.newStatus,
            action.command);
      }
    } catch (SQLException e) {
      LOG.error("Erro ao controlar dispositivo", e);
      throw e;
    }
  }

  private DeviceInfo getRandomDeviceInRoom(Connection conn, int roomId) throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, getRandomDeviceStmt)) {
      stmt.setInt(1, roomId);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return new DeviceInfo(rs.getInt("device_id"), rs.getInt("type"), rs.getString("status"));
        }
      }
    }
    return null;
  }

  private DeviceAction generateDeviceAction(Random gen, int deviceType) {
    // Lógica mais sofisticada baseada no tipo de dispositivo
    double rand = gen.nextDouble();

    if (deviceType == BenchIOConfig.DEVICE_TYPE_LIGHT) {
      if (rand > 0.8) {
        return new DeviceAction("TOGGLE", gen.nextBoolean() ? "ON" : "OFF");
      }
    } else if (deviceType == BenchIOConfig.DEVICE_TYPE_THERMOSTAT) {
      if (rand > 0.9) {
        return new DeviceAction("ADJUST", "TEMP_" + (18 + gen.nextInt(5)));
      }
    }
    return new DeviceAction("NO_ACTION", null);
  }

  private static class DeviceInfo {
    public final int deviceId;
    public final int type;
    public final String status;

    public DeviceInfo(int deviceId, int type, String status) {
      this.deviceId = deviceId;
      this.type = type;
      this.status = status;
    }
  }

  private static class DeviceAction {
    public final String command;
    public final String newStatus;

    public DeviceAction(String command, String newStatus) {
      this.command = command;
      this.newStatus = newStatus;
    }
  }
}
