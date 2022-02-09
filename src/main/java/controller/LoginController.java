package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Database;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML
    private TextField host;

    @FXML
    private TextField database;

    @FXML
    private PasswordField password;

    @FXML
    private TextField username;

    /**
     * Sets a new Menu stage after the user is logged in.
     */
    @FXML
    private void login() {
        boolean connected = Database.getInstance().connect();
        if (connected) {
            try {
                Stage stage = (Stage) host.getScene().getWindow();
                Parent root = FXMLLoader.load(getClass().getResource("/view/mainMenu.fxml"));
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/stylesheets/LightMode.css").toExternalForm());
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Binds Database bidirectional on initialization.
     *
     * @param location  autofills.
     * @param resources autofills.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Database db = Database.getInstance();
        db.host.bindBidirectional(host.textProperty());
        db.database.bindBidirectional(database.textProperty());
        db.username.bindBidirectional(username.textProperty());
        db.password.bindBidirectional(password.textProperty());
    }
}
