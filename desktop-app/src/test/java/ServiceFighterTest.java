import entities.Fighter;
import org.junit.jupiter.api.*;
import services.ServiceFighter;

import java.time.LocalDate;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceFighterTest {

    private static ServiceFighter serviceFighter;
    private static int testFighterId = -1;
    // userId=1 exists in seed data (admin user inserted in Phase 2)
    private static final int EXISTING_USER_ID = 1;

    @BeforeAll
    static void setUp() {
        serviceFighter = new ServiceFighter();
        System.out.println("ServiceFighterTest initialized — using smartfight DB");
    }

    @Test
    @Order(1)
    void testAjouter() throws SQLException {
        Fighter fighter = new Fighter(
                EXISTING_USER_ID,
                "TestNick_AI",
                LocalDate.of(1995, 6, 15),
                "French",
                0, 0, 0, 0,
                "ACTIVE");
        serviceFighter.ajouter(fighter);

        List<Fighter> fighters = serviceFighter.recuperer();
        boolean found = false;
        for (Fighter f : fighters) {
            if ("TestNick_AI".equals(f.getNickname())) {
                testFighterId = f.getId();
                found = true;
                break;
            }
        }
        assertTrue(found, "Test fighter should have been inserted.");
        assertTrue(testFighterId > 0, "testFighterId should be a valid auto-generated ID");
        System.out.println("testAjouter passed — testFighterId=" + testFighterId);
    }

    @Test
    @Order(2)
    void testRecuperer() throws SQLException {
        List<Fighter> fighters = serviceFighter.recuperer();
        assertNotNull(fighters, "Fighter list should not be null.");
        assertFalse(fighters.isEmpty(), "Fighter list should not be empty.");
        System.out.println("testRecuperer passed — " + fighters.size() + " fighters found.");
    }

    @Test
    @Order(3)
    void testModifier() throws SQLException {
        Assumptions.assumeTrue(testFighterId > 0, "Skip update: test fighter not inserted.");
        Fighter fighter = new Fighter(
                testFighterId,
                EXISTING_USER_ID,
                "TestNick_Updated",
                LocalDate.of(1995, 6, 15),
                "Tunisian",
                0, 5, 2, 1,
                "ACTIVE");
        serviceFighter.modifier(fighter);

        List<Fighter> fighters = serviceFighter.recuperer();
        boolean updated = false;
        for (Fighter f : fighters) {
            if (f.getId() == testFighterId && "TestNick_Updated".equals(f.getNickname())) {
                updated = true;
                break;
            }
        }
        assertTrue(updated, "Test fighter should have been updated.");
        System.out.println("testModifier passed — nickname updated to TestNick_Updated");
    }

    @Test
    @Order(4)
    void testSupprimer() throws SQLException {
        Assumptions.assumeTrue(testFighterId > 0, "Skip delete: test fighter not inserted.");
        Fighter fighter = new Fighter();
        fighter.setId(testFighterId);
        serviceFighter.supprimer(fighter);

        List<Fighter> fighters = serviceFighter.recuperer();
        boolean stillExists = false;
        for (Fighter f : fighters) {
            if (f.getId() == testFighterId) {
                stillExists = true;
                break;
            }
        }
        assertFalse(stillExists, "Test fighter should have been deleted.");
        System.out.println("testSupprimer passed — testFighterId=" + testFighterId + " deleted");
        testFighterId = -1;
    }
}