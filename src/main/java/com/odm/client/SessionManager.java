package com.odm.client;

import com.vaadin.flow.server.VaadinSession;

public class SessionManager {

    private static final String OFFLINE_MODE_KEY = "offlineMode";

    public static void setOfflineMode(boolean isOffline) {
        VaadinSession.getCurrent().setAttribute(OFFLINE_MODE_KEY, isOffline);
    }

    public static boolean isOfflineMode() {
        Boolean isOffline = (Boolean) VaadinSession.getCurrent().getAttribute(OFFLINE_MODE_KEY);
        return isOffline != null && isOffline;
    }
}

