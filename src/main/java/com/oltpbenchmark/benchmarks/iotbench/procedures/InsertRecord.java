package com.oltpbenchmark.benchmarks.iotbench.procedures;

import static com.oltpbenchmark.benchmarks.iotbench.IotBenchConstants.TABLENAME_USERTABLE;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertRecord extends Procedure {
  // SQL Statement para inserir registros na tabela usertable
  public final SQLStmt insertStmt =
      new SQLStmt("INSERT INTO " + TABLENAME_USERTABLE + " VALUES (?, ?, ?, ?)");

  public void run(Connection conn, int keyname, double field1, double field2, double field3)
      throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, this.insertStmt)) {
      stmt.setInt(1, keyname); // IotBench_KEY
      // to do
      stmt.setDouble(2, field1); // FIELD1
      stmt.setDouble(3, field2); // FIELD2
      stmt.setDouble(4, field3); // FIELD3
      stmt.executeUpdate();
    } catch (SQLException e) {
      System.err.println("Error inserting record: " + e.getMessage());
      throw e; // Re-throwing the exception after logging it
    }
  }
}
