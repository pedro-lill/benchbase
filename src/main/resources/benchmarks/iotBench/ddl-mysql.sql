DROP TABLE IF EXISTS actionlogs;
DROP TABLE IF EXISTS automationprofile;
DROP TABLE IF EXISTS sensorlog;
DROP TABLE IF EXISTS sensor;
DROP TABLE IF EXISTS device;
DROP TABLE IF EXISTS room;
DROP TABLE IF EXISTS hub;
DROP TABLE IF EXISTS usertable;

CREATE TABLE usertable (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name_iot VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    user_type INT NOT NULL
);

CREATE TABLE hub (
    hub_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE room (
    room_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    room_type INT NOT NULL
);

CREATE TABLE device (
    device_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    device_type INT NOT NULL,
    room_id INT,
    hub_id INT,
    FOREIGN KEY (room_id) REFERENCES room(room_id) ON DELETE SET NULL,
    FOREIGN KEY (hub_id) REFERENCES hub(hub_id) ON DELETE SET NULL
);

CREATE TABLE sensor (
    sensor_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    type INT NOT NULL,
    value DECIMAL(10, 2) NOT NULL,
    device_id INT,
    FOREIGN KEY (device_id) REFERENCES device(device_id) ON DELETE SET NULL
);

CREATE TABLE sensorlog (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sensor_id INT NOT NULL,
    value DECIMAL(10, 2) NOT NULL,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sensor_id) REFERENCES sensor(sensor_id) ON DELETE CASCADE
);

CREATE TABLE automationprofile (
    profile_id INT PRIMARY KEY AUTO_INCREMENT,
    device_id INT NOT NULL,
    user_id INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    command VARCHAR(255) NOT NULL,
    FOREIGN KEY (device_id) REFERENCES device(device_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES usertable(user_id) ON DELETE CASCADE
);

CREATE TABLE actionlogs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    device_id INT NOT NULL,
    action VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES usertable(user_id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES device(device_id) ON DELETE CASCADE
);