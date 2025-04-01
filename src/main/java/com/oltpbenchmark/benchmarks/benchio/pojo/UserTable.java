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

public class UserTable {
  private int userId;
  private String nameIot;
  private String email;
  private String passwordHash;
  private int userType;

  // Getters e Setters
  public int getUserId() {
      return userId;
  }

  public void setUserId(int userId) {
      this.userId = userId;
  }

  public String getNameIot() {
      return nameIot;
  }

  public void setNameIot(String nameIot) {
      this.nameIot = nameIot;
  }

  public String getEmail() {
      return email;
  }

  public void setEmail(String email) {
      this.email = email;
  }

  public String getPasswordHash() {
      return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
      this.passwordHash = passwordHash;
  }

  public int getUserType() {
      return userType;
  }

  public void setUserType(int userType) {
      this.userType = userType;
  }
}