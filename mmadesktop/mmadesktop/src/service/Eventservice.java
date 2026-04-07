package service;

import dao.EventDao;
import model.Event;
import java.time.LocalDate;
import java.util.List;

public class Eventservice {
    private EventDao eventDao;

    public Eventservice() {
        this.eventDao = new EventDao();
    }

    public boolean addEvent(Event event) {
        if (event.getEventName() == null || event.getEventName().isEmpty()) {
            return false;
        }
        if (event.getEventDate() == null || event.getEventDate().isBefore(LocalDate.now())) {
            return false;
        }
        return eventDao.addEvent(event);
    }

    public Event getEventById(int eventId) {
        return eventDao.getEventById(eventId);
    }

    public List<Event> getAllEvents() {
        return eventDao.getAllEvents();
    }

    public boolean updateEvent(Event event) {
        if (event.getEventId() <= 0) {
            return false;
        }
        return eventDao.updateEvent(event);
    }

    public boolean deleteEvent(int eventId) {
        if (eventId <= 0) {
            return false;
        }
        return eventDao.deleteEvent(eventId);
    }

    public List<Event> getUpcomingEvents() {
        List<Event> allEvents = eventDao.getAllEvents();
        allEvents.removeIf(event -> event.getEventDate().isBefore(LocalDate.now()));
        return allEvents;
    }
}