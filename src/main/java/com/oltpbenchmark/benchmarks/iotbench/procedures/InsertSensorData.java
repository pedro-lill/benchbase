package com.oltpbenchmark.benchmarks.iotbench.procedures;

import com.oltpbenchmark.api.Procedure;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class InsertSensorData extends Procedure {

  private Random random = new Random();

  public void run(Connection conn) throws SQLException {
    long FIELD1 = System.currentTimeMillis();
    double FIELD2 =
        generateSeriesData(
            random, SeriesType.values()[random.nextInt(SeriesType.values().length)], FIELD1);
    double FIELD3 =
        generateSeriesData(
            random, SeriesType.values()[random.nextInt(SeriesType.values().length)], FIELD1);

    String insertSQL = "INSERT INTO USERTABLE (FIELD1, FIELD2, FIELD3) VALUES (?, ?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
      stmt.setDouble(1, FIELD1);
      stmt.setDouble(2, FIELD2);
      stmt.setDouble(3, FIELD3);
      stmt.executeUpdate();
    }
  }

  private enum SeriesType {
    STEP,
    SINE,
    LINEAR,
    RANDOM,
    CONSTANT
  }

  private double generateSeriesData(Random random, SeriesType seriesType, long FIELD1) {
    switch (seriesType) {
      case STEP:
        return 20 + (FIELD1 / 1000 % 2 == 0 ? 20 : 40) + random.nextDouble(-5, 5);
      case SINE:
        return 40 + 10 * Math.sin(0.1 * FIELD1) + random.nextDouble(-2, 2);
      case LINEAR:
        return 20 + 5 * (FIELD1 / 1000) % 60 + random.nextDouble(-3, 3);
      case RANDOM:
        return random.nextDouble(20, 50);
      case CONSTANT:
        return 35 + random.nextDouble(-1, 1);
      default:
        return 0;
    }
  }
}
