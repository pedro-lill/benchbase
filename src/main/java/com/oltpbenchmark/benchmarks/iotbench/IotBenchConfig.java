package com.oltpbenchmark.benchmarks.iotbench;

import java.text.SimpleDateFormat;

public final class IotBenchConfig {

    public static final String[] nameTokens = {
        "BAR", "OUGHT", "ABLE", "PRI", "PRES", "ESE", "ANTI", "CALLY", "ATION", "EING"
    };

    public static final String terminalPrefix = "Term-";
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // Quantidade de cada entidade
    public static final int configHubCount = 5; // Número de hubs
    public static final int configRoomsPerHub = 10; // Número de salas por hub
    public static final int configDevicesPerRoom = 5; // Número de dispositivos por sala
    public static final int configSensorsPerDevice = 3; // Número de sensores por dispositivo
    public static final int configUserCount = 100; // Número de usuários

    // Configuração do batch size para carregamento de dados
    public static final int batchSize = 100;
}