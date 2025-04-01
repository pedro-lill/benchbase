-- Remove as tabelas se já existirem
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
    hub_id INT,
    FOREIGN KEY (room_id) REFERENCES room (room_id) ON DELETE CASCADE,
    FOREIGN KEY (hub_id) REFERENCES hub (hub_id) ON DELETE CASCADE
);

-- Tabela sensor
CREATE TABLE sensor (
    sensor_id INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type INT NOT NULL,
    value DECIMAL(10, 2) NOT NULL,
    device_id INT,
    FOREIGN KEY (device_id) REFERENCES device (device_id) ON DELETE CASCADE
);

-- Tabela sensorlog
CREATE TABLE sensorlog (
    id SERIAL PRIMARY KEY, -- Usando SERIAL para autoincremento
    sensor_id INT NOT NULL,
    value DECIMAL(10, 2) NOT NULL,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sensor_id) REFERENCES sensor (sensor_id) ON DELETE CASCADE
);

-- Tabela automationprofile
CREATE TABLE automationprofile (
    profile_id INT PRIMARY KEY,
    device_id INT NOT NULL,
    user_id INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    command VARCHAR(255) NOT NULL,
    FOREIGN KEY (device_id) REFERENCES device (device_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES usertable (user_id) ON DELETE CASCADE
);

-- Tabela actionlogs
CREATE TABLE actionlogs (
    log_id SERIAL PRIMARY KEY, -- Usando SERIAL para autoincremento
    user_id INT NOT NULL,
    device_id INT NOT NULL,
    action VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES usertable (user_id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES device (device_id) ON DELETE CASCADE
);

-- Índices para melhorar o desempenho das consultas
CREATE INDEX idx_sensorlog_sensor_id ON sensorlog (sensor_id);
CREATE INDEX idx_actionlogs_user_id ON actionlogs (user_id);
CREATE INDEX idx_actionlogs_device_id ON actionlogs (device_id);
CREATE INDEX idx_automationprofile_device_id ON automationprofile (device_id);
CREATE INDEX idx_automationprofile_user_id ON automationprofile (user_id);