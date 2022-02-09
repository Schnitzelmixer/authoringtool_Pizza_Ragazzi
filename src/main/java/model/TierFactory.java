package model;

import javafx.scene.control.Alert;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TierFactory implements Factory {

    private static final TierFactory instance = new TierFactory();
    private static final Database db = Database.getInstance();

    public static TierFactory getInstance() {
        return instance;
    }

    public void createTier(String name, int totalPoints) {
        try {
            String sql = "INSERT INTO Tier (name, gesamtpunkte) VALUES (?, ?)";
            PreparedStatement stmt = db.conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setInt(2, totalPoints);
            stmt.execute();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public Tier getTierById(int idTier) {
        TierFactory.Tier tier = null;
        try {
            String query = "SELECT * FROM Tier Where idTier = ? ";
            PreparedStatement stmt = db.conn.prepareStatement(query);
            stmt.setInt(1, idTier);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tier = new TierFactory.Tier(rs);
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return tier;
    }

    public List<Tier> getAllTiers() {
        ArrayList<TierFactory.Tier> allTiers = new ArrayList<>();
        try {
            String query = "SELECT * FROM Tier";
            Statement stmt = db.conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                TierFactory.Tier tier = new TierFactory.Tier(rs);
                allTiers.add(tier);
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
        return allTiers;
    }

    public static class Tier {
        private int idTier;
        private String name;
        private int totalPoints;

        public Tier(int idTier, String name, int totalPoints) {
            this.idTier = idTier;
            this.name = name;
            this.totalPoints = totalPoints;
        }

        public Tier(ResultSet rs) throws SQLException {
            this.idTier = rs.getInt("idTier");
            this.name = rs.getString("name");
            this.totalPoints = rs.getInt("gesamtpunkte");
        }

        public void save() {
            try {
                String sql = "Update Tier set name = ?, gesamtpunkte = ? where idTier = ?";
                PreparedStatement stmt = db.conn.prepareStatement(sql);
                stmt.setString(1, this.getName());
                stmt.setInt(2, this.getTotalPoints());
                stmt.setInt(3, this.getIdTier());
                stmt.executeUpdate();
                stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        public void delete() {
            try {
                String sql = "DELETE FROM Tier where idTier = ?";
                PreparedStatement stmt = db.conn.prepareStatement(sql);
                stmt.setInt(1, this.getIdTier());
                stmt.execute();
                stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        public int getIdTier() {
            return idTier;
        }

        public void setIdTier(int idTier) {
            try {
                String sql = "Update Tier set idTier = ? where idTier = ?";
                PreparedStatement stmt = db.conn.prepareStatement(sql);
                stmt.setInt(1, idTier);
                stmt.setInt(2, getIdTier());
                stmt.executeUpdate();
                stmt.close();
            } catch (SQLIntegrityConstraintViolationException duplicate) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText("This tier already exists!\nChoose a different one!");
                alert.show();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            this.idTier = idTier;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
            save();
        }

        public int getTotalPoints() {
            return totalPoints;
        }

        public void setTotalPoints(int totalPoints) {
            this.totalPoints = totalPoints;
            save();
        }
    }
}
