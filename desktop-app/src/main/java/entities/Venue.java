package entities;

public class Venue {
    private int id;
    private String name;
    private String address;
    private String city;
    private String country;
    private int capacity;
    private String contactEmail;
    private String contactPhone;

    public Venue() {
    }

    public Venue(String name, String address, String city, String country,
            int capacity, String contactEmail, String contactPhone) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.country = country;
        this.capacity = capacity;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
    }

    public Venue(int id, String name, String address, String city, String country,
            int capacity, String contactEmail, String contactPhone) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.country = country;
        this.capacity = capacity;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    @Override
    public String toString() {
        return "Venue{id=" + id + ", name='" + name + "', city='" + city + "', country='" + country + "', capacity="
                + capacity + "}";
    }
}
