package com.oltpbenchmark.benchmarks.iotBench;

import com.oltpbenchmark.api.AbstractTestWorker;
import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.benchmarks.iotbench.iotBenchBenchmark;
import java.util.List;

public class TestIotBenchWorker extends AbstractTestWorker<iotBenchBenchmark> {

  @Override
  public List<Class<? extends Procedure>> procedures() {
    return TestIotBenchBenchmark.PROCEDURE_CLASSES;
  }

  @Override
  public Class<iotBenchBenchmark> benchmarkClass() {
    return iotBenchBenchmark.class;
  }
}
