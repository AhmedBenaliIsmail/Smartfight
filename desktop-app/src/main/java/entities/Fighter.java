package entities;

import java.time.LocalDate;

public class Fighter {
    private int id;
    private int userId;
    private String nickname;
    private LocalDate dateOfBirth;
    private String nationality;
    private int weightClassId;
    private int wins;
    private int losses;
    private int draws;
    private String status;

    public Fighter() {
    }

    public Fighter(int userId, String nickname, LocalDate dateOfBirth, String nationality,
            int weightClassId, int wins, int losses, int draws, String status) {
        this.userId = userId;
        this.nickname = nickname;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
        this.weightClassId = weightClassId;
        this.wins = wins;
        this.losses = losses;
        this.draws = draws;
        this.status = status;
    }

    public Fighter(int id, int userId, String nickname, LocalDate dateOfBirth, String nationality,
            int weightClassId, int wins, int losses, int draws, String status) {
        this.id = id;
        this.userId = userId;
        this.nickname = nickname;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
        this.weightClassId = weightClassId;
        this.wins = wins;
        this.losses = losses;
        this.draws = draws;
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public int getWeightClassId() {
        return weightClassId;
    }

    public void setWeightClassId(int weightClassId) {
        this.weightClassId = weightClassId;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Fighter{id=" + id + ", userId=" + userId + ", nickname='" + nickname +
                "', nationality='" + nationality + "', status='" + status + "'}";
    }
}
