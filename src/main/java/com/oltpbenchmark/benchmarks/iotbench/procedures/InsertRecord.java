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
import java.sql.SQLException;

public class InsertRecord extends Procedure {
  public final SQLStmt insertStmt =
      new SQLStmt("INSERT INTO " + TABLE_NAME + " (FIELD1, FIELD2, FIELD3) VALUES (?, ?, ?)");

  public void run(Connection conn, double FIELD1, double FIELD2, double FIELD3)
      throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, this.insertStmt)) {
      stmt.setDouble(1, FIELD1);
      stmt.setDouble(2, FIELD2);
      stmt.setDouble(3, FIELD3);
      stmt.executeUpdate();
    } catch (SQLException e) {
      System.err.println("Error inserting record: " + e.getMessage());
      throw e; // Re-throwing the exception after logging it
    }
  }
}
