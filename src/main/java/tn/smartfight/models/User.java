package tn.smartfight.models;

public class User {

    private int     id;
    private String  firstName;
    private String  lastName;
    private String  email;
    private String  phone;
    private int     roleId;
    private boolean isActive;
    private String  roleName;

    public User() {}

    public User(int id, String firstName, String lastName, String email,
                String phone, int roleId, boolean isActive) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.roleId = roleId;
        this.isActive = isActive;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    public String getFullName() { return firstName + " " + lastName; }

    public boolean isAdmin()     { return roleId == 1; }
    public boolean isFan()       { return roleId == 5; }

    @Override
    public String toString() { return getFullName(); }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int getId()              { return id; }
    public void setId(int id)       { this.id = id; }

    public String getFirstName()                   { return firstName; }
    public void setFirstName(String firstName)     { this.firstName = firstName; }

    public String getLastName()                    { return lastName; }
    public void setLastName(String lastName)       { this.lastName = lastName; }

    public String getEmail()               { return email; }
    public void setEmail(String email)     { this.email = email; }

    public String getPhone()               { return phone; }
    public void setPhone(String phone)     { this.phone = phone; }

    public int getRoleId()                 { return roleId; }
    public void setRoleId(int roleId)      { this.roleId = roleId; }

    public boolean isActive()              { return isActive; }
    public void setActive(boolean active)  { isActive = active; }

    public String getRoleName()                    { return roleName; }
    public void setRoleName(String roleName)       { this.roleName = roleName; }
}
