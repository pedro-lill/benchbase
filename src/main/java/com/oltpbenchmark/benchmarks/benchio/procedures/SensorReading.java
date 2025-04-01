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

 import com.oltpbenchmark.api.Procedure;
 import com.oltpbenchmark.api.SQLStmt;
 import com.oltpbenchmark.benchmarks.benchio.IotBenchConstants;
 import com.oltpbenchmark.benchmarks.benchio.IotBenchConfig;
 import com.oltpbenchmark.benchmarks.benchio.pojo.SensorLog;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.Random;

 public class SensorReading extends Procedure {

     // SQL para inserir uma nova leitura de sensor
     public final SQLStmt insertSensorLogStmt = new SQLStmt(
         "INSERT INTO " + IotBenchConstants.TABLENAME_SENSORLOG + " (" +
         "sensor_id, value, date" +
         ") VALUES (?, ?, ?)"
     );

     // SQL para obter informações do sensor
     public final SQLStmt getSensorStmt = new SQLStmt(
         "SELECT type, device_id FROM " + IotBenchConstants.TABLENAME_SENSOR +
         " WHERE sensor_id = ?"
     );

     // SQL para atualizar o valor atual do sensor
     public final SQLStmt updateSensorValueStmt = new SQLStmt(
         "UPDATE " + IotBenchConstants.TABLENAME_SENSOR +
         " SET value = ? WHERE sensor_id = ?"
     );

     // SQL para verificar regras de automação
     public final SQLStmt checkAutomationStmt = new SQLStmt(
         "SELECT command FROM " + IotBenchConstants.TABLENAME_AUTOMATION +
         " WHERE device_id = ? AND status = 'active'"
     );

     public void run(Connection conn, int sensorId, Random rng) throws SQLException {
         // 1. Obter informações do sensor
         int sensorType = -1;
         int deviceId = -1;

         try (PreparedStatement stmt = this.getPreparedStatement(conn, getSensorStmt)) {
             stmt.setInt(1, sensorId);
             try (ResultSet rs = stmt.executeQuery()) {
                 if (rs.next()) {
                     sensorType = rs.getInt("type");
                     deviceId = rs.getInt("device_id");
                 } else {
                     throw new SQLException("Sensor não encontrado: " + sensorId);
                 }
             }
         }

         // 2. Gerar uma nova leitura simulada
         double newValue = generateSensorValue(sensorType, rng);
         Timestamp currentTime = new Timestamp(System.currentTimeMillis());

         // 3. Registrar a leitura no log
         try (PreparedStatement stmt = this.getPreparedStatement(conn, insertSensorLogStmt)) {
             stmt.setInt(1, sensorId);
             stmt.setDouble(2, newValue);
             stmt.setTimestamp(3, currentTime);
             stmt.executeUpdate();
         }

         // 4. Atualizar o valor atual do sensor
         try (PreparedStatement stmt = this.getPreparedStatement(conn, updateSensorValueStmt)) {
             stmt.setDouble(1, newValue);
             stmt.setInt(2, sensorId);
             stmt.executeUpdate();
         }

         // 5. Verificar regras de automação (opcional)
         checkAutomationRules(conn, deviceId, sensorType, newValue);
     }

     private double generateSensorValue(int sensorType, Random rng) {
         // Lógica para gerar valores realistas baseados no tipo de sensor
         switch (sensorType) {
             case IotBenchConfig.SENSOR_TYPE_TEMPERATURE:
                 return 20 + rng.nextDouble() * 15; // 20-35°C
             case IotBenchConfig.SENSOR_TYPE_HUMIDITY:
                 return 30 + rng.nextDouble() * 50; // 30-80%
             case IotBenchConfig.SENSOR_TYPE_MOTION:
                 return rng.nextDouble() > 0.9 ? 1 : 0; // 10% chance de detecção
             default:
                 return rng.nextDouble() * 100; // Valor genérico
         }
     }

     private void checkAutomationRules(Connection conn, int deviceId, int sensorType, double value)
             throws SQLException {
         try (PreparedStatement stmt = this.getPreparedStatement(conn, checkAutomationStmt)) {
             stmt.setInt(1, deviceId);
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     String command = rs.getString("command");
                     // Aqui você pode implementar a lógica para executar comandos
                     // quando certas condições forem atendidas
                     if (shouldTriggerAutomation(sensorType, value, command)) {
                         executeAutomationCommand(conn, deviceId, command);
                     }
                 }
             }
         }
     }

     private boolean shouldTriggerAutomation(int sensorType, double value, String command) {
         // Implemente sua lógica de condição aqui
         // Exemplo: se temperatura > 30 e comando for "turn_on_ac"
         return true; // Simplificado para o exemplo
     }

     private void executeAutomationCommand(Connection conn, int deviceId, String command) {
         // Implemente a execução do comando de automação
         // Ex: ligar/desligar dispositivos, ajustar parâmetros, etc.
     }
 }