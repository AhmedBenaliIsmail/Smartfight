package entities;

public class Coach {
    private int id;
    private int userId;
    private String speciality;
    private int experienceYears;
    private String certification;
    private String status;

    public Coach() {
    }

    public Coach(int userId, String speciality, int experienceYears, String certification, String status) {
        this.userId = userId;
        this.speciality = speciality;
        this.experienceYears = experienceYears;
        this.certification = certification;
        this.status = status;
    }

    public Coach(int id, int userId, String speciality, int experienceYears, String certification, String status) {
        this.id = id;
        this.userId = userId;
        this.speciality = speciality;
        this.experienceYears = experienceYears;
        this.certification = certification;
        this.status = status;
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

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getCertification() {
        return certification;
    }

    public void setCertification(String certification) {
        this.certification = certification;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Coach{id=" + id + ", userId=" + userId + ", speciality='" + speciality +
                "', experienceYears=" + experienceYears + ", status='" + status + "'}";
    }
}
