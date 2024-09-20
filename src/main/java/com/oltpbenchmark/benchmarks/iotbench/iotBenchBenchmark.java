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

public final class iotBenchBenchmark extends BenchmarkModule {

  private static final Logger LOG = LoggerFactory.getLogger(iotBenchBenchmark.class);

  /** The length in characters of each field */
  protected final int fieldSize;

  /** The constant used in the zipfian distribution (to modify the skew) */
  protected final double skewFactor;

  // variaveis usadas para gerar dados de acordo com a distribuição Zipifian

  public iotBenchBenchmark(WorkloadConfiguration workConf) {
    super(workConf);

    int fieldSize = iotBenchConstants.MAX_FIELD_SIZE;
    if (workConf.getXmlConfig() != null && workConf.getXmlConfig().containsKey("fieldSize")) {
      fieldSize =
          Math.min(workConf.getXmlConfig().getInt("fieldSize"), iotBenchConstants.MAX_FIELD_SIZE);
    }
    this.fieldSize = fieldSize;
    if (this.fieldSize <= 0) {
      throw new RuntimeException("Invalid iotBench fieldSize '" + this.fieldSize + "'");
    }

    double skewFactor = 0.99;
    if (workConf.getXmlConfig() != null && workConf.getXmlConfig().containsKey("skewFactor")) {
      skewFactor = workConf.getXmlConfig().getDouble("skewFactor");
      if (skewFactor <= 0 || skewFactor >= 1) {
        throw new RuntimeException("Invalid iotBench skewFactor '" + skewFactor + "'");
      }
    }
    this.skewFactor = skewFactor;
  }

  @Override
  protected List<Worker<? extends BenchmarkModule>> makeWorkersImpl() {
    List<Worker<? extends BenchmarkModule>> workers = new ArrayList<>();
    try {
      // LOADING FROM THE DATABASE IMPORTANT INFORMATION
      // LIST OF USERS
      Table t = this.getCatalog().getTable("USERTABLE");
      String userCount = SQLUtil.getMaxColSQL(this.workConf.getDatabaseType(), t, "iotBench_key");

      try (Connection metaConn = this.makeConnection();
          Statement stmt = metaConn.createStatement();
          ResultSet res = stmt.executeQuery(userCount)) {
        int init_record_count = 0;
        while (res.next()) {
          init_record_count = res.getInt(1);
        }

        for (int i = 0; i < workConf.getTerminals(); ++i) {
          workers.add(new iotBenchWorker(this, i, init_record_count + 1));
        }
      }
    } catch (SQLException e) {
      LOG.error(e.getMessage(), e);
    }
    return workers;
  }

  @Override
  protected Loader<iotBenchBenchmark> makeLoaderImpl() {
    return new iotBenchLoader(this);
  }

  @Override
  protected Package getProcedurePackageImpl() {
    return InsertRecord.class.getPackage();
  }
}
