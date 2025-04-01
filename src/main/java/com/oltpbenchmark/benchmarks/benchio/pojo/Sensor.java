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

package com.oltpbenchmark.benchmarks.benchio.pojo;

public class Sensor {
  private int sensorId;
  private String name;
  private int type;
  private double value;
  private int deviceId;

  // Getters e Setters
  public int getSensorId() {
      return sensorId;
  }

  public void setSensorId(int sensorId) {
      this.sensorId = sensorId;
  }

  public String getName() {
      return name;
  }

  public void setName(String name) {
      this.name = name;
  }

  public int getType() {
      return type;
  }

  public void setType(int type) {
      this.type = type;
  }

  public double getValue() {
      return value;
  }

  public void setValue(double value) {
      this.value = value;
  }

  public int getDeviceId() {
      return deviceId;
  }

  public void setDeviceId(int deviceId) {
      this.deviceId = deviceId;
  }
}