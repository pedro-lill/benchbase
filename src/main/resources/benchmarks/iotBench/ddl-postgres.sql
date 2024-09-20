DROP TABLE IF EXISTS sensor_data;

CREATE TABLE sensor_data (
    id SERIAL PRIMARY KEY,
    timestamp BIGINT NOT NULL,
    temperature DOUBLE PRECISION,
    humidity DOUBLE PRECISION
);
