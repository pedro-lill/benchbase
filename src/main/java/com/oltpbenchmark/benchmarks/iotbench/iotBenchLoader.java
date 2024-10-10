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

import com.oltpbenchmark.api.Loader;
import com.oltpbenchmark.api.LoaderThread;
import com.oltpbenchmark.catalog.Table;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

class iotBenchLoader extends Loader<iotBenchBenchmark> {
  private final int num_record;

  public iotBenchLoader(iotBenchBenchmark benchmark) {
    super(benchmark);
    this.num_record = (int) Math.round(iotBenchConstants.RECORD_COUNT * this.scaleFactor);
    if (LOG.isDebugEnabled()) {
      LOG.debug("# of RECORDS: {}", this.num_record);
    }
  }

  @Override
  public List<LoaderThread> createLoaderThreads() {
    List<LoaderThread> threads = new ArrayList<>();
    int count = 0;
    while (count < this.num_record) {
      final int start = count;
      final int stop = Math.min(start + iotBenchConstants.THREAD_BATCH_SIZE, this.num_record);
      threads.add(
          new LoaderThread(this.benchmark) {
            @Override
            public void load(Connection conn) throws SQLException {
              if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("IotBenchLoadThread[%d, %d]", start, stop));
              }
              loadRecords(conn, start, stop);
            }
          });
      count = stop;
    }
    return threads;
  }

  private void loadRecords(Connection conn, int start, int stop) throws SQLException {
    Table catalog_tbl = benchmark.getCatalog().getTable("usertable");

    String sql = "INSERT INTO usertable (iotBench_key, FIELD1, FIELD2, FIELD3) VALUES (?, ?, ?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      int batch = 0;
      for (int i = start; i < stop; i++) {
        int iotBenchKey = start + i; // Isso garante que a chave será única
        stmt.setInt(1, iotBenchKey); // A coluna IOTBENCH_KEY
        stmt.setDouble(2, Math.random() * 100); // FIELD1
        stmt.setDouble(3, Math.random() * 100); // FIELD2
        stmt.setDouble(4, Math.random() * 100); // FIELD3
        stmt.addBatch();

        if (++batch >= workConf.getBatchSize()) {
          stmt.executeBatch();
          batch = 0;
        }
      }
      if (batch > 0) {
        stmt.executeBatch();
      }
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("Finished loading {}", catalog_tbl.getName());
    }
  }
}
