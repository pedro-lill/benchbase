package com.oltpbenchmark.benchmarks.iotbench;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oltpbenchmark.WorkloadConfiguration;
import com.oltpbenchmark.api.BenchmarkModule;
import com.oltpbenchmark.api.Loader;
import com.oltpbenchmark.api.Worker;

public final class IotBenchBenchmark extends BenchmarkModule {
    private static final Logger LOG = LoggerFactory.getLogger(IotBenchBenchmark.class);

    public IotBenchBenchmark(WorkloadConfiguration workConf) {
        super(workConf);
    }

    @Override
    protected Package getProcedurePackageImpl() {
        return this.getClass().getPackage(); // Corrigido para referenciar o próprio pacote do benchmark
    }

    @Override
    protected List<Worker<? extends BenchmarkModule>> makeWorkersImpl() {
        List<Worker<? extends BenchmarkModule>> workers = new ArrayList<>();

        try {
            workers.addAll(createTerminals());
        } catch (Exception e) {
            LOG.error("Erro ao criar terminais: " + e.getMessage(), e);
        }

        return workers;
    }

    @Override
    protected Loader<IotBenchBenchmark> makeLoaderImpl() {
        return new IotBenchLoader(this);
    }

    protected List<IotBenchWorker> createTerminals() throws SQLException {
        List<IotBenchWorker> terminals = new ArrayList<>();

        int numHubs = IotBenchConfig.configHubCount;
        int numRooms = IotBenchConfig.configRoomsPerHub * numHubs;
        int numDevices = IotBenchConfig.configDevicesPerRoom * numRooms;
        int numSensors = IotBenchConfig.configSensorsPerDevice * numDevices;

        int numTerminals = workConf.getTerminals();
        final double terminalsPerHub = (double) numTerminals / numHubs;
        int workerId = 0;

        for (int h = 0; h < numHubs; h++) {
            int lowerTerminalId = (int) (h * terminalsPerHub);
            int upperTerminalId = (int) ((h + 1) * terminalsPerHub);
            int hubId = h + 1;
            if (hubId == numHubs) {
                upperTerminalId = numTerminals;
            }
            int numHubTerminals = upperTerminalId - lowerTerminalId;

            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    String.format(
                        "hub_id %d = %d terminais [lower=%d / upper=%d]",
                        hubId, numHubTerminals, lowerTerminalId, upperTerminalId));
            }

            for (int terminalId = 0; terminalId < numHubTerminals; terminalId++) {
                IotBenchWorker terminal =
                    new IotBenchWorker(this, workerId++, hubId, numRooms, numDevices, numSensors);
                terminals.add(terminal); // Adicionando diretamente na lista
            }
        }

        return terminals;
    }
}
