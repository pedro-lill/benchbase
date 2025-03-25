package com.oltpbenchmark.benchmarks.iotbench.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.iotbench.IotBenchConstants;
import com.oltpbenchmark.util.SQLUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertUserRecord extends Procedure {

  private static final Logger LOG = LoggerFactory.getLogger(InsertUserRecord.class);

  public final SQLStmt insertStmt =
      new SQLStmt(
          "INSERT INTO "
              + IotBenchConstants.TABLENAME_USERTABLE
              + " (userId, nameIot, email, password_hash, usertype) VALUES (?, ?, ?, ?, ?)");

  public void run(
      Connection conn, int userId, String nameIot, String email, String passwordHash, int userType)
      throws SQLException {

    try (PreparedStatement stmt = this.getPreparedStatement(conn, this.insertStmt)) {
      stmt.setInt(1, userId);
      stmt.setString(2, nameIot);
      stmt.setString(3, email);
      stmt.setString(4, passwordHash);
      stmt.setInt(5, userType);

      stmt.executeUpdate();
    } catch (SQLException e) {
      if (SQLUtil.isDuplicateKeyException(e)) {
        LOG.debug("Registro duplicado para userId: " + userId, e);
        throw new SQLException("O usuário com userId " + userId + " já existe.", e);
      } else {
        LOG.error("Erro ao inserir registro do usuário: ", e);
        throw e;
      }
    }
  }
}
