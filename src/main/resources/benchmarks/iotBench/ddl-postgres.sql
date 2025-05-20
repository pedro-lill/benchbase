DROP TABLE IF EXISTS actionlogs CASCADE;
DROP TABLE IF EXISTS automationprofile CASCADE;
DROP TABLE IF EXISTS sensorlog CASCADE;
DROP TABLE IF EXISTS sensor CASCADE;
DROP TABLE IF EXISTS device CASCADE;
DROP TABLE IF EXISTS room CASCADE;
DROP TABLE IF EXISTS hub CASCADE;
DROP TABLE IF EXISTS usertable CASCADE;

-- Tabela usertable
CREATE TABLE usertable (
    user_id INT PRIMARY KEY,
    name_iot VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    user_type INT NOT NULL
);

-- Tabela hub
CREATE TABLE hub (
    hub_id INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL
);

-- Tabela room
CREATE TABLE room (
    room_id INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    room_type INT NOT NULL
);

-- Tabela device
CREATE TABLE device (
    device_id INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    device_type INT NOT NULL,
    room_id INT,
    hub_id INT
);

-- Tabela sensor
CREATE TABLE sensor (
    sensor_id INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type INT NOT NULL,
    value DECIMAL(10, 2) NOT NULL,
    device_id INT
);

-- Tabela sensorlog
CREATE TABLE sensorlog (
    id SERIAL PRIMARY KEY,
    sensor_id INT NOT NULL,
    value DECIMAL(10, 2) NOT NULL,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE automationprofile (
    profile_id INT PRIMARY KEY,
    device_id INT NOT NULL,
    user_id INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    command VARCHAR(255) NOT NULL
);

-- Tabela actionlogs
CREATE TABLE actionlogs (
    log_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    device_id INT NOT NULL,
    action VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
