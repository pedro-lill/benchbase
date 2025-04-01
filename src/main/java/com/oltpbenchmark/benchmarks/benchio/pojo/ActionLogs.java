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

  public void setDate(Timestamp date) {
      this.date = date;
  }
}