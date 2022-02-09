package model;

import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import model.factoryExceptions.ProfilePictureException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserFactory implements Factory {

    private static final UserFactory instance = new UserFactory();
    private static final Database db = Database.getInstance();

    public static UserFactory getInstance() {
        return instance;
    }


    /**
     * Updates the user if it already exists and creates it otherwise. Assumes an
     * autoincrement id column.
     */
    public void saveUser(User user) throws SQLException {
        String sql = "UPDATE User SET username = ?, email = ?, password = ?, gesamtpunkte = ?, highscore = ?, Tier_idTier = ? WHERE idUser = ?";
        PreparedStatement stmt = db.conn.prepareStatement(sql);
        stmt.setString(1, user.getUsername());
        stmt.setString(2, user.getEmail());
        stmt.setString(3, user.getPassword());
        stmt.setInt(4, user.getTotalPoints());
        stmt.setInt(5, user.getHighScore());
        stmt.setInt(6, user.getCurrentTier());
        stmt.setInt(7, user.getId());
        stmt.executeUpdate();
        stmt.close();
    }

    public void deleteUser(User user) throws SQLException {
        String sql = "DELETE FROM `User` WHERE idUser = ?";
        PreparedStatement stmt = db.conn.prepareStatement(sql);
        stmt.setInt(1, user.getId());
        stmt.executeUpdate();
        stmt.close();
    }

    public void addUser(String username, String email, String password, int gesamtpunkte, int highscore, int tierId) throws SQLException {
        String sql = "INSERT INTO `User`(username, email, password, gesamtpunkte, highscore, Tier_idTier) VALUES (?,?,?,?,?,?)";
        PreparedStatement stmt = db.conn.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, email);
        stmt.setString(3, password);
        stmt.setInt(4, gesamtpunkte);
        stmt.setInt(5, highscore);
        stmt.setInt(6, tierId);
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Retrieves a user from database with given ID
     *
     * @param id id of user to find
     * @return User if found, else null
     */
    public User getUserById(int id) throws SQLException {
        User user = null;
        PreparedStatement stmt = db.conn.prepareStatement("SELECT * FROM User WHERE idUser = ?");
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            user = new User(rs);
        }
        rs.close();
        stmt.close();
        return user;
    }

    /**
     * Retrieves a user from database with given Username
     *
     * @param username of user to find
     * @return User if found, else null
     */
    public User getUserByUsername(String username) throws SQLException {
        User user = null;
        PreparedStatement stmt = db.conn.prepareStatement("SELECT * FROM User WHERE username = ?");
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            user = new User(rs);
        }
        rs.close();
        stmt.close();
        return user;
    }

    /**
     * Gets friends of user as List.
     *
     * @return the friends-List
     */
    public List<User> getFriends(User user) throws SQLException {
        List<User> result = new ArrayList<>();
        String sql = "SELECT * FROM `Friendship` WHERE User_idUser_One = ? OR User_idUser_Two = ?";
        PreparedStatement stmt = db.conn.prepareStatement(sql);
        stmt.setInt(1, user.getId());
        stmt.setInt(2, user.getId());
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {

            int friendId = (int) rs.getObject("User_idUser_one");
            if (friendId == user.getId()) {
                friendId = (int) rs.getObject("User_idUser_two");
            }
            User user1 = getUserById(friendId);
            result.add(user1);
        }
        rs.close();
        stmt.close();
        return result;
    }

    /**
     * Gets friends of user as List.
     *
     * @param id from user
     * @return the friends-List
     */
    public List<String> getFriendsNames(int id) throws SQLException {
        List<String> result = new ArrayList<>();
        String sql = "SELECT * FROM `Friendship` WHERE User_idUser_One = ? OR User_idUser_Two = ?";
        PreparedStatement stmt = db.conn.prepareStatement(sql);
        stmt.setInt(1, id);
        stmt.setInt(2, id);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            int friendId = (int) rs.getObject("User_idUser_one");
            if (friendId == id) {
                friendId = (int) rs.getObject("User_idUser_two");
            }
            User user1 = getUserById(friendId);
            result.add(user1.getUsername());
        }
        rs.close();
        stmt.close();
        return result;
    }

    /**
     * Removes a friend of a User
     *
     * @param user       User
     * @param friendName name of friend
     */
    public void removeFriend(User user, String friendName) throws SQLException {
        User friend = getUserByUsername(friendName);
        String sql = "DELETE FROM `Friendship` WHERE (User_idUser_one = ? AND User_idUser_two = ?) OR (User_idUser_one = ? AND User_idUser_two = ?)";
        PreparedStatement stmt = db.conn.prepareStatement(sql);
        stmt.setInt(1, user.getId());
        stmt.setInt(2, friend.getId());
        stmt.setInt(3, friend.getId());
        stmt.setInt(4, user.getId());
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Adds a friend to a User
     *
     * @param user       User
     * @param friendName name of friend
     * @return the boolean if it was successfull
     */
    public boolean addFriend(User user, String friendName) throws SQLException {
        User friend = getUserByUsername(friendName);

        if (friend == null) {
            return false;
        }
        if (user.getId() == friend.getId()) {
            return false;
        }

        List<User> friends = getFriends(user);
        boolean alreadyFriend = false;

        for (User user1 : friends) {         //checken ob sie schon befreundet sind
            if (user1.getId() == friend.getId()) {
                alreadyFriend = true;
                break;
            }
        }

        if (!alreadyFriend) {
            PreparedStatement stmt = db.conn.prepareStatement("INSERT INTO `Friendship` (User_idUser_one, User_idUser_two) VALUES (?, ?)");
            stmt.setInt(1, user.getId());
            stmt.setInt(2, friend.getId());
            stmt.executeUpdate();
            stmt.close();
            return true;
        }
        return false;
    }

    public List<User> getAllUsers() {

        List<User> allUsers = new ArrayList<>();

        try (Statement stmt = db.conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM User")) {

            while (rs.next()) {

                allUsers.add(new User(rs));
            }

            return allUsers;
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }

        return null;
    }

    public InputStream getProfilePictureById(int UserId) {
        InputStream image = null;
        try {
            String query = "SELECT * FROM `User` Where idUser = ? ";
            PreparedStatement stmt = db.conn.prepareStatement(query);
            stmt.setInt(1, UserId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                image = rs.getBinaryStream("profilepicture");
            }
            stmt.close();
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return image;
    }

    public void saveProfilePictureById(int userId, InputStream picture) {
        try {
            String sql = "Update `User` set profilepicture = ? where idUser = ?";
            PreparedStatement stmt = db.conn.prepareStatement(sql);
            InputStream photoStream = new BufferedInputStream(picture);
            stmt.setBinaryStream(1, photoStream, photoStream.available());
            stmt.setInt(2, userId);
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

    public static class User {
        private int id;
        private String username;
        private String email;
        private String password;
        private int totalPoints;
        private int highScore;
        private int currentTier;
        private BufferedImage profilePicture;


        public User(int id, String username, String email, String password, int totalPoints, int highScore, BufferedImage profilePicture, int currentTier) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.password = password;
            this.totalPoints = totalPoints;
            this.highScore = highScore;
            this.profilePicture = profilePicture;
            this.currentTier = currentTier;
        }

        public User(ResultSet rs) throws SQLException {
            this.id = rs.getInt("idUser");
            this.username = rs.getString("username");
            this.email = rs.getString("email");
            this.password = rs.getString("password");
            this.totalPoints = rs.getInt("gesamtpunkte");
            this.highScore = rs.getInt("highscore");
            BufferedInputStream bis = new BufferedInputStream(rs.getBinaryStream("profilepicture"));
            try {
                this.profilePicture = ImageIO.read(bis);
                bis.close();
                //this.profilePicture.set(ImageIO.read(bis));
            } catch (IOException invalidProfilePicture) {
                throw new ProfilePictureException("We had trouble getting the profile picture");
            }
            this.currentTier = rs.getInt("Tier_idTier");
        }

        public void save() throws SQLException {
            UserFactory.getInstance().saveUser(this);
        }

        public void delete() throws SQLException {
            UserFactory.getInstance().deleteUser(this);
        }

        public List<String> getFriendsNames() throws SQLException {
            return UserFactory.getInstance().getFriendsNames(this.getId());
        }

        public void removeFriend(String friendName) throws SQLException {
            UserFactory.getInstance().removeFriend(this, friendName);
        }

        public boolean addFriend(String friendName) throws SQLException {
            return UserFactory.getInstance().addFriend(this, friendName);
        }

        public int getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) throws SQLException {
            this.username = username;
            save();
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) throws SQLException {
            if (!email.matches("[a-zA-Z0-9._%+-]+[@]+[a-zA-Z0-9.-]+[.]+[a-zA-Z]{2,6}")) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Email " + email + " nicht valide!");
                alert.setHeaderText(null);
                alert.setContentText("Bitte korrekte Email eintragen.");

                alert.showAndWait();
            } else {
                this.email = email;
                save();
            }
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) throws SQLException {
            this.password = password;
            save();
        }

        public int getTotalPoints() {
            return totalPoints;
        }

        public void setTotalPoints(int totalPoints) throws SQLException {
            this.totalPoints = totalPoints;
            save();
        }

        public int getHighScore() {
            return highScore;
        }

        public void setHighScore(int highScore) throws SQLException {
            this.highScore = highScore;
            save();
        }

        public BufferedImage getProfilePicture() {
            return profilePicture;
        }

        public void setProfilePicture(BufferedImage profilePicture) throws SQLException {
            this.profilePicture = profilePicture;
            save();
        }

        public int getCurrentTier() {
            return currentTier;
        }

        public void setCurrentTier(int currentTier) throws SQLException {
            this.currentTier = currentTier;
            save();
        }

    }
}
