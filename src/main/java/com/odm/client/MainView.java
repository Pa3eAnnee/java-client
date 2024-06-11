package com.odm.client;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;

@CssImport("./styles/shared-styles.css")
public class MainView extends AppLayout {
    private static MainView currentInstance;

    private VerticalLayout menuLayout;
    private Checkbox offlineCheckbox;

    public MainView() {
        currentInstance = this; // Définir l'instance actuelle
        setPrimarySection(Section.DRAWER);
        DrawerToggle toggle = new DrawerToggle();
        H1 title = new H1("ODM Planification");

        // Création du sélecteur de thème
        Select<String> themeSelector = new Select<>();
        themeSelector.setItems("Lumo Light", "Lumo Dark");
        themeSelector.setValue("Lumo Light"); // Thème par défaut

        themeSelector.addValueChangeListener(event -> {
            applyTheme(event.getValue());
        });

        offlineCheckbox = new Checkbox("Offline Mode");
        offlineCheckbox.addClassName("switch");
        offlineCheckbox.setValue(SessionManager.isOfflineMode());
        offlineCheckbox.addValueChangeListener(event -> {
            boolean isOffline = event.getValue();
            SessionManager.setOfflineMode(isOffline); // Save the state to session
            Notification.show("Offline mode " + (isOffline ? "enabled" : "disabled"));
        });

        HorizontalLayout navbar = new HorizontalLayout(toggle, title, themeSelector, offlineCheckbox);
        navbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        navbar.expand(title);

        addToNavbar(navbar);

        menuLayout = new VerticalLayout();
        addToDrawer(menuLayout);
        addMenuItems();
    }

    private void applyTheme(String theme) {
        // Supprimer les anciens thèmes
        UI.getCurrent().getElement().getThemeList().clear();

        // Ajouter le nouveau thème
        switch (theme) {
            case "Lumo Light":
                UI.getCurrent().getElement().setAttribute("theme", Lumo.LIGHT);
                break;
            case "Lumo Dark":
                UI.getCurrent().getElement().setAttribute("theme", Lumo.DARK);
                break;
            default:
                break;
        }
    }

    public boolean isOffline() {
        return offlineCheckbox.getValue();
    }

    public static MainView getCurrentInstance() {
        return currentInstance;
    }

    private void addMenuItems() {
        RouterLink eventsLink = new RouterLink("Events", EventsView.class);
        RouterLink pluginsLink = new RouterLink("Plugins", PluginsView.class);
        menuLayout.add(eventsLink, pluginsLink);
    }

    public void addNavigationTab(String name, Class<? extends Component> viewClass) {
        UI.getCurrent().access(() -> {
            RouterLink link = new RouterLink(name, viewClass);
            menuLayout.add(link);
        });
    }

    public void removeNavigationTab(String name) {
        UI.getCurrent().access(() -> {
            menuLayout.getChildren()
                    .filter(component -> component instanceof RouterLink && ((RouterLink) component).getText().equals(name))
                    .findFirst()
                    .ifPresent(menuLayout::remove);
        });
    }
}
