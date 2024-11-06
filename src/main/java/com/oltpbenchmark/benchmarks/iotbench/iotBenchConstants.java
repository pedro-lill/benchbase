package com.oltpbenchmark.benchmarks.iotbench;

public abstract class IotBenchConstants {

  // Table names
  public static final String TABLENAME_USERTABLE = "USERTABLE";
  public static final String TABLENAME_HUB = "HUB";
  public static final String TABLENAME_ROOM = "ROOM";
  public static final String TABLENAME_DEVICE = "DEVICE";
  public static final String TABLENAME_SENSOR = "SENSOR";
  public static final String TABLENAME_SENSOR_LOG = "SENSORLOG";
  public static final String TABLENAME_AUTOMATION_PROFILE = "AUTOMATIONPROFILE";
  public static final String TABLENAME_ACTION_LOGS = "ACTIONLOGS";
  public static final String TABLE_NAME = "AA";

  public static final int NUM_USERS = 1000;
  public static final int NUM_HUBS = 100;
  public static final int NUM_ROOMS = 500;
  public static final int NUM_DEVICES = 2000;
  public static final int NUM_SENSORS = 5000;

  // Record count baseline
  public static final int RECORD_COUNT = 1000;

  // Number of fields in the user table
  public static final int NUM_FIELDS = 4;

  // Maximum size for each field in the USERTABLE
  public static final int MAX_FIELD_SIZE = 100; // chars

  // Number of records to be loaded by each thread
  public static final int THREAD_BATCH_SIZE = 50000;

  // Max number of scan operations
  public static final int MAX_SCAN = 1000;

  // Limits for data retrieval in different tables
  public static int LIMIT_USERS = 1000;
  public static int LIMIT_HUBS = 1000;
  public static int LIMIT_DEVICES = 2000;
  public static int LIMIT_SENSORS = 5000;

  // Other relevant constants (if needed)
  public static final int MAX_NAME_LENGTH = 255;
  public static final int MAX_STATUS_LENGTH = 50;
}
