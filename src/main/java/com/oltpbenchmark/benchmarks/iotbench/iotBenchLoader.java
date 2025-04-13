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

 package com.oltpbenchmark.benchmarks.iotbench;

 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.CountDownLatch;

 import com.oltpbenchmark.api.Loader;
 import com.oltpbenchmark.api.LoaderThread;
 import com.oltpbenchmark.benchmarks.iotbench.pojo.*;
 import com.oltpbenchmark.catalog.Table;
 import com.oltpbenchmark.util.SQLUtil;

 public final class IotBenchLoader extends Loader<IotBenchBenchmark> {

     private final int numHubs; // Número de hubs a serem carregados
     private final int numRoomsPerHub; // Número de salas por hub
     private final int numDevicesPerRoom; // Número de dispositivos por sala
     private final int numSensorsPerDevice; // Número de sensores por dispositivo

     public IotBenchLoader(IotBenchBenchmark benchmark) {
         super(benchmark);
         this.numHubs = IotBenchConfig.configHubCount;
         this.numRoomsPerHub = IotBenchConfig.configRoomsPerHub;
         this.numDevicesPerRoom = IotBenchConfig.configDevicesPerRoom;
         this.numSensorsPerDevice = IotBenchConfig.configSensorsPerDevice;
     }

     @Override
     public List<LoaderThread> createLoaderThreads() {
         List<LoaderThread> threads = new ArrayList<>();
         final CountDownLatch userLatch = new CountDownLatch(1);

         // Carrega a tabela de usuários primeiro
         threads.add(
             new LoaderThread(this.benchmark) {
                 @Override
                 public void load(Connection conn) {
                     loadUsers(conn, IotBenchConfig.configUserCount);
                 }

                 @Override
                 public void afterLoad() {
                     userLatch.countDown();
                 }
             }
         );

         // Carrega os hubs, salas, dispositivos e sensores
         for (int h = 1; h <= numHubs; h++) {
             final int hubId = h;
             LoaderThread t = new LoaderThread(this.benchmark) {
                 @Override
                 public void load(Connection conn) {
                     // Carrega o hub
                     loadHub(conn, hubId);

                     // Carrega as salas do hub
                     for (int r = 1; r <= numRoomsPerHub; r++) {
                         int roomId = (hubId - 1) * numRoomsPerHub + r;
                         loadRoom(conn, roomId, hubId);

                         // Carrega os dispositivos da sala
                         for (int d = 1; d <= numDevicesPerRoom; d++) {
                             int deviceId = (roomId - 1) * numDevicesPerRoom + d;
                             loadDevice(conn, deviceId, roomId, hubId);

                             // Carrega os sensores do dispositivo
                             for (int s = 1; s <= numSensorsPerDevice; s++) {
                                 int sensorId = (deviceId - 1) * numSensorsPerDevice + s;
                                 loadSensor(conn, sensorId, deviceId);
                             }
                         }
                     }
                 }

                 @Override
                 public void beforeLoad() {
                     // Espera a carga dos usuários ser concluída
                     try {
                         userLatch.await();
                     } catch (InterruptedException ex) {
                         throw new RuntimeException(ex);
                     }
                 }
             };
             threads.add(t);
         }

         return threads;
     }

     private PreparedStatement getInsertStatement(Connection conn, String tableName) throws SQLException {
         Table catalogTbl = benchmark.getCatalog().getTable(tableName);
         String sql = SQLUtil.getInsertSQL(catalogTbl, this.getDatabaseType());
         return conn.prepareStatement(sql);
     }

     protected void loadUsers(Connection conn, int userCount) {
         try (PreparedStatement userPrepStmt = getInsertStatement(conn, IotBenchConstants.TABLENAME_USERTABLE)) {
             for (int i = 1; i <= userCount; i++) {
                 User user = new User();
                 user.setUserId(i);
                 user.setNameIot("User" + i);
                 user.setEmail("user" + i + "@example.com");
                 user.setPasswordHash("hash" + i);
                 user.setUserType(i % 2); // Alterna entre tipos de usuário (0 e 1)

                 int idx = 1;
                 userPrepStmt.setInt(idx++, user.getUserId());
                 userPrepStmt.setString(idx++, user.getNameIot());
                 userPrepStmt.setString(idx++, user.getEmail());
                 userPrepStmt.setString(idx++, user.getPasswordHash());
                 userPrepStmt.setInt(idx, user.getUserType());
                 userPrepStmt.addBatch();

                 if (i % workConf.getBatchSize() == 0) {
                     userPrepStmt.executeBatch();
                     userPrepStmt.clearBatch();
                 }
             }
             userPrepStmt.executeBatch();
         } catch (SQLException se) {
             LOG.error("Erro ao carregar usuários: " + se.getMessage());
         }
     }

     protected void loadHub(Connection conn, int hubId) {
         try (PreparedStatement hubPrepStmt = getInsertStatement(conn, IotBenchConstants.TABLENAME_HUB)) {
             Hub hub = new Hub();
             hub.setHubId(hubId);
             hub.setName("Hub" + hubId);
             hub.setStatus("ativo");

             int idx = 1;
             hubPrepStmt.setInt(idx++, hub.getHubId());
             hubPrepStmt.setString(idx++, hub.getName());
             hubPrepStmt.setString(idx, hub.getStatus());
             hubPrepStmt.executeUpdate();
         } catch (SQLException se) {
             LOG.error("Erro ao carregar hub: " + se.getMessage());
         }
     }

     protected void loadRoom(Connection conn, int roomId, int hubId) {
         try (PreparedStatement roomPrepStmt = getInsertStatement(conn, IotBenchConstants.TABLENAME_ROOM)) {
             Room room = new Room();
             room.setRoomId(roomId);
             room.setName("Room" + roomId);
             room.setRoomType(roomId % 3); // Alterna entre tipos de sala (0, 1, 2)

             int idx = 1;
             roomPrepStmt.setInt(idx++, room.getRoomId());
             roomPrepStmt.setString(idx++, room.getName());
             roomPrepStmt.setInt(idx, room.getRoomType());
             roomPrepStmt.executeUpdate();
         } catch (SQLException se) {
             LOG.error("Erro ao carregar sala: " + se.getMessage());
         }
     }

     protected void loadDevice(Connection conn, int deviceId, int roomId, int hubId) {
         try (PreparedStatement devicePrepStmt = getInsertStatement(conn, IotBenchConstants.TABLENAME_DEVICE)) {
             Device device = new Device();
             device.setDeviceId(deviceId);
             device.setName("Device" + deviceId);
             device.setStatus("ligado");
             device.setDeviceType(deviceId % 2); // Alterna entre tipos de dispositivo (0 e 1)
             device.setRoomId(roomId);
             device.setHubId(hubId);

             int idx = 1;
             devicePrepStmt.setInt(idx++, device.getDeviceId());
             devicePrepStmt.setString(idx++, device.getName());
             devicePrepStmt.setString(idx++, device.getStatus());
             devicePrepStmt.setInt(idx++, device.getDeviceType());
             devicePrepStmt.setInt(idx++, device.getRoomId());
             devicePrepStmt.setInt(idx, device.getHubId());
             devicePrepStmt.executeUpdate();
         } catch (SQLException se) {
             LOG.error("Erro ao carregar dispositivo: " + se.getMessage());
         }
     }

     protected void loadSensor(Connection conn, int sensorId, int deviceId) {
         try (PreparedStatement sensorPrepStmt = getInsertStatement(conn, IotBenchConstants.TABLENAME_SENSOR)) {
             Sensor sensor = new Sensor();
             sensor.setSensorId(sensorId);
             sensor.setName("Sensor" + sensorId);
             sensor.setType(sensorId % 2); // Alterna entre tipos de sensor (0 e 1)
             sensor.setValue(25.0); // Valor inicial do sensor
             sensor.setDeviceId(deviceId);

             int idx = 1;
             sensorPrepStmt.setInt(idx++, sensor.getSensorId());
             sensorPrepStmt.setString(idx++, sensor.getName());
             sensorPrepStmt.setInt(idx++, sensor.getType());
             sensorPrepStmt.setDouble(idx++, sensor.getValue());
             sensorPrepStmt.setInt(idx, sensor.getDeviceId());
             sensorPrepStmt.executeUpdate();
         } catch (SQLException se) {
             LOG.error("Erro ao carregar sensor: " + se.getMessage());
         }
     }
 }