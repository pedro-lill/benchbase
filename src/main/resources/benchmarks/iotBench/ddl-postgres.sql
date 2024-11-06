DROP TABLE IF EXISTS ACTIONLOGS;
DROP TABLE IF EXISTS AUTOMATIONPROFILE;
DROP TABLE IF EXISTS SENSORLOG;
DROP TABLE IF EXISTS SENSOR;
DROP TABLE IF EXISTS DEVICE;
DROP TABLE IF EXISTS ROOM;
DROP TABLE IF EXISTS HUB;
DROP TABLE IF EXISTS USERTABLE;

CREATE TABLE USERTABLE (
    userId INT PRIMARY KEY,
    nameIot VARCHAR(255),
    email VARCHAR(255),
    password_hash VARCHAR(255),
    usertype int
);

CREATE TABLE HUB (
    hubId INT PRIMARY KEY,
    name VARCHAR(255),
    status VARCHAR(50)
);

CREATE TABLE ROOM (
    roomId INT PRIMARY KEY,
    name VARCHAR(255),
    room_type int
);

CREATE TABLE DEVICE (
    deviceId INT PRIMARY KEY,
    name VARCHAR(255),
    status VARCHAR(50),
    device_type INT,
    room_id INT,
    hub_id INT
);

CREATE TABLE SENSOR (
    sensorId INT PRIMARY KEY,
    name VARCHAR(255),
    type int,
    value DECIMAL(10, 2),
    deviceId INT
);

CREATE TABLE SENSORLOG (
    id INT PRIMARY KEY,
    sensor_id INT,
    value DECIMAL(10, 2),
    date timestamp
);

CREATE TABLE AUTOMATIONPROFILE (
    profile_id INT PRIMARY KEY,
    device_id INT,
    user_id int,
    status VARCHAR(50),
    command VARCHAR(255)
);

CREATE TABLE ACTIONLOGS (
    log_id INT PRIMARY KEY,
    user_id INT,
    device_id INT,
    action VARCHAR(255),
    status VARCHAR(50),
    date timestamp
);
