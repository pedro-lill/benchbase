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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import static com.oltpbenchmark.benchmarks.iotbench.iotBenchConstants.TABLE_NAME;

public class ReadModifyWriteRecord extends Procedure {
  // SQL Statement para selecionar o registro
  public final SQLStmt selectStmt =
      new SQLStmt("SELECT * FROM " + TABLE_NAME + " WHERE iotBench_KEY=? FOR UPDATE");

  // SQL Statement para atualizar o registro
  public final SQLStmt updateAllStmt =
      new SQLStmt(
          "UPDATE " + TABLE_NAME + " SET field1 = ?, field2 = ?, field3 = ? WHERE iotBench_KEY=?");

  public void run(
      Connection conn,
      int keyname,
      double newFIELD1,
      double newFIELD2,
      double newFIELD3,
      double[] results)
      throws SQLException {

    if (conn == null) {
      throw new SQLException("Connection cannot be null");
    }

    // Fetch the current values
    try (PreparedStatement stmt = this.getPreparedStatement(conn, selectStmt)) {
      stmt.setInt(1, keyname);
      try (ResultSet r = stmt.executeQuery()) {
        if (r.next()) {
          results[0] = r.getDouble("field1");
          results[1] = r.getDouble("field2");
          results[2] = r.getDouble("field3");
        } else {
          throw new SQLException("No record found with the provided iotBench_KEY: " + keyname);
        }
      }
    }

    // Update the record with new values
    try (PreparedStatement stmt = this.getPreparedStatement(conn, updateAllStmt)) {
      stmt.setInt(4, keyname);
      stmt.setDouble(1, newFIELD1);
      stmt.setDouble(2, newFIELD2);
      stmt.setDouble(3, newFIELD3);
      stmt.executeUpdate();
    }
  }
}
