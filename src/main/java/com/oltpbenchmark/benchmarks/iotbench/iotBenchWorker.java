package com.oltpbenchmark.benchmarks.iotbench;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.Procedure.UserAbortException;
import com.oltpbenchmark.api.TransactionType;
import com.oltpbenchmark.benchmarks.iotbench.procedures.AutomationTrigger;
import com.oltpbenchmark.benchmarks.iotbench.procedures.DeviceControl;
import com.oltpbenchmark.benchmarks.iotbench.procedures.GetDeviceStatus;
import com.oltpbenchmark.benchmarks.iotbench.procedures.GetRoomOverview;
import com.oltpbenchmark.benchmarks.iotbench.procedures.GetSensorHistory;
import com.oltpbenchmark.benchmarks.iotbench.procedures.SensorReading;
import com.oltpbenchmark.types.TransactionStatus;
import com.oltpbenchmark.util.RandomGenerator;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

class IotBenchWorker extends com.oltpbenchmark.api.Worker<IotBenchBenchmark> {

  private final RandomGenerator randScan;

  private final SensorReading procSensorReading;
  private final DeviceControl procDeviceControl;
  private final AutomationTrigger procAutomationTrigger;
  private final GetDeviceStatus procGetDeviceStatus;
  private final GetSensorHistory procGetSensorHistory;
  private final GetRoomOverview procGetRoomOverview;

  public IotBenchWorker(IotBenchBenchmark benchmarkModule, int id, int init_record_count) {
    super(benchmarkModule, init_record_count);

    this.randScan = new RandomGenerator(init_record_count);

    this.procSensorReading = this.getProcedure(SensorReading.class);
    this.procDeviceControl = this.getProcedure(DeviceControl.class);
    this.procAutomationTrigger = this.getProcedure(AutomationTrigger.class);
    this.procGetDeviceStatus = this.getProcedure(GetDeviceStatus.class);
    this.procGetSensorHistory = this.getProcedure(GetSensorHistory.class);
    this.procGetRoomOverview = this.getProcedure(GetRoomOverview.class);
  }

  @Override
  protected TransactionStatus executeWork(Connection conn, TransactionType nextTrans)
      throws UserAbortException, SQLException {

    Class<? extends Procedure> procClass = nextTrans.getProcedureClass();

    if (procClass.equals(SensorReading.class)) {
      sensorReading(conn);
    } else if (procClass.equals(DeviceControl.class)) {
      deviceControl(conn);
    } else if (procClass.equals(AutomationTrigger.class)) {
      automationTrigger(conn);
    } else if (procClass.equals(GetDeviceStatus.class)) {
      getDeviceStatus(conn);
    } else if (procClass.equals(GetSensorHistory.class)) {
      getSensorHistory(conn);
    } else if (procClass.equals(GetRoomOverview.class)) {
      getRoomOverview(conn);
    } else {
      throw new RuntimeException("Unknown procedure class: " + procClass.getName());
    }

    return TransactionStatus.SUCCESS;
  }

  private void sensorReading(Connection conn) throws SQLException {
    int sensorId = randScan.nextInt(IotBenchConstants.NUM_SENSORS) + 1;
    double value = randScan.nextDouble() * 100;
    this.procSensorReading.run(conn, sensorId, value);
  }

  private void deviceControl(Connection conn) throws SQLException {
    int userId = randScan.nextInt(IotBenchConstants.NUM_USERS) + 1;
    int deviceId = randScan.nextInt(IotBenchConstants.NUM_DEVICES) + 1;
    String action = randScan.nextBoolean() ? "ON" : "OFF";
    String newStatus = action.equalsIgnoreCase("ON") ? "Active" : "Inactive";
    this.procDeviceControl.run(conn, userId, deviceId, action, newStatus);
  }

  private void automationTrigger(Connection conn) throws SQLException {
    int profileId = randScan.nextInt(IotBenchConstants.NUM_AUTOMATION_PROFILES) + 1;
    int triggeringSensorId = randScan.nextInt(IotBenchConstants.NUM_SENSORS) + 1;
    double triggerValue = randScan.nextDouble() * 50;
    int userId = randScan.nextInt(IotBenchConstants.NUM_USERS) + 1;
    this.procAutomationTrigger.run(conn, profileId, triggeringSensorId, triggerValue, userId);
  }

  private void getDeviceStatus(Connection conn) throws SQLException {
    int deviceId = randScan.nextInt(IotBenchConstants.NUM_DEVICES) + 1;
    this.procGetDeviceStatus.run(conn, deviceId);
  }

  private void getSensorHistory(Connection conn) throws SQLException {
    int sensorId = randScan.nextInt(IotBenchConstants.NUM_SENSORS) + 1;
    long now = System.currentTimeMillis();
    long past = now - (long) (randScan.nextDouble() * 3600 * 1000);
    Timestamp startDate = new Timestamp(past);
    Timestamp endDate = new Timestamp(now);
    this.procGetSensorHistory.run(conn, sensorId, startDate, endDate);
  }

  private void getRoomOverview(Connection conn) throws SQLException {
    int roomId = randScan.nextInt(IotBenchConstants.NUM_ROOMS) + 1;
    this.procGetRoomOverview.run(conn, roomId);
  }
}
