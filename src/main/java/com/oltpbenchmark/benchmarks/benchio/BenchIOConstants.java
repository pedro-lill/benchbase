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

 public abstract class BenchIOConstants {
     // Tabelas principais do sistema IoT
     public static final String TABLENAME_USER = "usertable";
     public static final String TABLENAME_HUB = "hub";
     public static final String TABLENAME_ROOM = "room";
     public static final String TABLENAME_DEVICE = "device";
     public static final String TABLENAME_SENSOR = "sensor";

     // Tabelas de registros e logs
     public static final String TABLENAME_SENSORLOG = "sensorlog";
     public static final String TABLENAME_ACTIONLOGS = "actionlogs";
     public static final String TABLENAME_AUTOMATION = "automationprofile";

     // Nomes de colunas comuns (opcional - pode ser útil para consultas)
     public static final String COLUMN_ID = "id";
     public static final String COLUMN_NAME = "name";
     public static final String COLUMN_STATUS = "status";
     public static final String COLUMN_TIMESTAMP = "timestamp";
     public static final String COLUMN_VALUE = "value";
 }