import entities.User;
import org.junit.jupiter.api.*;
import services.ServiceUser;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceUserTest {

    private static ServiceUser serviceUser;
    private static int testUserId = -1;

    @BeforeAll
    static void setUp() {
        serviceUser = new ServiceUser();
    }

    @Test
    @Order(1)
    void testAjouter() throws SQLException {
        User user = new User("Test", "User", "testuser@smartfight.com", "password123", "0112223333", 1, true);
        serviceUser.ajouter(user);
        List<User> users = serviceUser.recuperer();
        boolean found = false;
        for (User u : users) {
            if ("testuser@smartfight.com".equals(u.getEmail())) {
                testUserId = u.getId();
                found = true;
                break;
            }
        }
        assertTrue(found, "Test user should have been inserted.");
    }

    @Test
    @Order(2)
    void testRecuperer() throws SQLException {
        List<User> users = serviceUser.recuperer();
        assertNotNull(users, "User list should not be null.");
        assertFalse(users.isEmpty(), "User list should not be empty.");
    }

    @Test
    @Order(3)
    void testModifier() throws SQLException {
        Assumptions.assumeTrue(testUserId > 0, "Skip update: test user was not inserted.");
        User user = new User(testUserId, "Updated", "User", "testuser@smartfight.com", "newpassword", "0199999999", 1,
                true);
        serviceUser.modifier(user);
        List<User> users = serviceUser.recuperer();
        boolean updated = false;
        for (User u : users) {
            if ("testuser@smartfight.com".equals(u.getEmail()) && "Updated".equals(u.getFirstName())) {
                updated = true;
                break;
            }
        }
        assertTrue(updated, "Test user should have been updated.");
    }

    @Test
    @Order(4)
    void testSupprimer() throws SQLException {
        Assumptions.assumeTrue(testUserId > 0, "Skip delete: test user was not inserted.");
        User user = new User();
        user.setId(testUserId);
        serviceUser.supprimer(user);
        List<User> users = serviceUser.recuperer();
        boolean stillExists = false;
        for (User u : users) {
            if (u.getId() == testUserId) {
                stillExists = true;
                break;
            }
        }
        assertFalse(stillExists, "Test user should have been deleted.");
    }
}
