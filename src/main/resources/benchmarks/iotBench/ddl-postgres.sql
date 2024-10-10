    DROP TABLE IF EXISTS ActionLogs;
    DROP TABLE IF EXISTS AutomationProfile;
    DROP TABLE IF EXISTS sensor_log;
    DROP TABLE IF EXISTS Sensor;
    DROP TABLE IF EXISTS Device;
    DROP TABLE IF EXISTS Room;
    DROP TABLE IF EXISTS Hub;
    DROP TABLE IF EXISTS UserTable;

    CREATE TABLE UserTable (
    	userId INT PRIMARY KEY,
        name VARCHAR(255),
        email VARCHAR(255),
        password_hash VARCHAR(255),
        userType int
    );

    CREATE TABLE Hub (
        hubId INT PRIMARY KEY,
        name VARCHAR(255),
        status VARCHAR(50)
    );

    CREATE TABLE Room (
        roomId INT PRIMARY KEY,
        name VARCHAR(255),
        room_type int
    );

    CREATE TABLE Device (
        deviceId INT PRIMARY KEY,
        name VARCHAR(255),
        status VARCHAR(50),
        device_type INT,
        room_id INT,
        hub_id INT,
        FOREIGN KEY (room_id) REFERENCES Room(roomId),
        FOREIGN KEY (hub_id) REFERENCES Hub(hubId)
    );

    CREATE TABLE Sensor (
        sensorId INT PRIMARY KEY,
        name VARCHAR(255),
        type int,
        value DECIMAL(10, 2),
        deviceId INT,
        FOREIGN KEY (deviceId) REFERENCES Device(deviceId)
    );

    CREATE TABLE SensorLog (
        id INT PRIMARY KEY,
        sensor_id INT,
        value DECIMAL(10, 2),
        date timestamp,
        FOREIGN KEY (sensor_id) REFERENCES Sensor(sensorId)
    );

    CREATE TABLE AutomationProfile (
        profile_id INT PRIMARY KEY,
        device_id INT,
        user_id int,
        status VARCHAR(50),
        command VARCHAR(255),
        FOREIGN KEY (device_id) REFERENCES Device(deviceId),
        FOREIGN KEY (user_id) REFERENCES UserTable(userId)
    );

    CREATE TABLE ActionLogs (
        log_id INT PRIMARY KEY,
        user_id INT,
        device_id INT,
        action VARCHAR(255),
        status VARCHAR(50),
        date timestamp,
        FOREIGN KEY (user_id) REFERENCES UserTable(userId),
        FOREIGN KEY (device_id) REFERENCES Device(deviceId)
    );
