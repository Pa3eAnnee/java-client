package com.odm.client;

import com.odm.client.PluginManager;
import org.springframework.stereotype.Component;
import java.io.File;

@Component
public class PluginLoader {

    public void loadPlugins() {
        File pluginsDir = new File("plugins");
        if (pluginsDir.exists()) {
            File[] files = pluginsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".jar")) {
                        try {
                            PluginManager.getInstance().loadPlugin(file);
                        } catch (Exception e) {
                            System.err.println("Failed to load plugin from " + file.getName() + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            System.err.println("Plugins directory not found.");
        }
    }
}
