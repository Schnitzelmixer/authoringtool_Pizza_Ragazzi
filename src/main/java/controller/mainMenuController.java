package controller;

import javafx.fxml.FXML;

public class mainMenuController extends mainController {


    @FXML
    void buttonUsers() {
        openNewScene("/view/userData.fxml");
    }

    @FXML
    void buttonIngredients() {
        openNewScene("/view/ingredientData.fxml");
    }

    @FXML
    void buttonPizzas() {
        openNewScene("/view/pizzaData.fxml");
    }

    @FXML
    void buttonTier() {
        openNewScene("/view/tierData.fxml");
    }

}
