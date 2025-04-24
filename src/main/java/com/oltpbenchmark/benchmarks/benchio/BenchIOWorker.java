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

  // Configurações de partição de dados
  private final int terminalHubID;
  private final int terminalRoomLowerID;
  private final int terminalRoomUpperID;
  private final int numHubs;

  // Gerador de números aleatórios
  private final Random randomGenerator;

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
    this.randomGenerator = benchmarkModule.getRandomGenerator();

    LOG.debug(
        "Initialized worker {} for hub {} (rooms {} to {})",
        id,
        terminalHubID,
        terminalRoomLowerID,
        terminalRoomUpperID);
  }

  @Override
  protected TransactionStatus executeWork(Connection conn, TransactionType nextTransaction)
      throws UserAbortException, SQLException {
    try {
      // Obtém a instância do procedimento
      BenchIOProcedure proc =
          (BenchIOProcedure) this.getProcedure(nextTransaction.getProcedureClass());

      // Executa o procedimento com os parâmetros adequados
      return proc.run(
          conn,
          randomGenerator,
          terminalHubID,
          numHubs,
          terminalRoomLowerID,
          terminalRoomUpperID,
          this);

    } catch (ClassCastException ex) {
      LOG.error("Invalid transaction type: {} - {}", nextTransaction, ex.getMessage());
      throw new UserAbortException("Invalid transaction type: " + nextTransaction);
    } catch (SQLException ex) {
      LOG.error("SQL error executing transaction {}: {}", nextTransaction, ex.getMessage());
      throw ex;
    }
  }

  @Override
  protected long getPreExecutionWaitInMillis(TransactionType type) {
    // Pode ser personalizado por tipo de transação se necessário
    return type.getPreExecutionWait();
  }

  @Override
  protected long getPostExecutionWaitInMillis(TransactionType type) {
    // Implementação de think time com distribuição exponencial
    long mean = type.getPostExecutionWait();
    if (mean <= 0) return 0;

    float u = this.randomGenerator.nextFloat();
    long thinkTime = (long) (-mean * Math.log(1 - u));

    // Limita o think time para evitar valores extremamente altos
    return Math.min(thinkTime, mean * 10);
  }

  // Métodos auxiliares para acesso aos parâmetros de partição
  public int getTerminalHubID() {
    return terminalHubID;
  }

  public int getTerminalRoomLowerID() {
    return terminalRoomLowerID;
  }

  public int getTerminalRoomUpperID() {
    return terminalRoomUpperID;
  }
}
