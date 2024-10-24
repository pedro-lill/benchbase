package com.oltpbenchmark.benchmarks.iotbench;

import com.oltpbenchmark.api.Loader;
import com.oltpbenchmark.api.LoaderThread;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IotBenchLoader extends Loader<IotBenchBenchmark> {

  private static final Logger LOG = LoggerFactory.getLogger(IotBenchLoader.class);
  private final int numUsers;
  private final int numHubs;
  private final int numRooms;
  private final int numDevices;
  private final int numSensors;

  public IotBenchLoader(IotBenchBenchmark benchmark) {
    super(benchmark);
    this.numUsers = (int) Math.round(IotBenchConstants.NUM_USERS * this.scaleFactor);
    this.numHubs = (int) Math.round(IotBenchConstants.NUM_HUBS * this.scaleFactor);
    this.numRooms = (int) Math.round(IotBenchConstants.NUM_ROOMS * this.scaleFactor);
    this.numDevices = (int) Math.round(IotBenchConstants.NUM_DEVICES * this.scaleFactor);
    this.numSensors = (int) Math.round(IotBenchConstants.NUM_SENSORS * this.scaleFactor);
  }

  @Override
  public List<LoaderThread> createLoaderThreads() {
    List<LoaderThread> threads = new ArrayList<>();
    final int numLoaders = this.benchmark.getWorkloadConfiguration().getLoaderThreads();

    final int usersPerThread = Math.max(this.numUsers / numLoaders, 1);
    final int hubsPerThread = Math.max(this.numHubs / numLoaders, 1);
    final int roomsPerThread = Math.max(this.numRooms / numLoaders, 1);
    final int devicesPerThread = Math.max(this.numDevices / numLoaders, 1);
    final int sensorsPerThread = Math.max(this.numSensors / numLoaders, 1);

    final CountDownLatch latch = new CountDownLatch(numLoaders);

    // Carregando usuários
    for (int i = 0; i < numLoaders; i++) {
      final int start = i * usersPerThread;
      final int stop = Math.min(this.numUsers, (i + 1) * usersPerThread);

      threads.add(
          new LoaderThread(this.benchmark) {
            @Override
            public void load(Connection conn) throws SQLException {
              loadUsers(conn, start, stop);
            }

            @Override
            public void afterLoad() {
              latch.countDown();
            }
          });
    }

    // Carregando hubs
    for (int i = 0; i < numLoaders; i++) {
      final int start = i * hubsPerThread;
      final int stop = Math.min(this.numHubs, (i + 1) * hubsPerThread);

      threads.add(
          new LoaderThread(this.benchmark) {
            @Override
            public void load(Connection conn) throws SQLException {
              loadHubs(conn, start, stop);
            }

            @Override
            public void beforeLoad() {
              try {
                latch.await(); // Espera carregar usuários antes de carregar hubs
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
            }
          });
    }

    // Carregando salas
    for (int i = 0; i < numLoaders; i++) {
      final int start = i * roomsPerThread;
      final int stop = Math.min(this.numRooms, (i + 1) * roomsPerThread);

      threads.add(
          new LoaderThread(this.benchmark) {
            @Override
            public void load(Connection conn) throws SQLException {
              loadRooms(conn, start, stop);
            }

            @Override
            public void beforeLoad() {
              try {
                latch.await();
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
            }
          });
    }

    // Carregando dispositivos
    for (int i = 0; i < numLoaders; i++) {
      final int start = i * devicesPerThread;
      final int stop = Math.min(this.numDevices, (i + 1) * devicesPerThread);

      threads.add(
          new LoaderThread(this.benchmark) {
            @Override
            public void load(Connection conn) throws SQLException {
              loadDevices(conn, start, stop);
            }

            @Override
            public void beforeLoad() {
              try {
                latch.await();
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
            }
          });
    }

    // Carregando sensores
    for (int i = 0; i < numLoaders; i++) {
      final int start = i * sensorsPerThread;
      final int stop = Math.min(this.numSensors, (i + 1) * sensorsPerThread);

      threads.add(
          new LoaderThread(this.benchmark) {
            @Override
            public void load(Connection conn) throws SQLException {
              loadSensors(conn, start, stop);
            }

            @Override
            public void beforeLoad() {
              try {
                latch.await();
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
            }
          });
    }

    return threads;
  }

  private void loadUsers(Connection conn, int start, int stop) throws SQLException {
    String sqlInsertUser =
        "INSERT INTO UserTable (userId, name, email, password_hash, userType) VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement stmtUser = conn.prepareStatement(sqlInsertUser)) {
      for (int i = start; i < stop; i++) {
        stmtUser.setInt(1, i);
        stmtUser.setString(2, "User_" + i);
        stmtUser.setString(3, "user" + i + "@iotbench.com");
        stmtUser.setString(4, "hash" + i);
        stmtUser.setInt(5, (i % 3) + 1);
        stmtUser.addBatch();

        if (i % 1000 == 0 || i == stop - 1) {
          stmtUser.executeBatch();
          LOG.info("Loaded {} users.", i + 1);
        }
      }
    }
  }

  // Função para carregar hubs
  private void loadHubs(Connection conn, int start, int stop) throws SQLException {
    String sqlInsertHub = "INSERT INTO Hub (hubId, name, status) VALUES (?, ?, ?)";
    try (PreparedStatement stmtHub = conn.prepareStatement(sqlInsertHub)) {
      for (int i = start; i < stop; i++) {
        stmtHub.setInt(1, i);
        stmtHub.setString(2, "Hub_" + i);
        stmtHub.setString(3, "active");
        stmtHub.addBatch();
      }
      stmtHub.executeBatch();
      LOG.info("Loaded {} hubs.", stop - start);
    }
  }

  // Função para carregar salas
  private void loadRooms(Connection conn, int start, int stop) throws SQLException {
    String sqlInsertRoom = "INSERT INTO Room (roomId, name, room_type) VALUES (?, ?, ?)";
    try (PreparedStatement stmtRoom = conn.prepareStatement(sqlInsertRoom)) {
      for (int i = start; i < stop; i++) {
        stmtRoom.setInt(1, i);
        stmtRoom.setString(2, "Room_" + i);
        stmtRoom.setInt(3, 1);
        stmtRoom.addBatch();
      }
      stmtRoom.executeBatch();
      LOG.info("Loaded {} rooms.", stop - start);
    }
  }

  // Função para carregar dispositivos
  private void loadDevices(Connection conn, int start, int stop) throws SQLException {
    String sqlInsertDevice =
        "INSERT INTO Device (deviceId, name, status, device_type, room_id, hub_id) VALUES (?, ?, ?, ?, ?, ?)";
    try (PreparedStatement stmtDevice = conn.prepareStatement(sqlInsertDevice)) {
      for (int i = start; i < stop; i++) {
        stmtDevice.setInt(1, i);
        stmtDevice.setString(2, "Device_" + i);
        stmtDevice.setString(3, "active");
        stmtDevice.setInt(4, 1);
        stmtDevice.setInt(5, i);
        stmtDevice.setInt(6, i);
        stmtDevice.addBatch();
      }
      stmtDevice.executeBatch();
      LOG.info("Loaded {} devices.", stop - start);
    }
  }

  // Função para carregar sensores
  private void loadSensors(Connection conn, int start, int stop) throws SQLException {
    String sqlInsertSensor =
        "INSERT INTO Sensor (sensorId, name, sensor_type, device_id) VALUES (?, ?, ?, ?)";
    try (PreparedStatement stmtSensor = conn.prepareStatement(sqlInsertSensor)) {
      for (int i = start; i < stop; i++) {
        stmtSensor.setInt(1, i);
        stmtSensor.setString(2, "Sensor_" + i);
        stmtSensor.setString(3, "temperature");
        stmtSensor.setInt(4, i);
        stmtSensor.addBatch();
      }
      stmtSensor.executeBatch();
      LOG.info("Loaded {} sensors.", stop - start);
    }
  }
}
