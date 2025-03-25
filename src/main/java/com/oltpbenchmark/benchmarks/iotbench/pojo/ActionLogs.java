package com.oltpbenchmark.benchmarks.iotbench.pojo;
import java.sql.Timestamp;
public class ActionLogs {
  private int logId;
  private int userId;
  private int deviceId;
  private String action;
  private String status;
  private Timestamp date;

  // Getters e Setters
  public int getLogId() {
      return logId;
  }

  public void setLogId(int logId) {
      this.logId = logId;
  }

  public int getUserId() {
      return userId;
  }

  public void setUserId(int userId) {
      this.userId = userId;
  }

  public int getDeviceId() {
      return deviceId;
  }

  public void setDeviceId(int deviceId) {
      this.deviceId = deviceId;
  }

  public String getAction() {
      return action;
  }

  public void setAction(String action) {
      this.action = action;
  }

  public String getStatus() {
      return status;
  }

  public void setStatus(String status) {
      this.status = status;
  }

  public Timestamp getDate() {
      return date;
  }

  public void setDate(java.sql.Timestamp date) {
      this.date = date;
  }
}