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
              + " (user_id, name_iot, email, password_hash, user_type) VALUES (?, ?, ?, ?, ?)");

  public void run(
      Connection conn,
      int user_id,
      String name_iot,
      String email,
      String password_hash,
      int user_type)
      throws SQLException {

    try (PreparedStatement stmt = this.getPreparedStatement(conn, this.insertStmt)) {
      stmt.setInt(1, user_id);
      stmt.setString(2, name_iot);
      stmt.setString(3, email);
      stmt.setString(4, password_hash);
      stmt.setInt(5, user_type);

      stmt.executeUpdate();
    } catch (SQLException e) {
      if (SQLUtil.isDuplicateKeyException(e)) {
        LOG.debug("Registro duplicado para user_id: " + user_id, e);
        throw new SQLException("O usuário com user_id " + user_id + " já existe.", e);
      } else {
        LOG.error("Erro ao inserir registro do usuário: ", e);
        throw e;
      }
    }
  }
}
