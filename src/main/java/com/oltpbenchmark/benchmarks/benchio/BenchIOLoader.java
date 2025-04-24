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
 */

package com.oltpbenchmark.benchmarks.benchio;

import com.oltpbenchmark.api.Loader;
import com.oltpbenchmark.api.LoaderThread;
import com.oltpbenchmark.benchmarks.benchio.pojo.ActionLogs;
import com.oltpbenchmark.benchmarks.benchio.pojo.AutomationProfile;
import com.oltpbenchmark.benchmarks.benchio.pojo.Device;
import com.oltpbenchmark.benchmarks.benchio.pojo.Hub;
import com.oltpbenchmark.benchmarks.benchio.pojo.Room;
import com.oltpbenchmark.benchmarks.benchio.pojo.Sensor;
import com.oltpbenchmark.benchmarks.benchio.pojo.SensorLog;
import com.oltpbenchmark.benchmarks.benchio.pojo.UserTable;
import com.oltpbenchmark.catalog.Table;
import com.oltpbenchmark.util.SQLUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public final class BenchIOLoader extends Loader<BenchIOBenchmark> {

  private final int numUsers;
  private final int numHubs;
  private final int numRoomsPerHub;
  private final int numDevicesPerRoom;
  private final int numSensorsPerDevice;
  private final int numAutomationProfiles;
  private final int initialSensorLogsPerSensor;
  private final int initialActionLogsPerUser;

  private final Random random = new Random();

  public BenchIOLoader(BenchIOBenchmark benchmark) {
    super(benchmark);
    this.numUsers = BenchIOConfig.configUserCount;
    this.numHubs = BenchIOConfig.configHubCount;
    this.numRoomsPerHub = BenchIOConfig.configRoomsPerHub;
    this.numDevicesPerRoom = BenchIOConfig.configDevicesPerRoom;
    this.numSensorsPerDevice = BenchIOConfig.configSensorsPerDevice;
    this.numAutomationProfiles = BenchIOConfig.configAutomationProfiles;
    this.initialSensorLogsPerSensor = BenchIOConfig.configInitialSensorLogs;
    this.initialActionLogsPerUser = BenchIOConfig.configInitialActionLogs;
  }

  @Override
  public List<LoaderThread> createLoaderThreads() {
    List<LoaderThread> threads = new ArrayList<>();
    final CountDownLatch baseDataLatch = new CountDownLatch(1);

    // Thread 1: Carrega dados básicos (users, hubs, rooms, devices, sensors)
    threads.add(
        new LoaderThread(this.benchmark) {
          @Override
          public void load(Connection conn) {
            loadUsers(conn);
            loadHubs(conn);
            loadRooms(conn);
            loadDevices(conn);
            loadSensors(conn);
            baseDataLatch.countDown();
          }
        });

    // Thread 2: Carrega dados dependentes (automation profiles, logs)
    threads.add(
        new LoaderThread(this.benchmark) {
          @Override
          public void load(Connection conn) {
            try {
              baseDataLatch.await();
              loadAutomationProfiles(conn);
              loadInitialSensorLogs(conn);
              loadInitialActionLogs(conn);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              throw new RuntimeException("Loader thread interrupted", e);
            }
          }
        });

    return threads;
  }

  private PreparedStatement getInsertStatement(Connection conn, String tableName)
      throws SQLException {
    Table catalogTbl = benchmark.getCatalog().getTable(tableName);
    if (catalogTbl == null) {
      throw new RuntimeException("Table not found in catalog: " + tableName);
    }
    String sql = SQLUtil.getInsertSQL(catalogTbl, this.getDatabaseType());
    return conn.prepareStatement(sql);
  }

  protected void loadUsers(Connection conn) {
    try (PreparedStatement stmt = getInsertStatement(conn, BenchIOConstants.TABLENAME_USER)) {
      for (int i = 1; i <= numUsers; i++) {
        UserTable user = new UserTable();
        user.setUserId(i);
        user.setNameIot("User" + i);
        user.setEmail("user" + i + "@example.com");
        user.setPasswordHash("hash" + i);
        user.setUserType(i % 2);

        int idx = 1;
        stmt.setInt(idx++, user.getUserId());
        stmt.setString(idx++, user.getNameIot());
        stmt.setString(idx++, user.getEmail());
        stmt.setString(idx++, user.getPasswordHash());
        stmt.setInt(idx, user.getUserType());
        stmt.addBatch();

        if (i % workConf.getBatchSize() == 0) {
          stmt.executeBatch();
        }
      }
      stmt.executeBatch();
    } catch (SQLException se) {
      LOG.error("Error loading users: " + se.getMessage());
    }
  }

  protected void loadHubs(Connection conn) {
    try (PreparedStatement stmt = getInsertStatement(conn, BenchIOConstants.TABLENAME_HUB)) {
      for (int i = 1; i <= numHubs; i++) {
        Hub hub = new Hub();
        hub.setHubId(i);
        hub.setName("Hub" + i);
        hub.setStatus(random.nextBoolean() ? "active" : "inactive");

        int idx = 1;
        stmt.setInt(idx++, hub.getHubId());
        stmt.setString(idx++, hub.getName());
        stmt.setString(idx, hub.getStatus());
        stmt.addBatch();

        if (i % workConf.getBatchSize() == 0) {
          stmt.executeBatch();
        }
      }
      stmt.executeBatch();
    } catch (SQLException se) {
      LOG.error("Error loading hubs: " + se.getMessage());
    }
  }

  protected void loadRooms(Connection conn) {
    try (PreparedStatement stmt = getInsertStatement(conn, BenchIOConstants.TABLENAME_ROOM)) {
      int roomId = 1;
      for (int h = 1; h <= numHubs; h++) {
        for (int r = 1; r <= numRoomsPerHub; r++) {
          Room room = new Room();
          room.setRoomId(roomId);
          room.setName("Room" + roomId);
          room.setRoomType(roomId % 3); // 0=Bedroom, 1=Living, 2=Kitchen

          int idx = 1;
          stmt.setInt(idx++, room.getRoomId());
          stmt.setString(idx++, room.getName());
          stmt.setInt(idx, room.getRoomType());
          stmt.addBatch();

          if (roomId % workConf.getBatchSize() == 0) {
            stmt.executeBatch();
          }
          roomId++;
        }
      }
      stmt.executeBatch();
    } catch (SQLException se) {
      LOG.error("Error loading rooms: " + se.getMessage());
    }
  }

  protected void loadDevices(Connection conn) {
    try (PreparedStatement stmt = getInsertStatement(conn, BenchIOConstants.TABLENAME_DEVICE)) {
      int deviceId = 1;
      int roomId = 1;

      for (int h = 1; h <= numHubs; h++) {
        for (int r = 1; r <= numRoomsPerHub; r++) {
          for (int d = 1; d <= numDevicesPerRoom; d++) {
            Device device = new Device();
            device.setDeviceId(deviceId);
            device.setName("Device" + deviceId);
            device.setStatus(random.nextBoolean() ? "on" : "off");
            device.setDeviceType(deviceId % 2); // 0=Switch, 1=Sensor
            device.setRoomId(roomId);
            device.setHubId(h);

            int idx = 1;
            stmt.setInt(idx++, device.getDeviceId());
            stmt.setString(idx++, device.getName());
            stmt.setString(idx++, device.getStatus());
            stmt.setInt(idx++, device.getDeviceType());
            stmt.setInt(idx++, device.getRoomId());
            stmt.setInt(idx, device.getHubId());
            stmt.addBatch();

            if (deviceId % workConf.getBatchSize() == 0) {
              stmt.executeBatch();
            }
            deviceId++;
          }
          roomId++;
        }
      }
      stmt.executeBatch();
    } catch (SQLException se) {
      LOG.error("Error loading devices: " + se.getMessage());
    }
  }

  protected void loadSensors(Connection conn) {
    try (PreparedStatement stmt = getInsertStatement(conn, BenchIOConstants.TABLENAME_SENSOR)) {
      int sensorId = 1;
      int deviceId = 1;

      // Considerando que apenas dispositivos do tipo 1 são sensores
      for (int h = 1; h <= numHubs; h++) {
        for (int r = 1; r <= numRoomsPerHub; r++) {
          for (int d = 1; d <= numDevicesPerRoom; d++) {
            if (deviceId % 2 == 1) { // Apenas dispositivos ímpares são sensores
              for (int s = 1; s <= numSensorsPerDevice; s++) {
                Sensor sensor = new Sensor();
                sensor.setSensorId(sensorId);
                sensor.setName("Sensor" + sensorId);
                sensor.setType(sensorId % 2); // 0=Temperature, 1=Humidity
                sensor.setValue(random.nextDouble() * 100);
                sensor.setDeviceId(deviceId);

                int idx = 1;
                stmt.setInt(idx++, sensor.getSensorId());
                stmt.setString(idx++, sensor.getName());
                stmt.setInt(idx++, sensor.getType());
                stmt.setDouble(idx++, sensor.getValue());
                stmt.setInt(idx, sensor.getDeviceId());
                stmt.addBatch();

                if (sensorId % workConf.getBatchSize() == 0) {
                  stmt.executeBatch();
                }
                sensorId++;
              }
            }
            deviceId++;
          }
        }
      }
      stmt.executeBatch();
    } catch (SQLException se) {
      LOG.error("Error loading sensors: " + se.getMessage());
    }
  }

  protected void loadAutomationProfiles(Connection conn) {
    try (PreparedStatement stmt =
        getInsertStatement(conn, BenchIOConstants.TABLENAME_AUTOMATIONPROFILE)) {
      for (int i = 1; i <= numAutomationProfiles; i++) {
        AutomationProfile profile = new AutomationProfile();
        profile.setProfileId(i);
        profile.setDeviceId(random.nextInt(numDevicesPerRoom * numRoomsPerHub * numHubs) + 1);
        profile.setUserId(random.nextInt(numUsers) + 1);
        profile.setStatus("active");
        profile.setCommand("command_" + i);

        int idx = 1;
        stmt.setInt(idx++, profile.getProfileId());
        stmt.setInt(idx++, profile.getDeviceId());
        stmt.setInt(idx++, profile.getUserId());
        stmt.setString(idx++, profile.getStatus());
        stmt.setString(idx, profile.getCommand());
        stmt.addBatch();

        if (i % workConf.getBatchSize() == 0) {
          stmt.executeBatch();
        }
      }
      stmt.executeBatch();
    } catch (SQLException se) {
      LOG.error("Error loading automation profiles: " + se.getMessage());
    }
  }

  protected void loadInitialSensorLogs(Connection conn) {
    try (PreparedStatement stmt = getInsertStatement(conn, BenchIOConstants.TABLENAME_SENSORLOG)) {
      int logId = 1;

      // Para cada sensor, criar alguns logs históricos
      int totalSensors = numHubs * numRoomsPerHub * numDevicesPerRoom * numSensorsPerDevice;
      for (int s = 1; s <= totalSensors; s++) {
        for (int l = 1; l <= initialSensorLogsPerSensor; l++) {
          SensorLog log = new SensorLog();
          log.setId(logId);
          log.setSensorId(s);
          log.setValue(random.nextDouble() * 100);
          log.setDate(
              Timestamp.valueOf(
                  LocalDateTime.now().minusDays(l).minusMinutes(l).minusSeconds(l).minusNanos(l)));

          int idx = 1;
          stmt.setInt(idx++, log.getId());
          stmt.setInt(idx++, log.getSensorId());
          stmt.setDouble(idx++, log.getValue());
          stmt.setTimestamp(idx, log.getDate());
          stmt.addBatch();

          if (logId % workConf.getBatchSize() == 0) {
            stmt.executeBatch();
          }
          logId++;
        }
      }
      stmt.executeBatch();
    } catch (SQLException se) {
      LOG.error("Error loading sensor logs: " + se.getMessage());
    }
  }

  protected void loadInitialActionLogs(Connection conn) {
    try (PreparedStatement stmt = getInsertStatement(conn, BenchIOConstants.TABLENAME_ACTIONLOGS)) {
      int logId = 1;

      // Para cada usuário, criar alguns logs de ação
      for (int u = 1; u <= numUsers; u++) {
        for (int l = 1; l <= initialActionLogsPerUser; l++) {
          ActionLogs log = new ActionLogs();
          log.setLogId(logId);
          log.setUserId(u);
          log.setDeviceId(random.nextInt(numDevicesPerRoom * numRoomsPerHub * numHubs) + 1);
          log.setAction("action_" + l);
          log.setStatus(random.nextBoolean() ? "success" : "failed");
          log.setDate(
              Timestamp.valueOf(
                  LocalDateTime.now().minusDays(l).minusMinutes(l).minusSeconds(l).minusNanos(l)));

          int idx = 1;
          stmt.setInt(idx++, log.getLogId());
          stmt.setInt(idx++, log.getUserId());
          stmt.setInt(idx++, log.getDeviceId());
          stmt.setString(idx++, log.getAction());
          stmt.setString(idx++, log.getStatus());
          stmt.setTimestamp(idx, log.getDate());
          stmt.addBatch();

          if (logId % workConf.getBatchSize() == 0) {
            stmt.executeBatch();
          }
          logId++;
        }
      }
      stmt.executeBatch();
    } catch (SQLException se) {
      LOG.error("Error loading action logs: " + se.getMessage());
    }
  }
}
// Compare this snippet from
// src/main/java/com/oltpbenchmark/benchmarks/benchio/BenchIOConstants.java:
// /*
