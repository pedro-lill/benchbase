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

 import com.oltpbenchmark.api.Procedure;
 import com.oltpbenchmark.api.SQLStmt;
 import com.oltpbenchmark.benchmarks.iotbench.IotBenchConstants;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;

 public class GetUsers extends Procedure {

   public final SQLStmt getUsers =
       new SQLStmt(
           "SELECT * FROM "
               + IotBenchConstants.TABLENAME_USERTABLE + ";"
               );

   public void run(Connection conn, long uid) throws SQLException {
     try (PreparedStatement stmt = this.getPreparedStatement(conn, getTweets)) {
       stmt.setLong(1, uid);
       try (ResultSet rs = stmt.executeQuery()) {
         assert rs != null;
       }
     }
   }
 }
