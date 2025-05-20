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

package com.oltpbenchmark.benchmarks.iotbench;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oltpbenchmark.api.Loader;
import com.oltpbenchmark.api.LoaderThread;
import com.oltpbenchmark.catalog.Table;
import com.oltpbenchmark.util.SQLUtil;

public final class IotBenchLoader extends Loader<IotBenchBenchmark> {

  private static final Logger LOG = LoggerFactory.getLogger(IotBenchLoader.class);

  private final int numUsers;
  private final int numHubs;
  private final int numRooms;
  private final Random rand = new Random();

  public IotBenchLoader(IotBenchBenchmark benchmark) {
    super(benchmark);
    this.numUsers = (int) Math.round(benchmark.getWorkloadConfiguration().getScaleFactor() * 1000);
    this.numHubs = 10; // Fixed number of hubs
    this.numRooms = numHubs * 5; // 5 rooms per hub
  }

  @Override
  public List<LoaderThread> createLoaderThreads() {
    try (Connection conn = this.benchmark.makeConnection()) {
      clearAllTables(conn);
    } catch (SQLException e) {
      LOG.error("Error clearing tables", e);
    }

    List<LoaderThread> threads = new ArrayList<>();
    final CountDownLatch userLatch = new CountDownLatch(1);
    final CountDownLatch hubLatch = new CountDownLatch(1);

    threads.add(
        new LoaderThread(this.benchmark) {
          @Override
          public void load(Connection conn) {
            loadUsers(conn, numUsers);
          }

          @Override
          public void afterLoad() {
            userLatch.countDown();
          }
        });

    threads.add(
        new LoaderThread(this.benchmark) {
          @Override
          public void load(Connection conn) {
            loadHubs(conn, numHubs);
          }

          @Override
          public void afterLoad() {
            hubLatch.countDown();
          }
        });

    threads.add(
        new LoaderThread(this.benchmark) {
          @Override
          public void load(Connection conn) {
            try {
              hubLatch.await();
              loadRooms(conn, numRooms, numHubs);
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
          }
        });

    threads.add(
        new LoaderThread(this.benchmark) {
          @Override
          public void load(Connection conn) {
            try {
              userLatch.await();
              hubLatch.await();
              loadDevices(conn, numRooms * 10, numRooms, numHubs);
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
          }
        });

    threads.add(
        new LoaderThread(this.benchmark) {
          @Override
          public void load(Connection conn) {
            try {
              Thread.sleep(2000); // Small delay
              loadSensors(conn, numRooms * 20, numRooms * 10);
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
          }
        });

    return threads;
  }

  private void clearAllTables(Connection conn) throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      stmt.execute("SET CONSTRAINTS ALL DEFERRED");
    }

    String[] tables = {
      "actionlogs", "automationprofile", "sensorlog", "sensor", "device", "room", "hub", "usertable"
    };

    for (String table : tables) {
      try (Statement stmt = conn.createStatement()) {
        stmt.execute("TRUNCATE TABLE " + table + " CASCADE");
        LOG.info("Cleared table: {}", table);
      } catch (SQLException e) {
        LOG.warn("Error clearing table {}: {}", table, e.getMessage());
      }
    }

    try (Statement stmt = conn.createStatement()) {
      stmt.execute("SET CONSTRAINTS ALL IMMEDIATE");
    }
  }

  private PreparedStatement getInsertStatement(Connection conn, String tableName)
      throws SQLException {
    Table catalog_tbl = benchmark.getCatalog().getTable(tableName);
    String sql = SQLUtil.getInsertSQL(catalog_tbl, this.getDatabaseType());
    return conn.prepareStatement(sql);
  }

  private int getNextId(Connection conn, String tableName, String idColumn) throws SQLException {
    try (Statement stmt = conn.createStatement();
        ResultSet rs =
            stmt.executeQuery("SELECT COALESCE(MAX(" + idColumn + "), 0) + 1 FROM " + tableName)) {
      return rs.next() ? rs.getInt(1) : 1;
    }
  }

  protected void loadUsers(Connection conn, int userCount) {
    try {
      int startId = getNextId(conn, "usertable", "user_id");

      try (PreparedStatement stmt =
          getInsertStatement(conn, IotBenchConstants.TABLENAME_USERTABLE)) {
        int batchSize = 0;
        for (int i = 0; i < userCount; i++) {
          int userId = startId + i;
          stmt.setInt(1, userId);
          stmt.setString(2, "User_" + userId);
          stmt.setString(3, "user" + userId + "@example.com");
          stmt.setString(4, "hash_" + userId);
          stmt.setInt(5, i % 3);
          stmt.addBatch();

          if (++batchSize % workConf.getBatchSize() == 0) {
            stmt.executeBatch();
            batchSize = 0;
          }
        }
        if (batchSize > 0) {
          stmt.executeBatch();
        }
        LOG.info("Loaded {} users", userCount);
      }
    } catch (SQLException e) {
      LOG.error("Error loading users", e);
    }
  }

  protected void loadHubs(Connection conn, int hubCount) {
    try {
      int startId = getNextId(conn, "hub", "hub_id");

      try (PreparedStatement stmt = getInsertStatement(conn, IotBenchConstants.TABLENAME_HUB)) {
        for (int i = 0; i < hubCount; i++) {
          int hubId = startId + i;
          stmt.setInt(1, hubId);
          stmt.setString(2, "Hub_" + hubId);
          stmt.setString(3, "ACTIVE");
          stmt.executeUpdate();
        }
        LOG.info("Loaded {} hubs", hubCount);
      }
    } catch (SQLException e) {
      LOG.error("Error loading hubs", e);
    }
  }

  protected void loadRooms(Connection conn, int roomCount, int hubCount) {
    try {
      int startId = getNextId(conn, "room", "room_id");

      try (PreparedStatement stmt = getInsertStatement(conn, IotBenchConstants.TABLENAME_ROOM)) {
        int batchSize = 0;
        for (int i = 0; i < roomCount; i++) {
          int roomId = startId + i;
          stmt.setInt(1, roomId);
          stmt.setString(2, "Room_" + roomId);
          stmt.setInt(3, i % 5);
          stmt.addBatch();

          if (++batchSize % workConf.getBatchSize() == 0) {
            stmt.executeBatch();
            batchSize = 0;
          }
        }
        if (batchSize > 0) {
          stmt.executeBatch();
        }
        LOG.info("Loaded {} rooms", roomCount);
      }
    } catch (SQLException e) {
      LOG.error("Error loading rooms", e);
    }
  }

  protected void loadDevices(Connection conn, int deviceCount, int roomCount, int hubCount) {
    try {
      int startId = getNextId(conn, "device", "device_id");

      try (PreparedStatement stmt = getInsertStatement(conn, IotBenchConstants.TABLENAME_DEVICE)) {
        int batchSize = 0;
        for (int i = 0; i < deviceCount; i++) {
          int deviceId = startId + i;
          try {
            stmt.setInt(1, deviceId);
            stmt.setString(2, "Device_" + deviceId);
            stmt.setString(3, rand.nextBoolean() ? "ON" : "OFF");
            stmt.setInt(4, i % 10);
            stmt.setInt(5, (i % roomCount) + 1);
            stmt.setInt(6, (i % hubCount) + 1);
            stmt.addBatch();

            if (++batchSize % workConf.getBatchSize() == 0) {
              stmt.executeBatch();
              batchSize = 0;
            }
          } catch (SQLException e) {
            if (e.getMessage().contains("duplicar valor da chave")) {
              LOG.warn("Duplicate device_id skipped: {}", deviceId);
              continue;
            }
            throw e;
          }
        }
        if (batchSize > 0) {
          stmt.executeBatch();
        }
        LOG.info("Loaded {} devices", deviceCount);
      }
    } catch (SQLException e) {
      LOG.error("Error loading devices", e);
    }
  }

  protected void loadSensors(Connection conn, int sensorCount, int deviceCount) {
    try {
      int startId = getNextId(conn, "sensor", "sensor_id");

      try (PreparedStatement stmt = getInsertStatement(conn, IotBenchConstants.TABLENAME_SENSOR)) {
        int batchSize = 0;
        for (int i = 0; i < sensorCount; i++) {
          int sensorId = startId + i;
          try {
            stmt.setInt(1, sensorId);
            stmt.setString(2, "Sensor_" + sensorId);
            stmt.setInt(3, i % 5);
            stmt.setDouble(4, rand.nextDouble() * 100);
            stmt.setInt(5, (i % deviceCount) + 1);
            stmt.addBatch();

            if (++batchSize % workConf.getBatchSize() == 0) {
              stmt.executeBatch();
              batchSize = 0;
            }
          } catch (SQLException e) {
            if (e.getMessage().contains("duplicar valor da chave")) {
              LOG.warn("Duplicate sensor_id skipped: {}", sensorId);
              continue;
            }
            throw e;
          }
        }
        if (batchSize > 0) {
          stmt.executeBatch();
        }
        LOG.info("Loaded {} sensors", sensorCount);
      }
    } catch (SQLException e) {
      LOG.error("Error loading sensors", e);
    }
  }
}
