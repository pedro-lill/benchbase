package com.oltpbenchmark.benchmarks.benchio;

import com.oltpbenchmark.api.AbstractTestWorker;
import com.oltpbenchmark.api.Procedure;
import java.util.List;

public class TestBenchIOWorker extends AbstractTestWorker<BenchIOBenchmark> {

  @Override
  public List<Class<? extends Procedure>> procedures() {
    return TestBenchIOBenchmark.PROCEDURE_CLASSES;
  }

  @Override
  public Class<BenchIOBenchmark> benchmarkClass() {
    return BenchIOBenchmark.class;
  }
}
