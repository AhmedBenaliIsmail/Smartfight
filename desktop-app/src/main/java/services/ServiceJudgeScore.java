package services;

import entities.JudgeScore;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceJudgeScore implements IService<JudgeScore> {

    private Connection connection;

    public ServiceJudgeScore() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(JudgeScore js) throws SQLException {
        String req = "INSERT INTO judge_score (fight_result_id, judge_name, score_red, score_blue) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, js.getFightResultId());
        ps.setString(2, js.getJudgeName());
        ps.setInt(3, js.getScoreRed());
        ps.setInt(4, js.getScoreBlue());
        ps.executeUpdate();
        System.out.println("JudgeScore added: judge=" + js.getJudgeName());
    }

    @Override
    public void modifier(JudgeScore js) throws SQLException {
        String req = "UPDATE judge_score SET fight_result_id=?, judge_name=?, score_red=?, score_blue=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, js.getFightResultId());
        ps.setString(2, js.getJudgeName());
        ps.setInt(3, js.getScoreRed());
        ps.setInt(4, js.getScoreBlue());
        ps.setInt(5, js.getId());
        ps.executeUpdate();
        System.out.println("JudgeScore updated: id=" + js.getId());
    }

    @Override
    public void supprimer(JudgeScore js) throws SQLException {
        String req = "DELETE FROM judge_score WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, js.getId());
        ps.executeUpdate();
        System.out.println("JudgeScore deleted: id=" + js.getId());
    }

    @Override
    public List<JudgeScore> recuperer() throws SQLException {
        List<JudgeScore> list = new ArrayList<>();
        String req = "SELECT * FROM judge_score";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new JudgeScore(
                    rs.getInt("id"),
                    rs.getInt("fight_result_id"),
                    rs.getString("judge_name"),
                    rs.getInt("score_red"),
                    rs.getInt("score_blue")));
        }
        return list;
    }
}
