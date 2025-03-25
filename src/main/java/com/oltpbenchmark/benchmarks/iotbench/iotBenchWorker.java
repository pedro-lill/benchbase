package com.oltpbenchmark.benchmarks.iotbench;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.Procedure.UserAbortException;
import com.oltpbenchmark.api.TransactionType;
import com.oltpbenchmark.benchmarks.iotbench.procedures.GetActiveSensorsPerRoom;
import com.oltpbenchmark.benchmarks.iotbench.procedures.GetSensorsAndDevicesFromRoom;
import com.oltpbenchmark.benchmarks.iotbench.procedures.InsertActionLogRecord;
import com.oltpbenchmark.benchmarks.iotbench.procedures.InsertSensorLogRecord;
import com.oltpbenchmark.benchmarks.iotbench.procedures.InsertSensorRecord;
import com.oltpbenchmark.benchmarks.iotbench.procedures.InsertUserRecord;
import com.oltpbenchmark.types.TransactionStatus;
import com.oltpbenchmark.util.RandomGenerator;
import java.sql.Connection;
import java.sql.SQLException;

class IotBenchWorker extends com.oltpbenchmark.api.Worker<IotBenchBenchmark> {

  private final RandomGenerator randScan;

  // Procedimentos
  private final GetSensorsAndDevicesFromRoom procGetSensorsAndDevicesFromRoom;
  private final InsertSensorLogRecord procInsertSensorLogRecord;
  private final InsertSensorRecord procInsertSensorRecord;
  private final InsertActionLogRecord procInsertActionLogRecord;
  private final InsertUserRecord procInsertUserRecord;
  private final GetActiveSensorsPerRoom procGetActiveSensorsPerRoom; // Novo procedimento

  public IotBenchWorker(IotBenchBenchmark benchmarkModule, int id, int init_record_count) {
    super(benchmarkModule, init_record_count);

    this.randScan = new RandomGenerator(init_record_count);

    this.procGetSensorsAndDevicesFromRoom = this.getProcedure(GetSensorsAndDevicesFromRoom.class);
    this.procInsertSensorLogRecord = this.getProcedure(InsertSensorLogRecord.class);
    this.procInsertSensorRecord = this.getProcedure(InsertSensorRecord.class);
    this.procInsertUserRecord = this.getProcedure(InsertUserRecord.class);
    this.procInsertActionLogRecord = this.getProcedure(InsertActionLogRecord.class);
    this.procGetActiveSensorsPerRoom = this.getProcedure(GetActiveSensorsPerRoom.class);
  }

  @Override
  protected TransactionStatus executeWork(Connection conn, TransactionType nextTrans)
      throws UserAbortException, SQLException {

    // Identifica o tipo de transação a ser executada e chama o procedimento correspondente
    Class<? extends Procedure> procClass = nextTrans.getProcedureClass();

    if (procClass.equals(GetSensorsAndDevicesFromRoom.class)) {
      getSensorsAndDevicesFromRoom(conn);
    } else if (procClass.equals(InsertSensorLogRecord.class)) {
      insertSensorLogRecord(conn);
    } else if (procClass.equals(InsertSensorRecord.class)) {
      insertSensorRecord(conn);
    } else if (procClass.equals(InsertUserRecord.class)) {
      insertUserRecord(conn);
    } else if (procClass.equals(InsertActionLogRecord.class)) {
      insertActionLogRecord(conn);
    } else if (procClass.equals(GetActiveSensorsPerRoom.class)) { // Nova transação
      GetActiveSensorsPerRoom(conn);
    } else {
      throw new RuntimeException("Unknown procedure class: " + procClass.getName());
    }

    return TransactionStatus.SUCCESS;
  }

  private void insertUserRecord(Connection conn) throws SQLException {
    int userId = randScan.nextInt(1000);
    String name = "User-" + userId;
    String email = "user" + userId + "@iotbench.com";
    String passwordHash = "hash" + userId;
    int userType = randScan.nextInt(3) + 1;

    this.procInsertUserRecord.run(conn, userId, name, email, passwordHash, userType);
  }

  private void insertActionLogRecord(Connection conn) throws SQLException {
    int logId = randScan.nextInt();
    int userId = randScan.nextInt();
    int deviceId = randScan.nextInt();
    String action = "ACTIVATE";
    String status = "SUCCESS";

    this.procInsertActionLogRecord.run(conn, logId, userId, deviceId, action, status);
  }

  private void getSensorsAndDevicesFromRoom(Connection conn) throws SQLException {
    int roomId = randScan.nextInt();
    this.procGetSensorsAndDevicesFromRoom.run(conn, roomId);
  }

  private void insertSensorLogRecord(Connection conn) throws SQLException {
    int sensorId = randScan.nextInt();
    double sensorValue = randScan.nextDouble();
    this.procInsertSensorLogRecord.run(conn, sensorId, sensorValue);
  }

  private void insertSensorRecord(Connection conn) throws SQLException {
    int sensorId = randScan.nextInt();
    String sensorName = "Sensor-" + sensorId;
    int sensorType = randScan.nextInt(5);
    double sensorValue = randScan.nextDouble();
    int deviceId = randScan.nextInt();

    this.procInsertSensorRecord.run(conn, sensorId, sensorName, sensorType, sensorValue, deviceId);
  }

  private void GetActiveSensorsPerRoom(Connection conn) throws SQLException {
    this.procGetActiveSensorsPerRoom.run(conn);
  }
}
