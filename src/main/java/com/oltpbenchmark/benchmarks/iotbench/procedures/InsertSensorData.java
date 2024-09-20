package com.oltpbenchmark.benchmarks.iotbench.procedures;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import com.oltpbenchmark.api.Procedure;

public class InsertSensorData extends Procedure {

    private Random random = new Random();

    public void run(Connection conn) throws SQLException {
        long timestamp = System.currentTimeMillis();
        double temperature = generateSeriesData(random, SeriesType.values()[random.nextInt(SeriesType.values().length)], timestamp);
        double humidity = generateSeriesData(random, SeriesType.values()[random.nextInt(SeriesType.values().length)], timestamp);

        String insertSQL = "INSERT INTO sensor_data (timestamp, temperature, humidity) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
            stmt.setLong(1, timestamp);
            stmt.setDouble(2, temperature);
            stmt.setDouble(3, humidity);
            stmt.executeUpdate();
        }
    }

    private enum SeriesType {
        STEP, SINE, LINEAR, RANDOM, CONSTANT
    }

    private double generateSeriesData(Random random, SeriesType seriesType, long timestamp) {
        switch (seriesType) {
            case STEP:
                return 20 + (timestamp / 1000 % 2 == 0 ? 20 : 40) + random.nextDouble(-5, 5);
            case SINE:
                return 40 + 10 * Math.sin(0.1 * timestamp) + random.nextDouble(-2, 2);
            case LINEAR:
                return 20 + 5 * (timestamp / 1000) % 60 + random.nextDouble(-3, 3);
            case RANDOM:
                return random.nextDouble(20, 50);
            case CONSTANT:
                return 35 + random.nextDouble(-1, 1);
            default:
                return 0;
        }
    }
}
