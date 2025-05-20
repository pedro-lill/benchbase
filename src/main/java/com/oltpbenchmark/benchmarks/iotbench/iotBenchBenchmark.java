/*
 * Copyright 2020 by OLTPBenchmark Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oltpbenchmark.benchmarks.iotbench;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oltpbenchmark.WorkloadConfiguration;
import com.oltpbenchmark.api.BenchmarkModule;
import com.oltpbenchmark.api.Loader;
import com.oltpbenchmark.api.Worker;

public final class IotBenchBenchmark extends BenchmarkModule {

  private static final Logger LOG = LoggerFactory.getLogger(IotBenchBenchmark.class);

  protected final int fieldSize;
  protected final double skewFactor;

  // Valores que serão populados após o Loader e usados pelos Workers
  int numUsers;
  int numSensors;
  int numRooms;
  int numDevices;
  int numAutomationProfiles;
  int numSensorLogs;
  int numActionLogs;
  int numHubs;

  public IotBenchBenchmark(WorkloadConfiguration workConf) {
    super(workConf);

    int fieldSize = IotBenchConstants.MAX_FIELD_SIZE;
    if (workConf.getXmlConfig() != null && workConf.getXmlConfig().containsKey("fieldSize")) {
      fieldSize =
          Math.min(workConf.getXmlConfig().getInt("fieldSize"), IotBenchConstants.MAX_FIELD_SIZE);
    }
    this.fieldSize = fieldSize;
    if (this.fieldSize <= 0) {
      throw new RuntimeException("Invalid IotBench fieldSize '" + this.fieldSize + "'");
    }

    double skewFactor = 0.99;
    if (workConf.getXmlConfig() != null && workConf.getXmlConfig().containsKey("skewFactor")) {
      skewFactor = workConf.getXmlConfig().getDouble("skewFactor");
      if (skewFactor <= 0 || skewFactor >= 1) {
        throw new RuntimeException("Invalid IotBench skewFactor '" + skewFactor + "'");
      }
    }
    this.skewFactor = skewFactor;
  }

  @Override
  protected List<Worker<? extends BenchmarkModule>> makeWorkersImpl() {
    List<Worker<? extends BenchmarkModule>> workers = new ArrayList<>();

    LOG.info("Fetching record counts after loading...");
    try (Connection conn = this.makeConnection();
        Statement stmt = conn.createStatement()) {
      ResultSet rs;

      rs = stmt.executeQuery("SELECT COUNT(*) FROM " + IotBenchConstants.TABLENAME_USERTABLE);
      if (rs.next()) this.numUsers = rs.getInt(1);
      else this.numUsers = 0;
      LOG.info("Number of users: {}", this.numUsers);
      rs.close();

      rs = stmt.executeQuery("SELECT COUNT(*) FROM " + IotBenchConstants.TABLENAME_SENSOR);
      if (rs.next()) this.numSensors = rs.getInt(1);
      else this.numSensors = 0;
      LOG.info("Number of sensors: {}", this.numSensors);
      rs.close();

      rs = stmt.executeQuery("SELECT COUNT(*) FROM " + IotBenchConstants.TABLENAME_ROOM);
      if (rs.next()) this.numRooms = rs.getInt(1);
      else this.numRooms = 0;
      LOG.info("Number of rooms: {}", this.numRooms);
      rs.close();

      rs = stmt.executeQuery("SELECT COUNT(*) FROM " + IotBenchConstants.TABLENAME_DEVICE);
      if (rs.next()) this.numDevices = rs.getInt(1);
      else this.numDevices = 0;
      LOG.info("Number of devices: {}", this.numDevices);
      rs.close();

      rs =
          stmt.executeQuery(
              "SELECT COUNT(*) FROM " + IotBenchConstants.TABLENAME_AUTOMATION_PROFILE);
      if (rs.next()) this.numAutomationProfiles = rs.getInt(1);
      else this.numAutomationProfiles = 0;
      LOG.info("Number of automation profiles: {}", this.numAutomationProfiles);
      rs.close();

    } catch (SQLException e) {
      LOG.error("Error fetching record counts after load: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to fetch record counts after loading", e);
    }

    for (int i = 0; i < workConf.getTerminals(); ++i) {

      workers.add(new IotBenchWorker(this, i, 0));
    }
    LOG.info("Workers initialized: " + workConf.getTerminals());
    return workers;
  }

  @Override
  protected Loader<IotBenchBenchmark> makeLoaderImpl() {
    return new IotBenchLoader(this);
  }

  @Override
  protected Package getProcedurePackageImpl() {
    return com.oltpbenchmark.benchmarks.iotbench.procedures.SensorReading.class.getPackage();
  }
}
