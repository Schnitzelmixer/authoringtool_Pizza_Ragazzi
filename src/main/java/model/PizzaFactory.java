package model;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PizzaFactory implements Factory {

    private static final PizzaFactory instance = new PizzaFactory();
    private static final Database db = Database.getInstance();

    public static PizzaFactory getInstance() {
        return instance;
    }


    public Map<Integer, InputStream> getPizzaPicturesById(int pizzaId) {
        HashMap<Integer, InputStream> pictureMap = new HashMap<>();

        try {
            String query = "SELECT zIndex, picture_baked FROM Ingredient " +
                    "JOIN Pizza_has_Ingredient PhI on Ingredient.idIngredient = PhI.Ingredient_idIngredient " +
                    "WHERE Pizza_idPizza = ?";
            PreparedStatement stmt = db.conn.prepareStatement(query);
            stmt.setInt(1, pizzaId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pictureMap.put(rs.getInt("zIndex"), rs.getBinaryStream("picture_baked"));
            }
            rs.close();
            stmt.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return pictureMap;
    }

    public List<Pizza> getAllPizzas() {

        ArrayList<Pizza> allPizzas = new ArrayList<>();
        try {
            String query = "SELECT * FROM Pizza";
            Statement stmt = db.conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Pizza pizza = new Pizza(rs);
                allPizzas.add(pizza);
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
        return allPizzas;
    }

    // TODO: Update Pizza_has_Ingredient
    public void createPizza(String name, int points, int order_time) {
        try {
            String sql = "INSERT INTO Pizza (name, points, order_time) VALUES (?, ?, ?)";
            PreparedStatement stmt = db.conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setInt(2, points);
            stmt.setInt(3, order_time);
            stmt.execute();
            stmt.close();

            // Add impasto as ingredient
            stmt = db.conn.prepareStatement("INSERT INTO Pizza_has_Ingredient (Pizza_idPizza, Ingredient_idIngredient) VALUES (LAST_INSERT_ID(), (SELECT idIngredient FROM Ingredient WHERE idIngredient = 1))");
            stmt.execute();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        //addIngredientToPizza(1, );
    }

    public Pizza getPizzaById(int id) {
        Pizza pizza = null;
        try {
            String query = "SELECT * FROM Pizza Where idPizza = ? ";
            PreparedStatement stmt = db.conn.prepareStatement(query);
            stmt.setInt(1, id);
            stmt.execute();
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pizza = new Pizza(rs);
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return pizza;
    }

    public boolean isIngredientOnPizza(int idIngredient, int idPizza) {
        try {
            String query = "SELECT * FROM Pizza_has_Ingredient Where Pizza_idPizza = ? ";
            PreparedStatement stmt = db.conn.prepareStatement(query);
            stmt.setInt(1, idPizza);
            stmt.execute();
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                if (rs.getInt("Ingredient_idIngredient") == idIngredient) {
                    return true;
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public List<IngredientFactory.Ingredient> getIngredientsOfPizza(int idPizza) {
        ArrayList<IngredientFactory.Ingredient> ret = new ArrayList<>();
        try {
            String query = "SELECT * FROM Pizza_has_Ingredient JOIN Ingredient I on Pizza_has_Ingredient.Ingredient_idIngredient = I.idIngredient " +
                    "Where Pizza_has_Ingredient.Pizza_idPizza = ? ";
            PreparedStatement stmt = db.conn.prepareStatement(query);
            stmt.setInt(1, idPizza);
            stmt.execute();
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ret.add(new IngredientFactory.Ingredient(rs));
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public void addIngredientToPizza(int idIngredient, int idPizza) {
        try {
            String sql = "INSERT INTO Pizza_has_Ingredient (Pizza_idPizza, Ingredient_idIngredient) VALUES (?, ?)";
            PreparedStatement stmt = db.conn.prepareStatement(sql);
            stmt.setInt(1, idPizza);
            stmt.setInt(2, idIngredient);
            stmt.execute();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void removeIngredientFromPizza(int idIngredient, int idPizza) {
        try {
            String sql = "DELETE FROM Pizza_has_Ingredient WHERE Pizza_idPizza = ? AND Ingredient_idIngredient = ?";
            PreparedStatement stmt = db.conn.prepareStatement(sql);
            stmt.setInt(1, idPizza);
            stmt.setInt(2, idIngredient);
            stmt.execute();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static class Pizza {

        int id;
        String name;
        int points;
        int order_time;

        public Pizza(int id, String name, int points, int order_time) {
            this.id = id;
            this.name = name;
            this.points = points;
            this.order_time = order_time;
        }

        public Pizza(String name, int points, int order_time) {
            this.name = name;
            this.points = points;
            this.order_time = order_time;
        }

        public Pizza(ResultSet rs) throws SQLException {

            this.id = rs.getInt("idPizza");
            this.name = rs.getString("name");
            this.points = rs.getInt("points");
            this.order_time = rs.getInt("order_time");
        }


        public void save() {
            try {
                String sql = "Update Pizza set name = ?, points = ?, order_time = ? where idPizza = ?";
                PreparedStatement stmt = db.conn.prepareStatement(sql);
                stmt.setString(1, this.getName());
                stmt.setInt(2, this.getPoints());
                stmt.setInt(3, this.getOrder_time());
                stmt.setInt(4, this.getId());
                stmt.executeUpdate();
                stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        public void delete() {
            try {
                String sql = "DELETE FROM Pizza where idPizza = ?";
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

        public void setId(int id) {
            this.id = id;
            save();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
            save();
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
            save();
        }

        public int getOrder_time() {
            return order_time;
        }

        public void setOrder_time(int order_time) {
            this.order_time = order_time;
            save();
        }
    }
}
