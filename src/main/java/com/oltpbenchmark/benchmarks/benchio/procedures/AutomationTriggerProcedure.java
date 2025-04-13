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
import com.oltpbenchmark.benchmarks.benchio.BenchIOConstants;
import com.oltpbenchmark.benchmarks.benchio.BenchIOWorker;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutomationTriggerProcedure extends BenchIOProcedure {

  private static final Logger LOG = LoggerFactory.getLogger(AutomationTriggerProcedure.class);

  public final SQLStmt getDevicesInRoomStmt =
      new SQLStmt(
          "SELECT device_id FROM "
              + BenchIOConstants.TABLENAME_DEVICE
              + " WHERE room_id BETWEEN ? AND ?");

  public final SQLStmt getAutomationRules =
      new SQLStmt(
          "SELECT * FROM "
              + BenchIOConstants.TABLENAME_AUTOMATION
              + " WHERE device_id = ? AND status = 'ACTIVE'");

  public final SQLStmt getCurrentSensorValue =
      new SQLStmt(
          "SELECT value FROM "
              + BenchIOConstants.TABLENAME_SENSOR
              + " WHERE device_id = ? AND type = ?");

  public final SQLStmt executeDeviceAction =
      new SQLStmt(
          "UPDATE " + BenchIOConstants.TABLENAME_DEVICE + " SET status = ? WHERE device_id = ?");

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
      // 1. Obter dispositivos na faixa de salas
      List<Integer> devices = getDevicesInRoomRange(conn, terminalRoomLowerID, terminalRoomUpperID);

      // 2. Verificar regras para cada dispositivo
      for (int deviceId : devices) {
        checkDeviceAutomation(conn, deviceId);
      }
    } catch (SQLException e) {
      LOG.error("Erro durante verificação de automação", e);
      throw e;
    }
  }

  private List<Integer> getDevicesInRoomRange(Connection conn, int roomLower, int roomUpper)
      throws SQLException {
    List<Integer> devices = new ArrayList<>();

    try (PreparedStatement stmt = this.getPreparedStatement(conn, getDevicesInRoomStmt)) {
      stmt.setInt(1, roomLower);
      stmt.setInt(2, roomUpper);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          devices.add(rs.getInt("device_id"));
        }
      }
    }
    return devices;
  }

  private void checkDeviceAutomation(Connection conn, int deviceId) throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, getAutomationRules)) {
      stmt.setInt(1, deviceId);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          int triggerType = rs.getInt("trigger_type");
          double threshold = rs.getDouble("threshold_value");
          String action = rs.getString("action_command");

          if (shouldTrigger(conn, deviceId, triggerType, threshold)) {
            executeAutomationAction(conn, deviceId, action);
            LOG.debug("Automação acionada para dispositivo {}: {}", deviceId, action);
          }
        }
      }
    }
  }

  private boolean shouldTrigger(Connection conn, int deviceId, int triggerType, double threshold)
      throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, getCurrentSensorValue)) {
      stmt.setInt(1, deviceId);
      stmt.setInt(2, triggerType);

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          double currentValue = rs.getDouble(1);
          return currentValue >= threshold;
        }
      }
    }
    return false;
  }

  private void executeAutomationAction(Connection conn, int deviceId, String action)
      throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, executeDeviceAction)) {
      // Converte a ação para um status de dispositivo
      String deviceStatus = convertActionToStatus(action);
      stmt.setString(1, deviceStatus);
      stmt.setInt(2, deviceId);
      stmt.executeUpdate();
    }
  }

  private String convertActionToStatus(String action) {
    // Lógica simples de conversão - pode ser expandida conforme necessário
    if (action.contains("ON")) return "ON";
    if (action.contains("OFF")) return "OFF";
    if (action.contains("TEMP")) return action; // Ex: "TEMP_22"
    return action;
  }
}
