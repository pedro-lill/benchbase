package com.oltpbenchmark.benchmarks.iotbench.pojo;

public class User {
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
