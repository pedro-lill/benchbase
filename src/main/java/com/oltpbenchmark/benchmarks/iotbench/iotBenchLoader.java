package com.oltpbenchmark.benchmarks.iotbench;

import com.oltpbenchmark.api.Loader;
import com.oltpbenchmark.api.LoaderThread;
import com.oltpbenchmark.catalog.Table;
import com.oltpbenchmark.util.SQLUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class IotBenchLoader extends Loader<IotBenchBenchmark> {

  private final Random rand = new Random();
  private static final int BATCH_SIZE = 1000;

  public IotBenchLoader(IotBenchBenchmark benchmark) {
    super(benchmark);
  }

  @Override
  public List<LoaderThread> createLoaderThreads() {
    List<LoaderThread> threads = new ArrayList<>();

    threads.add(
        new LoaderThread(this.benchmark) {
          @Override
          public void load(Connection conn) throws SQLException {
            loadUsers(conn);
          }
        });

    threads.add(
        new LoaderThread(this.benchmark) {
          @Override
          public void load(Connection conn) throws SQLException {
            loadRooms(conn);
          }
        });

    threads.add(
        new LoaderThread(this.benchmark) {
          @Override
          public void load(Connection conn) throws SQLException {
            loadHubs(conn);
          }
        });

    threads.add(
        new LoaderThread(this.benchmark) {
          @Override
          public void load(Connection conn) throws SQLException {
            loadSensors(conn);
          }
        });

    threads.add(
        new LoaderThread(this.benchmark) {
          @Override
          public void load(Connection conn) throws SQLException {
            loadDevices(conn);
          }
        });

    threads.add(
        new LoaderThread(this.benchmark) {
          @Override
          public void load(Connection conn) throws SQLException {
            loadSensorLogs(conn);
          }
        });

    threads.add(
        new LoaderThread(this.benchmark) {
          @Override
          public void load(Connection conn) throws SQLException {
            loadActionLogs(conn);
          }
        });

    return threads;
  }

  private void loadUsers(Connection conn) throws SQLException {
    Table catalog_tbl = benchmark.getCatalog().getTable("usertable");
    try (PreparedStatement ps =
        conn.prepareStatement(SQLUtil.getInsertSQL(catalog_tbl, this.getDatabaseType()))) {
      for (int i = 0; i < this.benchmark.numUsers; i++) {
        ps.setString(1, "User" + (i + 1));
        ps.setString(2, "user" + (i + 1) + "@example.com");
        ps.setString(3, "hashed_password");
        ps.setInt(4, 1);
        ps.addBatch();
        if (i % BATCH_SIZE == 0) ps.executeBatch();
      }
      ps.executeBatch();
    }
  }

  private void loadRooms(Connection conn) throws SQLException {
    Table catalog_tbl = benchmark.getCatalog().getTable("room");
    try (PreparedStatement ps =
        conn.prepareStatement(SQLUtil.getInsertSQL(catalog_tbl, this.getDatabaseType()))) {
      for (int i = 0; i < this.benchmark.numRooms; i++) {
        ps.setInt(1, i + 1);
        ps.setString(2, "Room" + (i + 1));
        ps.setInt(3, i % 2);
        ps.addBatch();
        if (i % BATCH_SIZE == 0) ps.executeBatch();
      }
      ps.executeBatch();
    }
  }

  private void loadHubs(Connection conn) throws SQLException {
    Table catalog_tbl = benchmark.getCatalog().getTable("hub");
    try (PreparedStatement ps =
        conn.prepareStatement(SQLUtil.getInsertSQL(catalog_tbl, this.getDatabaseType()))) {
      for (int i = 0; i < this.benchmark.numHubs; i++) {
        ps.setInt(1, i + 1);
        ps.setString(2, "Hub" + (i + 1));
        ps.setString(3, i % 2 == 0 ? "ACTIVE" : "INACTIVE");
        ps.addBatch();
        if (i % BATCH_SIZE == 0) ps.executeBatch();
      }
      ps.executeBatch();
    }
  }

  private void loadSensors(Connection conn) throws SQLException {
    Table catalog_tbl = benchmark.getCatalog().getTable("sensor");
    try (PreparedStatement ps =
        conn.prepareStatement(SQLUtil.getInsertSQL(catalog_tbl, this.getDatabaseType()))) {
      for (int i = 0; i < this.benchmark.numSensors; i++) {
        ps.setInt(1, i + 1);
        ps.setString(2, "Sensor" + (i + 1));
        ps.setString(3, "Type" + (i % 3));
        // valoress para o campo vlaue e o device_id
        ps.setDouble(4, 20 + rand.nextDouble() * 10);
        ps.setInt(5, 1 + rand.nextInt(this.benchmark.numHubs));
        ps.addBatch();
        if (i % BATCH_SIZE == 0) ps.executeBatch();
      }
      ps.executeBatch();
    }
  }

  private void loadSensorLogs(Connection conn) throws SQLException {
    Table catalog_tbl = benchmark.getCatalog().getTable("sensorlog");
    try (PreparedStatement ps =
        conn.prepareStatement(SQLUtil.getInsertSQL(catalog_tbl, this.getDatabaseType()))) {
      for (int i = 0; i < this.benchmark.numSensorLogs; i++) {
        int sensor_id = 1 + rand.nextInt(this.benchmark.numSensors); // FK válida

        ps.setInt(1, i + 1); // log_id
        ps.setInt(2, sensor_id); // sensor_id
        ps.setDouble(4, 20 + rand.nextDouble() * 10);
        ps.setTimestamp(
            3, new java.sql.Timestamp(System.currentTimeMillis() - rand.nextInt(1000000000)));
        ps.addBatch();
        if (i % BATCH_SIZE == 0) ps.executeBatch();
      }
      ps.executeBatch();
    }
  }

  private void loadDevices(Connection conn) throws SQLException {
    Table catalog_tbl = benchmark.getCatalog().getTable("device");
    try (PreparedStatement ps =
        conn.prepareStatement(SQLUtil.getInsertSQL(catalog_tbl, this.getDatabaseType()))) {
      for (int i = 0; i < this.benchmark.numDevices; i++) {
        ps.setInt(1, i + 1);
        ps.setString(2, "Device" + (i + 1));
        ps.setString(3, "Type" + (i % 3));
        ps.setInt(4, 1 + rand.nextInt(this.benchmark.numUsers));
        ps.addBatch();
        if (i % BATCH_SIZE == 0) ps.executeBatch();
      }
      ps.executeBatch();
    }
  }

  private void loadActionLogs(Connection conn) throws SQLException {
    Table catalog_tbl = benchmark.getCatalog().getTable("actionlogs");
    try (PreparedStatement ps =
        conn.prepareStatement(SQLUtil.getInsertSQL(catalog_tbl, this.getDatabaseType()))) {
      for (int i = 0; i < this.benchmark.numActionLogs; i++) {
        int user_id = 1 + rand.nextInt(this.benchmark.numUsers);
        int device_id = 1 + rand.nextInt(this.benchmark.numUsers);
        ps.setInt(1, i + 1);
        ps.setInt(2, user_id);
        ps.setInt(3, device_id);
        ps.setString(4, "ACTION_" + (i + 1));
        ps.setString(5, (i % 2 == 0) ? "SUCCESS" : "FAILURE");
        ps.setTimestamp(
            6, new java.sql.Timestamp(System.currentTimeMillis() - rand.nextInt(1000000000)));

        ps.addBatch();
        if (i % BATCH_SIZE == 0) ps.executeBatch();
      }
      ps.executeBatch();
    }
  }
}
