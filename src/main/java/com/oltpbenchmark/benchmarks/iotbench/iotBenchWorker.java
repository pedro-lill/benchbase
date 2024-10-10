package com.oltpbenchmark.benchmarks.iotbench;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.Procedure.UserAbortException;
import com.oltpbenchmark.api.TransactionType;
import com.oltpbenchmark.api.Worker;
import com.oltpbenchmark.benchmarks.iotbench.procedures.DeleteRecord;
import com.oltpbenchmark.benchmarks.iotbench.procedures.InsertRecord;
import com.oltpbenchmark.benchmarks.iotbench.procedures.ReadModifyWriteRecord;
import com.oltpbenchmark.benchmarks.iotbench.procedures.ReadRecord;
import com.oltpbenchmark.benchmarks.iotbench.procedures.ScanRecord;
import com.oltpbenchmark.benchmarks.iotbench.procedures.UpdateRecord;
import com.oltpbenchmark.distributions.CounterGenerator;
import com.oltpbenchmark.distributions.UniformGenerator;
import com.oltpbenchmark.distributions.ZipfianGenerator;
import com.oltpbenchmark.types.TransactionStatus;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * iotBenchWorker Implementation
 *
 * @author pavlo
 */
class iotBenchWorker extends Worker<iotBenchBenchmark> {

  private final ZipfianGenerator readRecord;
  private static CounterGenerator insertRecord;
  private final UniformGenerator randScan;

  private final double[] params = new double[3];
  private final double[] results = new double[3];

  private final UpdateRecord procUpdateRecord;
  private final ScanRecord procScanRecord;
  private final ReadRecord procReadRecord;
  private final ReadModifyWriteRecord procReadModifyWriteRecord;
  private final InsertRecord procInsertRecord;
  private final DeleteRecord procDeleteRecord;

  public iotBenchWorker(iotBenchBenchmark benchmarkModule, int id, int init_record_count) {
    super(benchmarkModule, id);
    this.readRecord =
        new ZipfianGenerator(
            rng(), init_record_count, benchmarkModule.skewFactor); // pool for read keys
    this.randScan = new UniformGenerator(1, iotBenchConstants.MAX_SCAN);

    synchronized (iotBenchWorker.class) {
      // We must know where to start inserting
      if (insertRecord == null) {
        insertRecord = new CounterGenerator(init_record_count);
      }
    }

    // This is a minor speed-up to avoid having to invoke the hashmap look-up
    // everytime we want to execute a txn. This is important to do on
    // a client machine with not a lot of cores    this.procUpdateRecord =
    // this.getProcedure(UpdateRecord.class);
    this.procScanRecord = this.getProcedure(ScanRecord.class);
    this.procReadRecord = this.getProcedure(ReadRecord.class);
    this.procReadModifyWriteRecord = this.getProcedure(ReadModifyWriteRecord.class);
    this.procInsertRecord = this.getProcedure(InsertRecord.class);
    this.procUpdateRecord = this.getProcedure(UpdateRecord.class);
    this.procDeleteRecord = this.getProcedure(DeleteRecord.class);
  }

  @Override
  protected TransactionStatus executeWork(Connection conn, TransactionType nextTrans)
      throws UserAbortException, SQLException {
    Class<? extends Procedure> procClass = nextTrans.getProcedureClass();

    if (procClass.equals(DeleteRecord.class)) {
      deleteRecord(conn);
    } else if (procClass.equals(InsertRecord.class)) {
      insertRecord(conn);
    } else if (procClass.equals(ReadModifyWriteRecord.class)) {
      readModifyWriteRecord(conn);
    } else if (procClass.equals(ReadRecord.class)) {
      readRecord(conn);
    } else if (procClass.equals(ScanRecord.class)) {
      scanRecord(conn);
    } else if (procClass.equals(UpdateRecord.class)) {
      updateRecord(conn);
    }
    return (TransactionStatus.SUCCESS);
  }

  private void updateRecord(Connection conn) throws SQLException {
    int keyname = readRecord.nextInt();
    this.buildParameters();
    this.procUpdateRecord.run(conn, keyname, this.params[0], this.params[1], this.params[2]);
  }

  private void scanRecord(Connection conn) throws SQLException {
    int keyname = readRecord.nextInt();
    int count = randScan.nextInt();
    this.procScanRecord.run(conn, keyname, count, new ArrayList<>());
  }

  private void readRecord(Connection conn) throws SQLException {
    int keyname = readRecord.nextInt();
    this.procReadRecord.run(conn, keyname, this.results);
  }

  private void readModifyWriteRecord(Connection conn) throws SQLException {
    int keyname = readRecord.nextInt();
    this.buildParameters();
    this.procReadModifyWriteRecord.run(
        conn, keyname, this.params[0], this.params[1], this.params[2], this.results);
  }

  private void insertRecord(Connection conn) throws SQLException {
    int keyname = insertRecord.nextInt();
    this.buildParameters();
    this.procInsertRecord.run(conn, keyname, this.params[0], this.params[1], this.params[2]);
  }

  private void deleteRecord(Connection conn) throws SQLException {
    int keyname = readRecord.nextInt();
    this.procDeleteRecord.run(conn, keyname);
  }

  private void buildParameters() {
    this.params[0] = Math.random() * 100;
    this.params[1] = Math.random() * 100;
    this.params[2] = Math.random() * 100;
  }
}
