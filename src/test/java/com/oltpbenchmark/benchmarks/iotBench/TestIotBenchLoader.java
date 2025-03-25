/******************************************************************************
 *  Copyright 2015 by OLTPBenchmark Project                                   *
 *                                                                            *
 *  Licensed under the Apache License, Version 2.0 (the "License");           *
 *  you may not use this file except in compliance with the License.          *
 *  You may obtain a copy of the License at                                   *
 *                                                                            *
 *    http://www.apache.org/licenses/LICENSE-2.0                              *
 *                                                                            *
 *  Unless required by applicable law or agreed to in writing, software       *
 *  distributed under the License is distributed on an "AS IS" BASIS,         *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 *  See the License for the specific language governing permissions and       *
 *  limitations under the License.                                            *
 ******************************************************************************/

package com.oltpbenchmark.benchmarks.iotbench;

import com.oltpbenchmark.api.AbstractTestLoader;
import com.oltpbenchmark.api.Procedure;
import java.util.List;

public class TestIotBenchLoader extends AbstractTestLoader<IotBenchBenchmark> {

  private static final String[] IGNORE = {
    IotBenchConstants.TABLENAME_USERTABLE,
    IotBenchConstants.TABLENAME_HUB,
    IotBenchConstants.TABLENAME_HUB,
    IotBenchConstants.TABLENAME_DEVICE,
    IotBenchConstants.TABLENAME_SENSOR,
    IotBenchConstants.TABLENAME_SENSOR_LOG,
    IotBenchConstants.TABLENAME_AUTOMATION_PROFILE,
    IotBenchConstants.TABLENAME_ACTION_LOGS
  };

  @Override
  public List<Class<? extends Procedure>> procedures() {
    return TestIotBenchBenchmark.PROCEDURE_CLASSES;
  }

  @Override
  public Class<IotBenchBenchmark> benchmarkClass() {
    return IotBenchBenchmark.class;
  }

  @Override
  public List<String> ignorableTables() {
    return List.of(IGNORE);
  }
}
