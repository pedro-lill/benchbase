package com.oltpbenchmark.benchmarks.iotbench.pojo;

public class Device {
  private int deviceId;
  private String name;
  private String status;
  private int deviceType;
  private int roomId;
  private int hubId;

  // Getters e Setters
  public int getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(int deviceId) {
    this.deviceId = deviceId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public int getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(int deviceType) {
    this.deviceType = deviceType;
  }

  public int getRoomId() {
    return roomId;
  }

  public void setRoomId(int roomId) {
    this.roomId = roomId;
  }

  public int getHubId() {
    return hubId;
  }

  public void setHubId(int hubId) {
    this.hubId = hubId;
  }
}
