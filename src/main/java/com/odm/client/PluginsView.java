package com.odm.client;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;

@Route(value = "plugins", layout = MainView.class)
@PageTitle("Plugins")
public class PluginsView extends VerticalLayout {

    public PluginsView() {
        updatePluginList();
    }

    private void updatePluginList() {
        removeAll();
        add(new H2("Loaded Plugins"));
        PluginManager.getInstance().getPlugins().forEach(pluginName -> {
            add(new H2(pluginName));
        });
    }
}
