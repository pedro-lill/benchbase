package com.oltpbenchmark.benchmarks.benchio;

import com.oltpbenchmark.WorkloadConfiguration;
import com.oltpbenchmark.api.BenchmarkModule;
import com.oltpbenchmark.api.Loader;
import com.oltpbenchmark.api.Worker;
import com.oltpbenchmark.benchmarks.benchio.procedures.SensorReadingProcedure;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BenchIOBenchmark extends BenchmarkModule {
  private static final Logger LOG = LoggerFactory.getLogger(BenchIOBenchmark.class);

  public BenchIOBenchmark(WorkloadConfiguration workConf) {
    super(workConf);
  }

  @Override
  protected Package getProcedurePackageImpl() {
    return SensorReadingProcedure.class.getPackage();
  }

  @Override
  protected List<Worker<? extends BenchmarkModule>> makeWorkersImpl() {
    List<Worker<? extends BenchmarkModule>> workers = new ArrayList<>();
    int numWorkers = workConf.getTerminals();

    try {
      for (int i = 0; i < numWorkers; i++) {
        int hubId = (i % BenchIOConfig.configHubCount) + 1;
        workers.add(
            new BenchIOWorker( // Corrigido para usar BenchIOWorker
                this,
                i, // workerId
                hubId, // terminalHubID
                1, // terminalRoomLowerID
                BenchIOConfig.configRoomsPerHub, // terminalRoomUpperID
                BenchIOConfig.configHubCount // numHubs
                ));
        LOG.info("Worker {} atribuído ao Hub {}", i, hubId);
      }
    } catch (Exception e) {
      LOG.error("Erro ao criar workers: " + e.getMessage(), e);
    }

    return workers;
  }

  @Override
  protected Loader<BenchIOBenchmark> makeLoaderImpl() {
    return new BenchIOLoader(this);
  }

  protected void configureWorkers() {
    int numHubs = BenchIOConfig.configHubCount;
    int numRooms = BenchIOConfig.configRoomsPerHub;

    LOG.info(
        "Configurando {} workers para {} hubs e {} salas por hub",
        workConf.getTerminals(),
        numHubs,
        numRooms);
  }

  // Adicionando método getRandomGenerator() que estava faltando
  public Random getRandomGenerator() {
    return this.rng();
  }
}
