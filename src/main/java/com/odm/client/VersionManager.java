package com.odm.client;

import java.io.PrintWriter;
import java.util.Properties;

public class VersionManager {
    static void updateVersionFile() {
        Properties properties = new Properties();
        try {
            properties.load(Application.class.getResourceAsStream("/odm-client.properties"));
            System.out.println("Current version: "+properties.getProperty("version"));
            PrintWriter writer = new PrintWriter("java-client.version");
            writer.print(properties.getProperty("version"));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}