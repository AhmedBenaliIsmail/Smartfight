package entities;

public class FanProfile {
    private int id;
    private int userId;
    private String favoriteSport;
    private String country;
    private String bio;

    public FanProfile() {
    }

    public FanProfile(int userId, String favoriteSport, String country, String bio) {
        this.userId = userId;
        this.favoriteSport = favoriteSport;
        this.country = country;
        this.bio = bio;
    }

    public FanProfile(int id, int userId, String favoriteSport, String country, String bio) {
        this.id = id;
        this.userId = userId;
        this.favoriteSport = favoriteSport;
        this.country = country;
        this.bio = bio;
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

    public String getFavoriteSport() {
        return favoriteSport;
    }

    public void setFavoriteSport(String favoriteSport) {
        this.favoriteSport = favoriteSport;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @Override
    public String toString() {
        return "FanProfile{id=" + id + ", userId=" + userId + ", favoriteSport='" + favoriteSport + "', country='"
                + country + "'}";
    }
}
