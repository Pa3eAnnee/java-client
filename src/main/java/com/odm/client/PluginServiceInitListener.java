package com.odm.client;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

public class PluginServiceInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            PluginLoader pluginLoader = new PluginLoader(); // Assurez-vous que PluginLoader est prêt à être utilisé
            pluginLoader.loadPlugins();
        });
    }
}