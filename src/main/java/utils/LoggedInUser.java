package utils;

public class LoggedInUser {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private int roleId;

    private static LoggedInUser instance;
    public static LoggedInUser getInstance() { return instance; }
    public static void setInstance(LoggedInUser user) { instance = user; }

    public LoggedInUser(int id, String firstName, String lastName,
                        String email, String role, int roleId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.roleId = roleId;
    }

    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public int getRoleId() { return roleId; }
    public String getFullName() { return firstName + " " + lastName; }
}
