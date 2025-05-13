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
 *
 */

package com.oltpbenchmark.benchmarks.iotbench;

import com.oltpbenchmark.WorkloadConfiguration;
import com.oltpbenchmark.api.BenchmarkModule;
import com.oltpbenchmark.api.Loader;
import com.oltpbenchmark.api.Worker;
import com.oltpbenchmark.benchmarks.iotbench.procedures.InsertRecord;
import com.oltpbenchmark.catalog.Table;
import com.oltpbenchmark.util.SQLUtil;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IotBenchBenchmark extends BenchmarkModule {

  private static final Logger LOG = LoggerFactory.getLogger(IotBenchBenchmark.class);

  protected final int fieldSize;

  protected final double skewFactor;

  int numUsers;
  int numSensors;
  int numSensorLogs;
  int numActionLogs;
  int numRooms;
  int numHubs;
  int numDevices;

  public IotBenchBenchmark(WorkloadConfiguration workConf) {
    super(workConf);

    int fieldSize = IotBenchConstants.MAX_FIELD_SIZE;
    if (workConf.getXmlConfig() != null && workConf.getXmlConfig().containsKey("fieldSize")) {
      fieldSize =
          Math.min(workConf.getXmlConfig().getInt("fieldSize"), IotBenchConstants.MAX_FIELD_SIZE);
    }
    this.fieldSize = fieldSize;
    if (this.fieldSize <= 0) {
      throw new RuntimeException("Invalid IotBench fieldSize '" + this.fieldSize + "'");
    }

    double skewFactor = 0.99;
    if (workConf.getXmlConfig() != null && workConf.getXmlConfig().containsKey("skewFactor")) {
      skewFactor = workConf.getXmlConfig().getDouble("skewFactor");
      if (skewFactor <= 0 || skewFactor >= 1) {
        throw new RuntimeException("Invalid IotBench skewFactor '" + skewFactor + "'");
      }
    }
    this.skewFactor = skewFactor;
  }

  @Override
  protected List<Worker<? extends BenchmarkModule>> makeWorkersImpl() {
    List<Worker<? extends BenchmarkModule>> workers = new ArrayList<>();
    try {
      Table t = this.getCatalog().getTable(IotBenchConstants.TABLENAME_USERTABLE);
      String userCount = SQLUtil.getMaxColSQL(this.workConf.getDatabaseType(), t, "user_id");

      try (Connection metaConn = this.makeConnection();
          Statement stmt = metaConn.createStatement();
          ResultSet res = stmt.executeQuery(userCount)) {
        int initRecordCount = 0;
        if (res.next()) {
          initRecordCount = res.getInt(1);
        }

        for (int i = 0; i < workConf.getTerminals(); ++i) {
          workers.add(new IotBenchWorker(this, i, initRecordCount + 1));
        }

        LOG.info("Workers inicializados: " + workConf.getTerminals());
      }
    } catch (SQLException e) {
      LOG.error("Erro ao inicializar workers: " + e.getMessage(), e);
    }
    return workers;
  }

  @Override
  protected Loader<IotBenchBenchmark> makeLoaderImpl() {
    return new IotBenchLoader(this);
  }

  @Override
  protected Package getProcedurePackageImpl() {
    return InsertRecord.class.getPackage();
  }
}
