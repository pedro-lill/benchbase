package com.oltpbenchmark.benchmarks.iotbench.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.iotbench.IotBenchConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertUserRecord extends Procedure {
  public final SQLStmt insertStmt =
      new SQLStmt(
          "INSERT INTO "
              + IotBenchConstants.TABLENAME_USERTABLE
              + " (userId, name, email, password_hash, user_type) VALUES (?, ?, ?, ?, ?)");

  public void run(
      Connection conn, int userId, String name, String email, String passwordHash, int userType)
      throws SQLException {
    try (PreparedStatement stmt = this.getPreparedStatement(conn, this.insertStmt)) {
      stmt.setInt(1, userId);
      stmt.setString(2, name);
      stmt.setString(3, email);
      stmt.setString(4, passwordHash);
      stmt.setInt(5, userType);
      stmt.executeUpdate();
    } catch (SQLException e) {
      System.err.println("Error inserting user record: " + e.getMessage());
      throw e;
    }
  }
}
