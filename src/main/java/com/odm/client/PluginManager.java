package com.odm.client;

import com.odm.Plugin;
import com.vaadin.flow.component.UI;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public class PluginManager {
    private static PluginManager instance;
    private Map<String, PluginData> activePlugins = new HashMap<>();

    private PluginManager() {}

    public static synchronized PluginManager getInstance() {
        if (instance == null) {
            instance = new PluginManager();
        }
        return instance;
    }

    public void loadPlugin(File jarFile) throws Exception {
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{jarFile.toURI().toURL()});
        ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, classLoader);
        for (Plugin plugin : serviceLoader) {
            plugin.start();
            UI.getCurrent().access(() -> {
                MainView mainView = MainView.getCurrentInstance();
                if (mainView != null) {
                    mainView.addNavigationTab(plugin.getName(), plugin.getComponent().getClass());
                }
            });
            activePlugins.put(plugin.getName(), new PluginData(plugin, classLoader));
        }
    }

    public void unloadPlugin(String pluginName) {
        PluginData data = activePlugins.get(pluginName);
        if (data != null) {
            UI.getCurrent().access(() -> {
                MainView mainView = findMainView();
                if (mainView != null) {
                    mainView.removeNavigationTab(pluginName);
                }
                data.plugin.stop();
                activePlugins.remove(pluginName);
            });
            try {
                data.classLoader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> getPlugins() {
        return new ArrayList<>(activePlugins.keySet());
    }

    private MainView findMainView() {
        // Implement logic to find and return the MainView instance from the UI
        return (MainView) UI.getCurrent().getChildren()
                .filter(component -> component instanceof MainView)
                .findFirst()
                .orElse(null);
    }

    private static class PluginData {
        Plugin plugin;
        URLClassLoader classLoader;

        PluginData(Plugin plugin, URLClassLoader classLoader) {
            this.plugin = plugin;
            this.classLoader = classLoader;
        }
    }
}

