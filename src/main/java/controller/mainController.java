package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class mainController {

    @FXML
    private AnchorPane anchorPane;

    public void openNewScene(String addressFXML) {
        try {
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(addressFXML));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/stylesheets/LightMode.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
