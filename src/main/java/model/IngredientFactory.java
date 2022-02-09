package model;

import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IngredientFactory implements Factory {

    private static final IngredientFactory instance = new IngredientFactory();
    private static Database db;

    public IngredientFactory() {
        db = Database.getInstance();
    }

    public IngredientFactory(Database db) {
        IngredientFactory.db = db;
    }

    public static IngredientFactory getInstance() {
        return instance;
    }

    public void createIngredient(String name, int unlockTier) {
        try {
            String sql = "INSERT INTO Ingredient (name, Tier_idTier, picture_raw, picture_raw_distraction, picture_processed, picture_baked, picture_burnt) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = db.conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setInt(2, unlockTier);

            // Define default pictures
            for (int i = 3; i < 8; i++) {
                File defaultImageFile = new File("src/main/resources/images/default_ingredient.png");
                InputStream inputStream = new FileInputStream(defaultImageFile);
                InputStream defaultImage_stream = new BufferedInputStream(inputStream);
                stmt.setBinaryStream(i, defaultImage_stream);
                inputStream.close();
                defaultImage_stream.close();
            }

            stmt.execute();

            // Add default description
            stmt = db.conn.prepareStatement("SELECT LAST_INSERT_ID()");
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int lastId = rs.getInt("LAST_INSERT_ID()");
            MemoryFactory.getInstance().addMemoryToIngredient("default description", lastId);

            rs.close();
            stmt.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
    }

    public Ingredient getIngredientById(int id) {
        Ingredient ingredient = null;
        try {
            String query = "SELECT * FROM Ingredient Where idIngredient = ? ";
            PreparedStatement stmt = db.conn.prepareStatement(query);
            stmt.setInt(1, id);
            //stmt.execute();
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ingredient = new Ingredient(rs);
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ingredient;
    }

    public List<Ingredient> getAllIngredients() {
        ArrayList<Ingredient> allIngredients = new ArrayList<>();
        try {
            String query = "SELECT * FROM Ingredient";
            Statement stmt = db.conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Ingredient ingredient = new Ingredient(rs);
                allIngredients.add(ingredient);
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
        return allIngredients;
    }

    public Map<String, InputStream> getIngredientPicturesById(int ingredientId) {
        HashMap<String, InputStream> pictureMap = new HashMap<>();
        pictureMap.put("raw", null);
        pictureMap.put("distraction", null);
        pictureMap.put("processed", null);
        pictureMap.put("baked", null);
        pictureMap.put("burned", null);
        try {
            String query = "SELECT * FROM Ingredient Where idIngredient = ? ";
            PreparedStatement stmt = db.conn.prepareStatement(query);
            stmt.setInt(1, ingredientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pictureMap.put("raw", rs.getBinaryStream("picture_raw"));
                pictureMap.put("distraction", rs.getBinaryStream("picture_raw_distraction"));
                pictureMap.put("processed", rs.getBinaryStream("picture_processed"));
                pictureMap.put("baked", rs.getBinaryStream("picture_baked"));
                pictureMap.put("burned", rs.getBinaryStream("picture_burnt"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return pictureMap;
    }

    public void saveIngredientPicture(int ingredientId, InputStream picture, String pictureType) {
        try {
            String sql = "";
            //picture db column name
            switch (pictureType) {
                case "raw":
                    sql = "Update Ingredient set picture_raw = ? where idIngredient = ?";
                    break;
                case "distraction":
                    sql = "Update Ingredient set picture_raw_distraction = ? where idIngredient = ?";
                    break;
                case "processed":
                    sql = "Update Ingredient set picture_processed = ? where idIngredient = ?";
                    break;
                case "baked":
                    sql = "Update Ingredient set picture_baked = ? where idIngredient = ?";
                    break;
                case "burned":
                    sql = "Update Ingredient set picture_burnt = ? where idIngredient = ?";
                    break;
            }
            PreparedStatement stmt = db.conn.prepareStatement(sql);
            InputStream photoStream = new BufferedInputStream(picture);
            stmt.setBinaryStream(1, photoStream, photoStream.available());
            stmt.setInt(2, ingredientId);
            try {
                stmt.executeUpdate();
            } catch (MysqlDataTruncation large) {
                var alert = new Alert(Alert.AlertType.ERROR, "Your image is too large.\nPlease try to use a smaller image. Maximum size is 16MB", ButtonType.OK);
                alert.show();
            }
            stmt.close();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
    }

    public Object checkSpecificBehavior(int ingredientId) {
        Object object;
        object = getFlightBehaviorByIngredientId(ingredientId);
        if (object != null) {
            return object;
        }
        object = getStampBehaviorByIngredientId(ingredientId);
        return object;
    }

    public FlightBehavior getFlightBehaviorByIngredientId(int ingredientId) {
        FlightBehavior flightBehavior = null;
        try {
            String query = "SELECT * FROM FlightBehavior WHERE Ingredient_fk = ?";
            PreparedStatement stmt = db.conn.prepareStatement(query);
            stmt.setInt(1, ingredientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                flightBehavior = new FlightBehavior(rs);
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return flightBehavior;
    }

    public void createFlightBehavior(int ingredientId) {
        try {
            String sql = "insert into FlightBehavior (Ingredient_fk) VALUES (?)";
            PreparedStatement stmt = db.conn.prepareStatement(sql);
            stmt.setInt(1, ingredientId);
            stmt.execute();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public StampBehavior getStampBehaviorByIngredientId(int ingredientId) {
        StampBehavior stampBehavior = null;
        try {
            String query = "SELECT * FROM StampBehavior WHERE Ingredient_fk = ?";
            PreparedStatement stmt = db.conn.prepareStatement(query);
            stmt.setInt(1, ingredientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                stampBehavior = new StampBehavior(rs);
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return stampBehavior;
    }

    public void createStampBehavior(int ingredientId) {
        try {
            String sql = "insert into StampBehavior (Ingredient_fk) VALUES (?)";
            PreparedStatement stmt = db.conn.prepareStatement(sql);
            stmt.setInt(1, ingredientId);
            stmt.execute();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static class Ingredient {
        private final int id;
        private String name;
        private int idTier;
        private int zIndex;

        public Ingredient(int id, String name, int idTier, int zIndex) {
            this.id = id;
            this.name = name;
            this.idTier = idTier;
            this.zIndex = zIndex;
        }

        public Ingredient(ResultSet rs) throws SQLException {
            this.id = rs.getInt("idIngredient");
            this.name = rs.getString("name");
            this.idTier = rs.getInt("Tier_idTier");
            this.zIndex = rs.getInt("zIndex");
        }

        public void save() {
            try {
                String sql = "Update Ingredient set name = ?, Tier_idTier = ?, zIndex = ? where idIngredient = ?";
                PreparedStatement stmt = db.conn.prepareStatement(sql);
                stmt.setString(1, this.getName());
                stmt.setInt(2, this.getIdTier());
                stmt.setInt(3, this.getzIndex());
                stmt.setInt(4, this.getId());
                stmt.executeUpdate();
                stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        public void delete() {
            try {
                String sql = "DELETE FROM Ingredient where idIngredient = ?";
                PreparedStatement stmt = db.conn.prepareStatement(sql);
                stmt.setInt(1, this.getId());
                stmt.execute();
                stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
            save();
        }

        public int getIdTier() {
            return idTier;
        }

        public void setUnlockTier(int idTier) {
            this.idTier = idTier;
            save();
        }

        public int getzIndex() {
            return zIndex;
        }

        public void setZIndex(int zIndex) {
            this.zIndex = zIndex;
            save();
        }
    }

    public static class FlightBehavior {
        private int idFB;
        private int x;
        private int y;
        private double speed;
        private int rotation;
        private int hits;
        private int idIngredient;

        public FlightBehavior(int idFB, int x, int y, double speed, int rotation, int hits, int idIngredient) {
            this.idFB = idFB;
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.rotation = rotation;
            this.hits = hits;
            this.idIngredient = idIngredient;
        }

        public FlightBehavior(ResultSet rs) throws SQLException {
            this.idFB = rs.getInt("idFlightBehavior");
            this.x = rs.getInt("vertex_x_inPercent");
            this.y = rs.getInt("vertex_y_inPercent");
            this.speed = rs.getDouble("speed");
            this.rotation = rs.getInt("rotation");
            this.hits = rs.getInt("hits_required");
            this.idIngredient = rs.getInt("Ingredient_fk");
        }

        public void save() {
            try {
                String query = "UPDATE FlightBehavior set vertex_x_inPercent = ?, vertex_y_inPercent = ?, speed = ?, rotation = ?, hits_required = ? where Ingredient_fk = ?";
                PreparedStatement stmt = db.conn.prepareStatement(query);
                stmt.setInt(1, getX());
                stmt.setInt(2, getY());
                stmt.setDouble(3, getSpeed());
                stmt.setInt(4, getRotation());
                stmt.setInt(5, getHits());
                stmt.setInt(6, getIdIngredient());
                stmt.executeUpdate();
                stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        public void delete() {
            try {
                String sql = "DELETE FROM FlightBehavior where Ingredient_fk = ?";
                PreparedStatement stmt = db.conn.prepareStatement(sql);
                stmt.setInt(1, this.getIdIngredient());
                stmt.execute();
                stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        public int getIdFB() {
            return idFB;
        }

        public void setIdFB(int idFB) {
            this.idFB = idFB;
            save();
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
            save();
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
            save();
        }

        public double getSpeed() {
            return speed;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
            save();
        }

        public int getRotation() {
            return rotation;
        }

        public void setRotation(int rotation) {
            this.rotation = rotation;
            save();
        }

        public int getHits() {
            return hits;
        }

        public void setHits(int hits) {
            this.hits = hits;
            save();
        }

        public int getIdIngredient() {
            return idIngredient;
        }

        public void setIdIngredient(int idIngredient) {
            this.idIngredient = idIngredient;
            save();
        }
    }

    public static class StampBehavior {
        int idSB;
        int displayTime;
        int disablingTime;
        int hits;
        int idIngredient;

        public StampBehavior(int idSB, int displayTime, int disablingTime, int hits, int idIngredient) {
            this.idSB = idSB;
            this.displayTime = displayTime;
            this.disablingTime = disablingTime;
            this.hits = hits;
            this.idIngredient = idIngredient;
        }

        public StampBehavior(ResultSet rs) throws SQLException {
            this.idSB = rs.getInt("idStampBehavior");
            this.displayTime = rs.getInt("display_time");
            this.disablingTime = rs.getInt("disabling_time");
            this.hits = rs.getInt("hits_required");
            this.idIngredient = rs.getInt("Ingredient_fk");
        }

        public void save() {
            try {
                String query = "UPDATE StampBehavior set display_time = ?, disabling_time = ?, hits_required = ? where Ingredient_fk = ?";
                PreparedStatement stmt = db.conn.prepareStatement(query);
                stmt.setInt(1, getDisplayTime());
                stmt.setInt(2, getDisablingTime());
                stmt.setInt(3, getHits());
                stmt.setInt(4, getIdIngredient());
                stmt.executeUpdate();
                stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        public void delete() {
            try {
                String sql = "DELETE FROM StampBehavior where Ingredient_fk = ?";
                PreparedStatement stmt = db.conn.prepareStatement(sql);
                stmt.setInt(1, this.getIdIngredient());
                stmt.execute();
                stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        public int getIdSB() {
            return idSB;
        }

        public void setIdSB(int idSB) {
            this.idSB = idSB;
            save();
        }

        public int getDisplayTime() {
            return displayTime;
        }

        public void setDisplayTime(int displayTime) {
            this.displayTime = displayTime;
            save();
        }

        public int getDisablingTime() {
            return disablingTime;
        }

        public void setDisablingTime(int disablingTime) {
            this.disablingTime = disablingTime;
            save();
        }

        public int getHits() {
            return hits;
        }

        public void setHits(int hits) {
            this.hits = hits;
            save();
        }

        public int getIdIngredient() {
            return idIngredient;
        }

        public void setIdIngredient(int idIngredient) {
            this.idIngredient = idIngredient;
            save();
        }
    }

}
