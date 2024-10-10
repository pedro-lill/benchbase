package com.oltpbenchmark.benchmarks.iotbench.procedures;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import static com.oltpbenchmark.benchmarks.iotbench.iotBenchConstants.TABLE_NAME;

public class InsertRecord extends Procedure {
  // SQL Statement para inserir registros na tabela usertable
  public final SQLStmt insertStmt =
      new SQLStmt("INSERT INTO " + TABLE_NAME + " VALUES (?, ?, ?, ?)");

  public void run(Connection conn, int keyname, double field1, double field2, double field3)
      throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, this.insertStmt)) {
      stmt.setInt(1, keyname); // iotBench_KEY
      // depois posso inserir os dados de formas diferentes aqui
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
