package com.oltpbenchmark.benchmarks.iotbench.procedures;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import static com.oltpbenchmark.benchmarks.iotbench.iotBenchConstants.TABLE_NAME;

public class ReadRecord extends Procedure {
  // SQL Statement para ler os campos FIELD1, FIELD2, e FIELD3
  public final SQLStmt readStmt =
      new SQLStmt("SELECT field1, field2, field3 FROM " + TABLE_NAME + " WHERE iotBench_KEY=?");

  // Método para executar a leitura dos dados
  public void run(Connection conn, int keyname, double[] results) throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, readStmt)) {
      stmt.setInt(1, keyname);

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          results[0] = rs.getDouble("field1");
          results[1] = rs.getDouble("field2");
          results[2] = rs.getDouble("field3");
        }
      }
    }
  }
}
