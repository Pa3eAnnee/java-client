package com.odm.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Route(value = "events", layout = MainView.class)
public class EventsView extends VerticalLayout {

    private final UI ui;
    private Grid<Event> eventsGrid;
    private ListDataProvider<Event> dataProvider;
    private static final Path OFFLINE_EVENTS_FILE = Paths.get("offline-events.json");

    public EventsView() {
        this.ui = UI.getCurrent();
        setMargin(true);
        setAlignItems(Alignment.CENTER);
        add(new H2("Events"));
        add(createAddButton());
        setupGrid();
        if (SessionManager.isOfflineMode()) {
            loadEventsFromFile();
        } else {
            fetchEvents();
        }
    }

    private Button createAddButton() {
        Button addButton = new Button("Add Event", e -> {
            if (!SessionManager.isOfflineMode()) {
                showAddDialog();
            } else {
                Notification.show("Cannot add events in offline mode.");
            }
        });
        addButton.getStyle().set("background-color", "green");
        addButton.getStyle().set("color", "white");
        return addButton;
    }

    private void setupGrid() {
        eventsGrid = new Grid<>(Event.class, false);
        dataProvider = new ListDataProvider<>(new ArrayList<>());
        eventsGrid.setDataProvider(dataProvider);
        eventsGrid.addColumn(Event::getTitle).setHeader("Title");
        eventsGrid.addComponentColumn(this::createButtonLayout).setHeader("Actions");
        add(eventsGrid);
    }

    private HorizontalLayout createButtonLayout(Event event) {
        Icon viewIcon = VaadinIcon.EYE.create();
        viewIcon.getStyle().set("color", "blue");
        viewIcon.getElement().setAttribute("title", "View");
        viewIcon.addClickListener(e -> showEventDetails(event));

        Icon editIcon = VaadinIcon.PENCIL.create();
        editIcon.getStyle().set("color", "yellow");
        editIcon.getElement().setAttribute("title", "Edit");
        editIcon.addClickListener(e -> {
            if (!SessionManager.isOfflineMode()) {
                showEditDialog(event);
            } else {
                Notification.show("Cannot edit events in offline mode.");
            }
        });

        Icon deleteIcon = VaadinIcon.TRASH.create();
        deleteIcon.getStyle().set("color", "darkred");
        deleteIcon.getElement().setAttribute("title", "Delete");
        deleteIcon.addClickListener(e -> {
            if (!SessionManager.isOfflineMode()) {
                deleteEvent(event);
            } else {
                Notification.show("Cannot delete events in offline mode.");
            }
        });

        return new HorizontalLayout(viewIcon, editIcon, deleteIcon);
    }

    private void showEventDetails(Event event) {
        Dialog dialog = new Dialog();
        dialog.add(new H2(event.getTitle()));
        dialog.add(new Paragraph("Description: " + event.getDescription()));
        dialog.add(new Image(event.getImage(), "event image"));
        dialog.add(new Paragraph("Location: " + event.getLocation()));
        dialog.add(new Paragraph("Start: " + event.getDateStart()));
        dialog.add(new Paragraph("End: " + event.getDateEnd()));
        dialog.add(new Paragraph("Cost: " + event.getCost() + " USD"));
        dialog.add(new Paragraph("Status: " + event.getStatus()));
        dialog.add(new Button("Close", e -> dialog.close()));
        dialog.open();
    }

    private void showAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        TextField titleField = new TextField("Title");
        titleField.setWidthFull();

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setWidthFull();

        TextField imageField = new TextField("Image URL");
        imageField.setWidthFull();

        TextField locationField = new TextField("Location");
        locationField.setWidthFull();

        DateTimePicker startField = new DateTimePicker("Start Date");
        startField.setWidthFull();

        DateTimePicker endField = new DateTimePicker("End Date");
        endField.setWidthFull();

        TextField costField = new TextField("Cost");
        costField.setWidthFull();

        TextField statusField = new TextField("Status");
        statusField.setWidthFull();

        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setWidthFull();
        formLayout.add(
                titleField,
                descriptionField,
                imageField,
                locationField,
                startField,
                endField,
                costField,
                statusField
        );

        Button saveButton = new Button("Save", e -> {
            EventCreateDTO newEvent = new EventCreateDTO();
            newEvent.setTitle(titleField.getValue());
            newEvent.setDescription(descriptionField.getValue());
            newEvent.setImage(imageField.getValue());
            newEvent.setLocation(locationField.getValue());
            newEvent.setDateStart(startField.getValue() != null ? startField.getValue().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null);
            newEvent.setDateEnd(endField.getValue() != null ? endField.getValue().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null);
            newEvent.setCost(Integer.parseInt(costField.getValue()));
            newEvent.setStatus(statusField.getValue());

            addEvent(newEvent);
            dialog.close();
        });
        Button cancelButton = new Button("Cancel", e -> dialog.close());

        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(formLayout, buttonsLayout);
        dialog.open();
    }

    private void showEditDialog(Event event) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.add(new H2("Edit Event"));

        TextField titleField = new TextField("Title");
        titleField.setWidthFull();
        titleField.setValue(event.getTitle() != null ? event.getTitle() : "");

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setWidthFull();
        descriptionField.setValue(event.getDescription() != null ? event.getDescription() : "");

        TextField imageField = new TextField("Image URL");
        imageField.setWidthFull();
        imageField.setValue(event.getImage() != null ? event.getImage() : "");

        TextField locationField = new TextField("Location");
        locationField.setWidthFull();
        locationField.setValue(event.getLocation() != null ? event.getLocation() : "");

        DateTimePicker startField = new DateTimePicker("Start Date");
        startField.setWidthFull();
        if (event.getDateStart() != null) {
            startField.setValue(LocalDateTime.parse(event.getDateStart(), DateTimeFormatter.ISO_DATE_TIME));
        }

        DateTimePicker endField = new DateTimePicker("End Date");
        endField.setWidthFull();
        if (event.getDateEnd() != null) {
            endField.setValue(LocalDateTime.parse(event.getDateEnd(), DateTimeFormatter.ISO_DATE_TIME));
        }

        TextField costField = new TextField("Cost");
        costField.setWidthFull();
        costField.setValue(String.valueOf(event.getCost()));

        TextField statusField = new TextField("Status");
        statusField.setWidthFull();
        statusField.setValue(event.getStatus() != null ? event.getStatus() : "");

        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setWidthFull();
        formLayout.add(
                titleField,
                descriptionField,
                imageField,
                locationField,
                startField,
                endField,
                costField,
                statusField
        );

        Button saveButton = new Button("Save", e -> {
            event.setTitle(titleField.getValue());
            event.setDescription(descriptionField.getValue());
            event.setImage(imageField.getValue());
            event.setLocation(locationField.getValue());
            event.setDateStart(startField.getValue() != null ? startField.getValue().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null);
            event.setDateEnd(endField.getValue() != null ? endField.getValue().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null);
            event.setCost(Integer.parseInt(costField.getValue()));
            event.setStatus(statusField.getValue());

            updateEvent(event);
            dialog.close();
        });
        Button cancelButton = new Button("Cancel", e -> dialog.close());

        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(formLayout, buttonsLayout);
        dialog.open();
    }

    private void addEvent(EventCreateDTO event) {
        if (SessionManager.isOfflineMode()) {
            Notification.show("Cannot add events in offline mode.");
            return;
        }

        WebClient webClient = WebClient.create("http://localhost:3000");
        System.out.println("Sending POST request for new event: " + event);

        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonString = mapper.writeValueAsString(event);
            System.out.println("JSON Body: " + jsonString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Mono<Event> response = webClient.post()
                .uri("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(event)
                .retrieve()
                .bodyToMono(Event.class);

        response.subscribe(
                newEvent -> {
                    System.out.println("POST request successful for event: " + newEvent);
                    ui.access(() -> {
                        Notification.show("Event added successfully.");
                        ui.getPage().reload(); // Rafraîchit la page après ajout
                    });
                },
                error -> {
                    System.out.println("POST request failed for event: " + event + " with error: " + error.getMessage());
                    ui.access(() -> {
                        Notification.show("Failed to add event.", 3000, Notification.Position.MIDDLE);
                    });
                }
        );
    }

    private void updateEvent(Event event) {
        if (SessionManager.isOfflineMode()) {
            Notification.show("Cannot update events in offline mode.");
            return;
        }

        WebClient webClient = WebClient.create("http://localhost:3000");
        System.out.println("Sending PATCH request for event: " + event);

        Mono<Void> response = webClient.patch()
                .uri("/events/" + event.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(event)
                .retrieve()
                .bodyToMono(Void.class);

        response.subscribe(
                success -> {
                    System.out.println("PATCH request successful for event: " + event);
                    ui.access(() -> {
                        Notification.show("Event updated successfully.");
                        ui.getPage().reload(); // Rafraîchit la page après mise à jour
                    });
                },
                error -> {
                    System.out.println("PATCH request failed for event: " + event + " with error: " + error.getMessage());
                    ui.access(() -> {
                        Notification.show("Failed to update event.", 3000, Notification.Position.MIDDLE);
                    });
                }
        );
    }

    private void deleteEvent(Event event) {
        if (SessionManager.isOfflineMode()) {
            Notification.show("Cannot delete events in offline mode.");
            return;
        }

        WebClient webClient = WebClient.create("http://localhost:3000");
        webClient.delete()
                .uri("/events/" + event.getId())
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe(
                        success -> {
                            ui.access(() -> {
                                Notification.show("Event deleted successfully.");
                                dataProvider.getItems().remove(event);
                                eventsGrid.getDataProvider().refreshAll();
                            });
                        },
                        error -> {
                            ui.access(() -> {
                                Notification.show("Failed to delete event.", 3000, Notification.Position.MIDDLE);
                            });
                        }
                );
    }

    private void fetchEvents() {
        WebClient webClient = WebClient.create("http://localhost:3000");
        webClient.get()
                .uri("/events")
                .retrieve()
                .bodyToFlux(Event.class)
                .collectList()
                .subscribe(events -> {
                    ui.access(() -> {
                        eventsGrid.setItems(events);
                        saveEventsToFile(events); // Save events to file after fetching
                    });
                }, error -> {
                    ui.access(() -> {
                        Notification.show("Failed to load events.", 3000, Notification.Position.MIDDLE);
                    });
                });
    }

    private void saveEventsToFile(List<Event> events) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            mapper.writeValue(OFFLINE_EVENTS_FILE.toFile(), events);
            System.out.println("Events saved to file.");
        } catch (IOException e) {
            System.err.println("Failed to save events to file: " + e.getMessage());
        }
    }

    private void loadEventsFromFile() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Event> events = mapper.readValue(OFFLINE_EVENTS_FILE.toFile(), mapper.getTypeFactory().constructCollectionType(List.class, Event.class));
            eventsGrid.setItems(events);
            System.out.println("Events loaded from file.");
        } catch (IOException e) {
            System.err.println("Failed to load events from file: " + e.getMessage());
        }
    }
}
