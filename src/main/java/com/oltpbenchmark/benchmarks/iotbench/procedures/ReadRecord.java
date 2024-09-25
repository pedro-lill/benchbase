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

package com.oltpbenchmark.benchmarks.iotbench.procedures;

import static com.oltpbenchmark.benchmarks.iotbench.iotBenchConstants.TABLE_NAME;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReadRecord extends Procedure {
  public final SQLStmt readStmt =
      new SQLStmt("SELECT FIELD1, FIELD2, FIELD3 FROM " + TABLE_NAME + " WHERE iotBench_KEY=?");

  public void run(Connection conn, long id, Object[] results) throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, readStmt)) {
      stmt.setLong(1, id);
      try (ResultSet r = stmt.executeQuery()) {
        if (r.next()) {
          results[0] = r.getDouble("FIELD1");
          results[1] = r.getDouble("FIELD2");
          results[2] = r.getDouble("FIELD3");
        }
      }
    }
  }
}
