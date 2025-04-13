package com.oltpbenchmark.benchmarks.benchio;

import com.oltpbenchmark.api.Procedure.UserAbortException;
import com.oltpbenchmark.api.TransactionType;
import com.oltpbenchmark.api.Worker;
import com.oltpbenchmark.benchmarks.benchio.procedures.BenchIOProcedure;
import com.oltpbenchmark.types.TransactionStatus;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BenchIOWorker extends Worker<BenchIOBenchmark> {

  private static final Logger LOG = LoggerFactory.getLogger(BenchIOWorker.class);

  private final int terminalHubID;
  private final int terminalRoomLowerID;
  private final int terminalRoomUpperID;
  private final int numHubs;
  private final Random gen;

  public BenchIOWorker(
      BenchIOBenchmark benchmarkModule,
      int id,
      int terminalHubID,
      int terminalRoomLowerID,
      int terminalRoomUpperID,
      int numHubs) {
    super(benchmarkModule, id);
    this.terminalHubID = terminalHubID;
    this.terminalRoomLowerID = terminalRoomLowerID;
    this.terminalRoomUpperID = terminalRoomUpperID;
    this.numHubs = numHubs;
    this.gen = benchmarkModule.getRandomGenerator(); // Usando o gerador do benchmark
  }

  @Override
  protected TransactionStatus executeWork(Connection conn, TransactionType nextTransaction)
      throws UserAbortException, SQLException {
    try {
      BenchIOProcedure proc =
          (BenchIOProcedure) this.getProcedure(nextTransaction.getProcedureClass());
      proc.run(conn, gen, terminalHubID, numHubs, terminalRoomLowerID, terminalRoomUpperID, this);
    } catch (ClassCastException ex) {
      LOG.error("Tipo de transação inválido: {}", nextTransaction, ex);
      throw new RuntimeException("Tipo de transação inválido = " + nextTransaction);
    }
    return TransactionStatus.SUCCESS;
  }

  @Override
  protected long getPreExecutionWaitInMillis(TransactionType type) {
    return type.getPreExecutionWait();
  }

  @Override
  protected long getPostExecutionWaitInMillis(TransactionType type) {
    long mean = type.getPostExecutionWait();
    float c = this.gen.nextFloat(); // Usando o gerador local
    long thinkTime = (long) (-1 * Math.log(c) * mean);
    return Math.min(thinkTime, 10 * mean);
  }
}
