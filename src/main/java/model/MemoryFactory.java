package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MemoryFactory implements Factory {

    private static final MemoryFactory instance = new MemoryFactory();
    private static final Database db = Database.getInstance();

    public static MemoryFactory getInstance() {
        return instance;
    }


    public List<Memory> getAllMemoriesByIngredientId(int ingredientId) {
        List<Memory> memories = new ArrayList<>();

        try {
            String query = "SELECT * FROM Memory WHERE Ingredient_fk = ?";
            PreparedStatement stmt = db.conn.prepareStatement(query);
            stmt.setInt(1, ingredientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                memories.add(new Memory(rs));
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return memories;
    }

    public void addMemoryToIngredient(String description, int idIngredient) {
        try {
            String query = "INSERT INTO Memory (description, Ingredient_fk) VALUES (?, ?)";
            PreparedStatement stmt = db.conn.prepareStatement(query);
            stmt.setString(1, description);
            stmt.setInt(2, idIngredient);
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static class Memory {
        private int idMem;
        private String memoryDescription;
        private int idIngredient;

        public Memory(int idMem, String memoryDescription, int idIngredient) {
            this.idMem = idMem;
            this.memoryDescription = memoryDescription;
            this.idIngredient = idIngredient;
        }

        public Memory(ResultSet rs) throws SQLException {
            this.idMem = rs.getInt("idMemory");
            this.memoryDescription = rs.getString("description");
            this.idIngredient = rs.getInt("Ingredient_fk");
        }

        public void save() {
            try {
                String query = "UPDATE Memory SET description = ?, Ingredient_fk = ? WHERE idMemory = ?";
                PreparedStatement stmt = db.conn.prepareStatement(query);
                stmt.setString(1, getMemoryDescription());
                stmt.setInt(2, getIdIngredient());
                stmt.setInt(3, getIdMem());

                stmt.executeUpdate();
                stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        public void delete() {
            try {
                String sql = "DELETE FROM Memory where idMemory = ?";
                PreparedStatement stmt = db.conn.prepareStatement(sql);
                stmt.setInt(1, this.getIdMem());
                stmt.execute();
                stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        public int getIdMem() {
            return idMem;
        }

        public void setIdMem(int idMem) {
            this.idMem = idMem;
            save();
        }

        public String getMemoryDescription() {
            return memoryDescription;
        }

        public void setMemoryDescription(String memoryDescription) {
            this.memoryDescription = memoryDescription;
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
