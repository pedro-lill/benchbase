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

package com.oltpbenchmark.benchmarks.benchio;

import java.text.SimpleDateFormat;

public final class BenchIOConfig {

  // Configurações básicas do benchmark
  public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  // Quantidades padrão para o cenário IoT
  public static final int configHubCount = 5; // Número total de hubs
  public static final int configUserCount = 100; // Número total de usuários
  public static final int configRoomsPerHub = 10; // Salas por hub
  public static final int configDevicesPerRoom = 5; // Dispositivos por sala
  public static final int configSensorsPerDevice = 3; // Sensores por dispositivo
  public static final int configAutomationProfiles = 5;
  public static final int configInitialSensorLogs = 8;
  public static final int configInitialActionLogs = 6;

  // SENSOR_TYPE_TEMPERATURE
  // SENSOR_TYPE_HUMIDITY
  // SENSOR_TYPE_MOTION

  public static final int configSensorCount = 3; // Número total de sensores por dispositivo

  // Configurações de simulação
  public static final int sensorReadingInterval = 5000; // Intervalo entre leituras de sensor em ms
  public static final double minSensorValue = 0.0; // Valor mínimo para leituras de sensores
  public static final double maxSensorValue = 100.0; // Valor máximo para leituras de sensores

  // Tipos de dispositivos
  public static final int DEVICE_TYPE_LIGHT = 1;
  public static final int DEVICE_TYPE_THERMOSTAT = 2;
  public static final int DEVICE_TYPE_SECURITY = 3;
  public static final int DEVICE_TYPE_LOCK = 4;

  // Tipos de sensores
  public static final int SENSOR_TYPE_TEMPERATURE = 1;
  public static final int SENSOR_TYPE_HUMIDITY = 2;
  public static final int SENSOR_TYPE_MOTION = 3;

  // Status possíveis
  public static final String STATUS_ACTIVE = "active";
  public static final String STATUS_INACTIVE = "inactive";
  public static final String STATUS_ERROR = "error";

  // Comandos de automação comuns
  public static final String CMD_TURN_ON = "turn_on";
  public static final String CMD_TURN_OFF = "turn_off";
  public static final String CMD_ADJUST = "adjust";
}
