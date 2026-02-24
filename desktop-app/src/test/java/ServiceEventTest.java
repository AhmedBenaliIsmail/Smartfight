import entities.Event;
import org.junit.jupiter.api.*;
import services.ServiceEvent;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceEventTest {

    private static ServiceEvent serviceEvent;
    private static int testId = -1;
    private static final String TEST_EVENT_NAME = "JUNIT_TEST_EVENT";
    private static final String TEST_EVENT_NAME_UPDATE = "JUNIT_TEST_EVENT_UPDATED";

    @BeforeAll
    static void setUp() {
        serviceEvent = new ServiceEvent();
        System.out.println("ServiceEventTest initialized — using smartfight DB");
    }

    @Test
    @Order(1)
    void testAjouter() throws SQLException {
        Event event = new Event(
                TEST_EVENT_NAME,
                "JUnit 5 test insert",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 3),
                "SCHEDULED",
                "PRIVATE",
                100,
                0, 0, 0);
        serviceEvent.ajouter(event);

        List<Event> events = serviceEvent.recuperer();
        Assertions.assertFalse(events.isEmpty(), "Event list should not be empty after ajouter()");

        boolean found = false;
        for (Event e : events) {
            if (TEST_EVENT_NAME.equals(e.getName())) {
                found = true;
                testId = e.getId();
                break;
            }
        }
        Assertions.assertTrue(found, "Inserted event should be found in DB with name: " + TEST_EVENT_NAME);
        Assertions.assertTrue(testId > 0, "testId should be a valid auto-generated ID");
        System.out.println("testAjouter passed — testId=" + testId);
    }

    @Test
    @Order(2)
    void testModifier() throws SQLException {
        Assertions.assertTrue(testId > 0, "testId must be set by testAjouter() first");

        Event event = new Event(
                testId,
                TEST_EVENT_NAME_UPDATE,
                "JUnit 5 test update",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 5),
                "ONGOING",
                "PUBLIC",
                200,
                0, 0, 0);
        serviceEvent.modifier(event);

        List<Event> events = serviceEvent.recuperer();
        boolean found = false;
        for (Event e : events) {
            if (e.getId() == testId && TEST_EVENT_NAME_UPDATE.equals(e.getName())) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "Updated event should have name: " + TEST_EVENT_NAME_UPDATE);
        System.out.println("testModifier passed — name updated to: " + TEST_EVENT_NAME_UPDATE);
    }

    @Test
    @Order(3)
    void testSupprimer() throws SQLException {
        Assertions.assertTrue(testId > 0, "testId must be set by testAjouter() first");

        Event event = new Event();
        event.setId(testId);
        serviceEvent.supprimer(event);

        List<Event> events = serviceEvent.recuperer();
        boolean found = false;
        for (Event e : events) {
            if (e.getId() == testId) {
                found = true;
                break;
            }
        }
        Assertions.assertFalse(found, "Deleted event with id=" + testId + " should no longer exist in DB");
        System.out.println("testSupprimer passed — id=" + testId + " deleted");
        testId = -1;
    }

    @AfterEach
    void cleanUp() {
        try {
            Connection connection = MyDatabase.getInstance().getConnection();
            Statement st = connection.createStatement();
            st.executeUpdate("DELETE FROM event WHERE name='" + TEST_EVENT_NAME + "'");
            st.executeUpdate("DELETE FROM event WHERE name='" + TEST_EVENT_NAME_UPDATE + "'");
        } catch (SQLException e) {
            System.out.println("Cleanup warning: " + e.getMessage());
        }
    }
}
