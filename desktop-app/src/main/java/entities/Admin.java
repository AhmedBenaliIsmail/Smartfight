package entities;

public class Admin {
    private int id;
    private int userId;
    private String accessLevel;
    private String department;

    public Admin() {
    }

    public Admin(int userId, String accessLevel, String department) {
        this.userId = userId;
        this.accessLevel = accessLevel;
        this.department = department;
    }

    public Admin(int id, int userId, String accessLevel, String department) {
        this.id = id;
        this.userId = userId;
        this.accessLevel = accessLevel;
        this.department = department;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "Admin{id=" + id + ", userId=" + userId + ", accessLevel='" + accessLevel + "', department='"
                + department + "'}";
    }
}
