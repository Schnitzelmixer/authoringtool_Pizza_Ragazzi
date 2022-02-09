package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import model.UserFactory;
import model.UserFactory.User;

import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;

/**
 * The type UserDataController.
 */
public class UserDataController implements Initializable {

    private final ObservableList<UserFactory.User> table_users_data = FXCollections.observableArrayList(); // Where table_users gets its data from
    private final ObservableList<String> list_users_friends = FXCollections.observableArrayList();
    public TableView<UserFactory.User> table_users;
    public TableColumn<UserFactory.User, Integer> column_idUser;
    public TableColumn<UserFactory.User, String> column_username;
    public TableColumn<UserFactory.User, String> column_email;
    public TableColumn<UserFactory.User, String> column_password;
    public TableColumn<UserFactory.User, Integer> column_totalPoints;
    public TableColumn<UserFactory.User, Integer> column_highscore;
    public TableColumn<UserFactory.User, Integer> column_idTier;
    public Button deleteButton;
    public Button newButton;
    public Button button_refresh;
    public Button button_menu;
    public Button deleteFriendButton;
    public Button addFriendButton;
    public Button buttonImportProfilePic;
    public ImageView imageProfilePic;
    UserFactory.User selectedUser;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private ListView<String> listView_friends; //fxml fx:id Listview
    @FXML
    private Label labelListView;
    @FXML
    private TextField addFriendInput;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        column_idUser.setCellValueFactory(new PropertyValueFactory<>("id"));
        column_username.setCellValueFactory(new PropertyValueFactory<>("username"));
        column_email.setCellValueFactory(new PropertyValueFactory<>("email"));
        column_password.setCellValueFactory(new PropertyValueFactory<>("password"));
        column_totalPoints.setCellValueFactory(new PropertyValueFactory<>("totalPoints"));
        column_highscore.setCellValueFactory(new PropertyValueFactory<>("highScore"));
        column_idTier.setCellValueFactory(new PropertyValueFactory<>("currentTier"));

        makeCellsEditable();
        setOnclickForTableToDisplayFriends();

        table_users.setItems(table_users_data); // Tells table, what data to display

        table_users_data.addAll(UserFactory.getInstance().getAllUsers()); // Fills table_users_data with content

        buttonImportProfilePic.setOnAction(
                e -> importProfilePicture()
        );
    }

    @FXML
    void goBack() {
        try {
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/view/mainMenu.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/stylesheets/LightMode.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showProfilePicture(InputStream image) {
        if (image != null) {
            imageProfilePic.setImage(new Image(image));
        } else {
            imageProfilePic.setImage(new Image(getClass().getResourceAsStream("/images/default_ingredient.png")));
        }
    }

    private void importProfilePicture() {
        if (selectedUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("No user selected");
            alert.setContentText("Please make sure to select a user first before importing a new image!");
            alert.show();
        } else {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("View jpg Files, select picture Profile Picture");
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
                fileChooser.getExtensionFilters().addAll(extFilterJPG);
                FileInputStream fileInputStream = new FileInputStream(fileChooser.showOpenDialog(null));
                UserFactory.getInstance().saveProfilePictureById(selectedUser.getId(), fileInputStream);
                fileInputStream.close();
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText("Sorry, We couldn't find your file!\nPlease make sure to choose a file that is present on your disk");
                alert.show();
            } catch (NullPointerException ignored) {
                // NullPointer is thrown when FileChooser is closed without selecting a file
            } catch (IOException e) {
                e.printStackTrace();
            }
            showProfilePicture(UserFactory.getInstance().getProfilePictureById(selectedUser.getId()));
        }
    }

    private void makeCellsEditable() {
        column_username.setCellFactory(TextFieldTableCell.forTableColumn());
        column_username.setOnEditCommit(event -> {
            try {
                (event.getTableView().getItems().get(event.getTablePosition().getRow())).setUsername(event.getNewValue());
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        column_email.setCellFactory(TextFieldTableCell.forTableColumn());
        column_email.setOnEditCommit(event -> {
            try {
                (event.getTableView().getItems().get(event.getTablePosition().getRow())).setEmail(event.getNewValue());
            } catch (SQLIntegrityConstraintViolationException duplicate) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText("This e-mail address already exists!\nChoose a different e-mail address.");
                alert.show();
                refreshData();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        column_password.setCellFactory(TextFieldTableCell.forTableColumn());
        column_password.setOnEditCommit(event -> {
            try {
                (event.getTableView().getItems().get(event.getTablePosition().getRow())).setPassword(event.getNewValue());
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        column_totalPoints.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        column_totalPoints.setOnEditCommit(event -> {
            try {
                (event.getTableView().getItems().get(event.getTablePosition().getRow())).setTotalPoints(event.getNewValue());
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        column_highscore.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        column_highscore.setOnEditCommit(event -> {
            try {
                (event.getTableView().getItems().get(event.getTablePosition().getRow())).setHighScore(event.getNewValue());
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        column_idTier.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        column_idTier.setOnEditCommit(event -> {
            try {
                (event.getTableView().getItems().get(event.getTablePosition().getRow())).setCurrentTier(event.getNewValue());
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    /**
     * adds Eventhandler to Table to display correct friends
     */
    public void setOnclickForTableToDisplayFriends() {
        table_users.setOnMouseClicked(mouseEvent -> {
            selectedUser = table_users.getSelectionModel().getSelectedItem();
            labelListView.setText(selectedUser.getUsername() + "'s Friends:");

            List<String> friendsNames = null;
            try {
                friendsNames = new ArrayList<>(selectedUser.getFriendsNames());
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            list_users_friends.clear();
            listView_friends.getItems().clear();

            assert friendsNames != null;
            list_users_friends.addAll(friendsNames);
            listView_friends.getItems().addAll(list_users_friends);

            showProfilePicture(UserFactory.getInstance().getProfilePictureById(selectedUser.getId()));
        });
    }

    @FXML
    public void addFriend() throws SQLException {
        UserFactory.User selectedUser = table_users.getSelectionModel().getSelectedItem();
        String inputFriendUsername = addFriendInput.getText();
        boolean successful = selectedUser.addFriend(inputFriendUsername);

        if (successful) {
            listView_friends.getItems().add(inputFriendUsername);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Freund konnte nicht hinzugefügt werden!");
            alert.setHeaderText(null);
            alert.setContentText("Bitte trage einen validen Username ein!");
            alert.showAndWait();
        }
    }

    @FXML
    public void deleteFriend() throws SQLException {
        if (listView_friends.getSelectionModel().getSelectedItem() != null) {
            UserFactory.User selectedUser = table_users.getSelectionModel().getSelectedItem();
            String selectedFriendUsername = listView_friends.getSelectionModel().getSelectedItem();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText(null);
            alert.setContentText("Are your sure you want to delete this?");
            Optional<ButtonType> action = alert.showAndWait();

            if (action.isPresent() && action.get() == ButtonType.OK) {
                selectedUser.removeFriend(selectedFriendUsername);
                listView_friends.getItems().removeIf(friendUsername -> friendUsername.equals(selectedFriendUsername)); //remove Friend of listView
            }
        }
    }

    @FXML
    void newUser() throws SQLException {
        // Create the custom dialog.
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Create a new User.");

        // Set the button types.
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField name = new TextField("username");
        TextField email = new TextField("email");
        TextField pw = new TextField("password");
        grid.add(new Label("Username:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("E-Mail:"), 0, 1);
        grid.add(email, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(pw, 1, 2);

        // Enable/Disable login button depending on whether the input is valid.
        Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        HashMap<String, Boolean> validInput = new HashMap<>();
        validInput.put("name", false);
        validInput.put("email", false);
        validInput.put("pw", false);
        name.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                validInput.put("name", false);
                createButton.setDisable(true);
            } else {
                validInput.put("name", true);
                if (validInput.get("name") && validInput.get("email") && validInput.get("pw")) {
                    createButton.setDisable(false);
                }
            }
        });
        email.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("[a-zA-Z0-9._%+-]+[@]+[a-zA-Z0-9.-]+[.]+[a-zA-Z]{2,6}")) {
                validInput.put("email", true);
                if (validInput.get("name") && validInput.get("email") && validInput.get("pw")) {
                    createButton.setDisable(false);
                }
            } else {
                validInput.put("email", false);
                createButton.setDisable(true);
            }
        });
        pw.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                validInput.put("pw", false);
                createButton.setDisable(true);
            } else {
                validInput.put("pw", true);
                if (validInput.get("name") && validInput.get("email") && validInput.get("pw")) {
                    createButton.setDisable(false);
                }
            }
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(name::requestFocus);

        // Convert the result when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new String[]{name.getText(), email.getText(), pw.getText()};
            }
            return null;
        });
        Optional<String[]> result = dialog.showAndWait();

        if (result.isPresent()) {
            String[] text = result.get();
            try {
                UserFactory.getInstance().addUser(text[0], text[1], text[2], 0, 0, 1);
            } catch (SQLIntegrityConstraintViolationException duplicate) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText("This user already exists!\nChoose a different e-mail address.");
                alert.show();
            }
            refreshData();
        }

    }

    public void deleteUser() throws SQLException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(null);
        alert.setContentText("Are your sure you want to delete this user?");
        Optional<ButtonType> action = alert.showAndWait();

        if (action.isPresent() && action.get() == ButtonType.OK) {
            User userToDelete = table_users.getSelectionModel().getSelectedItem();
            userToDelete.delete();
            table_users_data.remove(userToDelete);
        }
    }

    public void refreshData() {
        table_users_data.clear();
        table_users_data.addAll(UserFactory.getInstance().getAllUsers());
    }
}
