package com.odm.client;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@CssImport("./styles/shared-styles.css")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public VaadinServiceInitListener vaadinServiceInitListener() {
        return new PluginServiceInitListener();
    }
}
