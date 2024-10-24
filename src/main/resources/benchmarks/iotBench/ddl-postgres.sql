    DROP TABLE IF EXISTS ActionLogs;
    DROP TABLE IF EXISTS AutomationProfile;
    DROP TABLE IF EXISTS sensor_log;
    DROP TABLE IF EXISTS Sensor;
    DROP TABLE IF EXISTS Device;
    DROP TABLE IF EXISTS Room;
    DROP TABLE IF EXISTS Hub;
    DROP TABLE IF EXISTS UserTable;

    CREATE TABLE UserTable (
    	userId INT PRIMARY KEY SERIAL,
        name VARCHAR(255),
        email VARCHAR(255),
        password_hash VARCHAR(255),
        userType int
    );

    CREATE TABLE Hub (
        hubId INT PRIMARY KEY SERIAL,
        name VARCHAR(255),
        status VARCHAR(50)
    );

    CREATE TABLE Room (
        roomId INT PRIMARY KEY SERIAL,
        name VARCHAR(255),
        room_type int
    );

    CREATE TABLE Device (
        deviceId INT PRIMARY KEY SERIAL,
        name VARCHAR(255),
        status VARCHAR(50),
        device_type INT,
        room_id INT,
        hub_id INT
    );

    CREATE TABLE Sensor (
        sensorId INT PRIMARY KEY SERIAL,
        name VARCHAR(255),
        type int,
        value DECIMAL(10, 2),
        deviceId INT
    );

    CREATE TABLE SensorLog (
        id INT PRIMARY KEY SERIAL,
        sensor_id INT,
        value DECIMAL(10, 2),
        date timestamp
    );

    CREATE TABLE AutomationProfile (
        profile_id INT PRIMARY KEY SERIAL,
        device_id INT,
        user_id int,
        status VARCHAR(50),
        command VARCHAR(255)
    );

    CREATE TABLE ActionLogs (
        log_id INT PRIMARY KEY SERIAL,
        user_id INT,
        device_id INT,
        action VARCHAR(255),
        status VARCHAR(50),
        date timestamp
    );
