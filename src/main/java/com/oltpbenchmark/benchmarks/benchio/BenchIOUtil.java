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

 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.Random;

 import com.oltpbenchmark.benchmarks.benchio.pojo.ActionLogs;
 import com.oltpbenchmark.benchmarks.benchio.pojo.AutomationProfile;
import com.oltpbenchmark.benchmarks.benchio.pojo.Device;
import com.oltpbenchmark.benchmarks.benchio.pojo.Hub;
import com.oltpbenchmark.benchmarks.benchio.pojo.Sensor;
import com.oltpbenchmark.benchmarks.benchio.pojo.SensorLog;
import com.oltpbenchmark.benchmarks.benchio.pojo.UserTable;
import com.oltpbenchmark.util.RandomGenerator;

 public class BenchIOUtil {

     private static final RandomGenerator ran = new RandomGenerator(0);

     // Formato de data e hora
     private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

     /**
      * Creates a UserTable object from the current row in the given ResultSet.
      *
      * @param rs an open ResultSet positioned to the desired row
      * @return the newly created UserTable object
      * @throws SQLException for problems getting data from row
      */
     public static UserTable newUserFromResults(ResultSet rs) throws SQLException {
         UserTable user = new UserTable();
         user.setUserId(rs.getInt("user_id"));
         user.setNameIot(rs.getString("name_iot"));
         user.setEmail(rs.getString("email"));
         user.setPasswordHash(rs.getString("password_hash"));
         user.setUserType(rs.getInt("user_type"));
         return user;
     }

     /**
      * Creates a Hub object from the current row in the given ResultSet.
      *
      * @param rs an open ResultSet positioned to the desired row
      * @return the newly created Hub object
      * @throws SQLException for problems getting data from row
      */
     public static Hub newHubFromResults(ResultSet rs) throws SQLException {
         Hub hub = new Hub();
         hub.setHubId(rs.getInt("hub_id"));
         hub.setName(rs.getString("name"));
         hub.setStatus(rs.getString("status"));
         return hub;
     }

     /**
      * Creates a Device object from the current row in the given ResultSet.
      *
      * @param rs an open ResultSet positioned to the desired row
      * @return the newly created Device object
      * @throws SQLException for problems getting data from row
      */
     public static Device newDeviceFromResults(ResultSet rs) throws SQLException {
         Device device = new Device();
         device.setDeviceId(rs.getInt("device_id"));
         device.setName(rs.getString("name"));
         device.setStatus(rs.getString("status"));
         device.setDeviceType(rs.getInt("device_type"));
         device.setRoomId(rs.getInt("room_id"));
         device.setHubId(rs.getInt("hub_id"));
         return device;
     }

     /**
      * Creates a Sensor object from the current row in the given ResultSet.
      *
      * @param rs an open ResultSet positioned to the desired row
      * @return the newly created Sensor object
      * @throws SQLException for problems getting data from row
      */
     public static Sensor newSensorFromResults(ResultSet rs) throws SQLException {
         Sensor sensor = new Sensor();
         sensor.setSensorId(rs.getInt("sensor_id"));
         sensor.setName(rs.getString("name"));
         sensor.setType(rs.getInt("type"));
         sensor.setValue(rs.getDouble("value"));
         sensor.setDeviceId(rs.getInt("device_id"));
         return sensor;
     }

     /**
      * Creates a SensorLog object from the current row in the given ResultSet.
      *
      * @param rs an open ResultSet positioned to the desired row
      * @return the newly created SensorLog object
      * @throws SQLException for problems getting data from row
      */
     public static SensorLog newSensorLogFromResults(ResultSet rs) throws SQLException {
         SensorLog sensorLog = new SensorLog();
         sensorLog.setId(rs.getInt("id"));
         sensorLog.setSensorId(rs.getInt("sensor_id"));
         sensorLog.setValue(rs.getDouble("value"));
         sensorLog.setDate(rs.getTimestamp("date"));
         return sensorLog;
     }

     /**
      * Creates an AutomationProfile object from the current row in the given ResultSet.
      *
      * @param rs an open ResultSet positioned to the desired row
      * @return the newly created AutomationProfile object
      * @throws SQLException for problems getting data from row
      */
     public static AutomationProfile newAutomationProfileFromResults(ResultSet rs) throws SQLException {
         AutomationProfile profile = new AutomationProfile();
         profile.setProfileId(rs.getInt("profile_id"));
         profile.setDeviceId(rs.getInt("device_id"));
         profile.setUserId(rs.getInt("user_id"));
         profile.setStatus(rs.getString("status"));
         profile.setCommand(rs.getString("command"));
         return profile;
     }

     /**
      * Creates an ActionLogs object from the current row in the given ResultSet.
      *
      * @param rs an open ResultSet positioned to the desired row
      * @return the newly created ActionLogs object
      * @throws SQLException for problems getting data from row
      */
     public static ActionLogs newActionLogsFromResults(ResultSet rs) throws SQLException {
         ActionLogs actionLog = new ActionLogs();
         actionLog.setLogId(rs.getInt("log_id"));
         actionLog.setUserId(rs.getInt("user_id"));
         actionLog.setDeviceId(rs.getInt("device_id"));
         actionLog.setAction(rs.getString("action"));
         actionLog.setStatus(rs.getString("status"));
         actionLog.setDate(rs.getTimestamp("date"));
         return actionLog;
     }

     /**
      * Generates a random string of the specified length.
      *
      * @param strLen the length of the string to generate
      * @return the generated string
      */
     public static String randomStr(int strLen) {
         if (strLen > 1) {
             return ran.astring(strLen - 1, strLen - 1);
         } else {
             return "";
         }
     }

     /**
      * Generates a random numeric string of the specified length.
      *
      * @param stringLength the length of the string to generate
      * @return the generated numeric string
      */
     public static String randomNStr(int stringLength) {
         if (stringLength > 0) {
             return ran.nstring(stringLength, stringLength);
         } else {
             return "";
         }
     }

     /**
      * Returns the current time as a formatted string.
      *
      * @return the current time as a string
      */
     public static String getCurrentTime() {
         return dateFormat.format(new java.util.Date());
     }

     /**
      * Formats a double value to a string with a maximum of 6 characters.
      *
      * @param d the double value to format
      * @return the formatted string
      */
     public static String formattedDouble(double d) {
         String dS = "" + d;
         return dS.length() > 6 ? dS.substring(0, 6) : dS;
     }

     /**
      * Generates a random number between min and max (inclusive).
      *
      * @param min the minimum value
      * @param max the maximum value
      * @param r   the Random object to use
      * @return the generated random number
      */
     public static int randomNumber(int min, int max, Random r) {
         return (int) (r.nextDouble() * (max - min + 1) + min);
     }

     /**
      * Generates a non-uniform random number based on the TPC-C specification.
      *
      * @param A   the range parameter
      * @param C   the constant parameter
      * @param min the minimum value
      * @param max the maximum value
      * @param r   the Random object to use
      * @return the generated random number
      */
     public static int nonUniformRandom(int A, int C, int min, int max, Random r) {
         return (((randomNumber(0, A, r) | randomNumber(min, max, r)) + C) % (max - min + 1)) + min;
     }
 }