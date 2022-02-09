package controller;

import com.sun.javafx.scene.control.DoubleField;
import com.sun.javafx.scene.control.IntegerField;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.util.converter.IntegerStringConverter;
import model.IngredientFactory;
import model.MemoryFactory;

import java.io.*;
import java.net.URL;
import java.util.*;

public class IngredientDataController implements Initializable {

    //Ingredient Table
    private final ObservableList<IngredientFactory.Ingredient> ingredientObservableList = FXCollections.observableArrayList();
    @FXML
    public TabPane tab_pane;
    public Button back_to_menu;
    public Button ingredient_new;
    public Button ingredient_refresh;
    //Memory
    public GridPane memoryGridPane;
    // For linking Pizza and Ingredient Editor together
    private PizzaDataController pizzaDataController;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TableView<IngredientFactory.Ingredient> table_ingredient;
    @FXML
    private TableColumn<IngredientFactory.Ingredient, Integer> column_idIngredient;
    @FXML
    private TableColumn<IngredientFactory.Ingredient, String> column_ingredientName;
    @FXML
    private TableColumn<IngredientFactory.Ingredient, Integer> column_idTier;
    //Ingredient Selection
    @FXML
    private Label ingredient_selected_label;
    private int idCurrentIngredient = 0;
    @FXML
    private Button ingredient_delete_button;
    //Ingredient Pictures
    @FXML
    private Button buttonPictureRaw;
    @FXML
    private Button buttonPictureDistraction;
    @FXML
    private Button buttonPictureProcessed;
    @FXML
    private Button buttonPictureBaked;
    @FXML
    private Button buttonPictureBurned;
    @FXML
    private ImageView imageRaw;
    @FXML
    private ImageView imageDistraction;
    @FXML
    private ImageView imageProcessed;
    @FXML
    private ImageView imageBaked;
    @FXML
    private ImageView imageBurned;
    //Specific Behavior
    @FXML
    private GridPane specificGridPane;

    public void setPizzaDataController(PizzaDataController pizzaDataController) {
        this.pizzaDataController = pizzaDataController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        column_idIngredient.setCellValueFactory(new PropertyValueFactory<>("id"));
        column_ingredientName.setCellValueFactory(new PropertyValueFactory<>("name"));
        column_idTier.setCellValueFactory(new PropertyValueFactory<>("idTier"));

        makeIngredientTableEditable();

        table_ingredient.setItems(ingredientObservableList);
        ingredientObservableList.addAll(IngredientFactory.getInstance().getAllIngredients());

        tab_pane.setVisible(false);

        buttonPictureRaw.setOnAction(
                e -> importIngredientPicture("raw")
        );
        buttonPictureDistraction.setOnAction(
                e -> importIngredientPicture("distraction")
        );
        buttonPictureProcessed.setOnAction(
                e -> importIngredientPicture("processed")
        );
        buttonPictureBaked.setOnAction(
                e -> importIngredientPicture("baked")
        );
        buttonPictureBurned.setOnAction(
                e -> importIngredientPicture("burned")
        );

        // Select the first row on Startup
        table_ingredient.requestFocus();
        table_ingredient.getSelectionModel().select(0);
        table_ingredient.getFocusModel().focus(0);
        ingredientSelected();
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

    private void makeIngredientTableEditable() {
        column_ingredientName.setCellFactory(TextFieldTableCell.forTableColumn());
        column_ingredientName.setOnEditCommit(event -> {
            IngredientFactory.Ingredient ingredientToEdit = event.getTableView().getItems().get(event.getTablePosition().getRow());
            if (ingredientToEdit.getId() == 1) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText("You can not edit this ingredient!");
                alert.show();
            } else {
                ingredientToEdit.setName(event.getNewValue());
            }
        });
        column_idTier.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        column_idTier.setOnEditCommit(event -> {

            IngredientFactory.Ingredient ingredientToEdit = event.getTableView().getItems().get(event.getTablePosition().getRow());
            if (ingredientToEdit.getId() == 1) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText("You can not edit this ingredient!");
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText(null);
                alert.setContentText("Are you sure?\nEditing the Tier could have strong effects on game playability");
                Optional<ButtonType> action = alert.showAndWait();

                if (action.isPresent() && action.get() == ButtonType.FINISH) {
                    ingredientToEdit.setUnlockTier(event.getNewValue());
                    refreshData();
                }
            }

        });
    }

    @FXML
    private void ingredientSelected() {
        idCurrentIngredient = table_ingredient.getSelectionModel().getSelectedItem().getId();
        String nameCurrent = table_ingredient.getSelectionModel().getSelectedItem().getName();
        ingredient_selected_label.setText("Edit Properties: " + nameCurrent);
        tab_pane.setVisible(true);
        ingredient_delete_button.setVisible(true);
        //Pics
        showPictures(IngredientFactory.getInstance().getIngredientPicturesById(idCurrentIngredient));
        //Memory
        displayMemory();
        //Flight and Stamp
        displaySpecific();
    }

    private void ingredientDeselect() {
        table_ingredient.getSelectionModel().clearSelection();
        idCurrentIngredient = 0;
        ingredient_selected_label.setText("no ingredient selected");
        tab_pane.setVisible(false);
        ingredient_delete_button.setVisible(false);


        HashMap<String, InputStream> pictureMap = new HashMap<>();
        pictureMap.put("raw", null);
        pictureMap.put("distraction", null);
        pictureMap.put("processed", null);
        pictureMap.put("baked", null);
        pictureMap.put("burned", null);
        showPictures(pictureMap);

        displaySpecific();
    }

    private void showPictures(Map<String, InputStream> picMap) {
        for (String picType : picMap.keySet()) {
            switch (picType) {
                case "raw":
                    try {
                        imageRaw.setImage(new Image(picMap.get(picType)));
                    } catch (NullPointerException n) {
                        imageRaw.setImage(null);
                    }
                    break;
                case "distraction":
                    try {
                        imageDistraction.setImage(new Image(picMap.get(picType)));
                    } catch (NullPointerException n) {
                        imageRaw.setImage(null);
                    }
                    break;
                case "processed":
                    try {
                        imageProcessed.setImage(new Image(picMap.get(picType)));
                    } catch (NullPointerException n) {
                        imageRaw.setImage(null);
                    }
                    break;
                case "baked":
                    try {
                        imageBaked.setImage(new Image(picMap.get(picType)));
                    } catch (NullPointerException n) {
                        imageRaw.setImage(null);
                    }
                    break;
                case "burned":
                    try {
                        imageBurned.setImage(new Image(picMap.get(picType)));
                    } catch (NullPointerException n) {
                        imageRaw.setImage(null);
                    }
                    break;
            }
        }

        if (pizzaDataController != null) {
            pizzaDataController.updatePictures();
        }
    }

    private void importIngredientPicture(String pictureType) {
        if (idCurrentIngredient == 0) {
            return;
        }
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("View PNG Files, select picture " + pictureType);
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
            fileChooser.getExtensionFilters().addAll(extFilterPNG);
            FileInputStream fileInputStream = new FileInputStream(fileChooser.showOpenDialog(null));
            IngredientFactory.getInstance().saveIngredientPicture(idCurrentIngredient, fileInputStream, pictureType);
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
        showPictures(IngredientFactory.getInstance().getIngredientPicturesById(idCurrentIngredient));
    }

    public void refreshData() {
        ingredientObservableList.clear();
        ingredientObservableList.addAll(IngredientFactory.getInstance().getAllIngredients());
        ingredientDeselect();
        if (pizzaDataController != null) {
            pizzaDataController.refreshData_list_ingredients();
            pizzaDataController.updatePictures();
        }
    }

    public void newIngredient() {

        // Create the custom dialog.
        Dialog<Pair<String, Integer>> dialog = new Dialog<>();
        dialog.setTitle("New Ingredient");
        dialog.setHeaderText("Create a new Ingredient\nRemember to add pictures later!");

        // Set the button types.
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField ingredient = new TextField("New Ingredient");
        IntegerField unlockTier = new IntegerField();
        unlockTier.setValue(1);
        grid.add(new Label("Ingredient Name:"), 0, 0);
        grid.add(ingredient, 1, 0);
        grid.add(new Label("Unlocked at tier:"), 0, 1);
        grid.add(unlockTier, 1, 1);

        // Enable/Disable login button depending on whether a name was entered.
        Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        ingredient.textProperty().addListener((observable, oldValue, newValue) -> createButton.setDisable(newValue.trim().isEmpty()));

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(ingredient::requestFocus);

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Pair<>(ingredient.getText(), unlockTier.getValue());
            }
            return null;
        });
        Optional<Pair<String, Integer>> result = dialog.showAndWait();

        result.ifPresent(nameTier -> {
            IngredientFactory.getInstance().createIngredient(nameTier.getKey(), nameTier.getValue());
            refreshData();
        });
    }

    public void deleteIngredient() {

        IngredientFactory.Ingredient ingredientToDelete = IngredientFactory.getInstance().getIngredientById(idCurrentIngredient);

        if (ingredientToDelete.getId() == 1) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("You can not delete this ingredient!");
            alert.show();
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete this ingredient?");
            Optional<ButtonType> action = alert.showAndWait();

            if (action.isPresent() && action.get() == ButtonType.OK) {
                ingredientToDelete.delete();
                ingredientDeselect();
                refreshData();
            }
        }
    }

    public void displayMemory() {
        List<MemoryFactory.Memory> memories = MemoryFactory.getInstance().getAllMemoriesByIngredientId(idCurrentIngredient);

        memoryGridPane.getChildren().clear();
        memoryGridPane.setHgap(5);
        memoryGridPane.setVgap(5);

        for (int i = 0; i < memories.size(); i++) {
            int finalI = i;

            TextField descriptionField = new TextField(memories.get(i).getMemoryDescription());
            descriptionField.setMinWidth(150.0);
            HBox.setHgrow(descriptionField, Priority.ALWAYS);
            descriptionField.textProperty().addListener((observable, oldValue, newValue) -> memories.get(finalI).setMemoryDescription(newValue));
            memoryGridPane.add(descriptionField, 0, i);

            Button deleteDescription = new Button("X");
            deleteDescription.setOnAction(e -> deleteDescription(finalI));
            memoryGridPane.add(deleteDescription, 1, i);
        }
    }

    public void newDescription() {

        MemoryFactory.getInstance().addMemoryToIngredient("description", idCurrentIngredient);
        displayMemory();
    }

    public void deleteDescription(int i) {

        List<MemoryFactory.Memory> memories = MemoryFactory.getInstance().getAllMemoriesByIngredientId(idCurrentIngredient);
        MemoryFactory.Memory memoryToDelete = memories.get(i);

        if (memories.size() == 1) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Ingredients should have at least one memory-description");
            alert.show();
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, null);
            alert.setContentText("Are you sure you want to delete this description?");
            Optional<ButtonType> action = alert.showAndWait();

            if (action.isPresent() && action.get() == ButtonType.OK) {
                memoryToDelete.delete();
                displayMemory();
            }
        }
    }

    public void displaySpecific() {
        Object object = IngredientFactory.getInstance().checkSpecificBehavior(idCurrentIngredient);
        if (object instanceof IngredientFactory.FlightBehavior) {
            displayCutting((IngredientFactory.FlightBehavior) object);
        } else if (object instanceof IngredientFactory.StampBehavior) {
            displayStamping((IngredientFactory.StampBehavior) object);
        } else if (idCurrentIngredient != 0) {
            IngredientFactory.getInstance().createFlightBehavior(idCurrentIngredient);
            displayCutting(IngredientFactory.getInstance().getFlightBehaviorByIngredientId(idCurrentIngredient));
        } else {
            specificGridPane.getChildren().clear();
        }
    }

    //<!-- Flight hat x, y, speed, rotation, hits required -->
    public void displayCutting(IngredientFactory.FlightBehavior flightBehavior) {

        specificGridPane.getChildren().clear();

        specificGridPane.setHgap(5);
        specificGridPane.setVgap(5);
        specificGridPane.add(new Label("Type: "), 0, 0);
        specificGridPane.add(new Label("Chopping"), 1, 0);

        Button change = new Button();
        change.setText("Change to Stamping");
        change.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to change this to stamping?\nCurrent chopping behavior will be lost!");
            Optional<ButtonType> action = alert.showAndWait();
            if (action.isPresent() && action.get() == ButtonType.OK) {
                //delete FlightBehavior for ingredient
                flightBehavior.delete();
                //add StampBehavior for ingredient
                IngredientFactory.getInstance().createStampBehavior(idCurrentIngredient);
                //display stamping
                displaySpecific();
            }
        });
        specificGridPane.add(change, 2, 0);

        // Vertex X
        Slider vertex_x_slider = new Slider(0, 100, flightBehavior.getX());
        vertex_x_slider.setValue(flightBehavior.getX());
        IntegerField vertex_x_value = new IntegerField();
        vertex_x_value.setValue(flightBehavior.getX());
        vertex_x_value.valueProperty().addListener((observableValue, oldValue, newValue) -> flightBehavior.setX(newValue.intValue()));
        vertex_x_slider.valueProperty().addListener((observable, oldValue, newValue) -> vertex_x_value.setValue(newValue.intValue()));
        Label vertex_x_label = new Label("X in %:");
        installTooltip(vertex_x_label, "Defines where (x-coordinate) this ingredient \nwill appear in chopping-minigame");
        specificGridPane.add(vertex_x_label, 0, 2);
        specificGridPane.add(vertex_x_slider, 1, 2);
        specificGridPane.add(vertex_x_value, 2, 2);

        // Vertex Y
        Slider vertex_y_slider = new Slider(0, 100, flightBehavior.getY());
        IntegerField vertex_y_value = new IntegerField();
        vertex_y_value.setValue(flightBehavior.getY());
        vertex_y_value.valueProperty().addListener((observableValue, oldValue, newValue) -> flightBehavior.setY(newValue.intValue()));
        vertex_y_slider.valueProperty().addListener((observable, oldValue, newValue) -> vertex_y_value.setValue(newValue.intValue()));
        Label vertex_y_label = new Label("Y in %:");
        installTooltip(vertex_y_label, "Defines how high (y-coordinate) this ingredient \nwill fly in chopping-minigame");
        specificGridPane.add(vertex_y_label, 0, 3);
        specificGridPane.add(vertex_y_slider, 1, 3);
        specificGridPane.add(vertex_y_value, 2, 3);

        // Speed
        Slider speed_slider = new Slider(1, 10, flightBehavior.getSpeed());
        DoubleField speed_value = new DoubleField();
        speed_value.setValue(flightBehavior.getSpeed());
        speed_value.valueProperty().addListener((observableValue, oldValue, newValue) -> flightBehavior.setSpeed((Double) newValue));
        speed_slider.valueProperty().addListener((observable, oldValue, newValue) -> speed_value.setValue(Math.round(((Double) newValue) * 10) / 10.0));
        Label speed_label = new Label("Speed:");
        installTooltip(speed_label, "Defines how fast the ingredient flies \nin chopping-minigame");
        specificGridPane.add(speed_label, 0, 4);
        specificGridPane.add(speed_slider, 1, 4);
        specificGridPane.add(speed_value, 2, 4);

        // Rotation
        Slider rotation_slider = new Slider(0, 20, flightBehavior.getRotation());
        rotation_slider.setBlockIncrement(1);
        IntegerField rotation_value = new IntegerField();
        rotation_value.setValue(flightBehavior.getRotation());
        rotation_value.valueProperty().addListener((observableValue, oldValue, newValue) -> flightBehavior.setRotation(newValue.intValue()));
        rotation_slider.valueProperty().addListener((observable, oldValue, newValue) -> rotation_value.setValue(newValue.intValue()));
        Label rotation_label = new Label("Rotation:");
        installTooltip(rotation_label, "Defines how fast the ingredient rotates \nin chopping minigame");
        specificGridPane.add(rotation_label, 0, 5);
        specificGridPane.add(rotation_slider, 1, 5);
        specificGridPane.add(rotation_value, 2, 5);

        // Hits
        IntegerField hits = new IntegerField();
        hits.setValue(flightBehavior.getHits());
        hits.valueProperty().addListener((observableValue, oldValue, newValue) -> flightBehavior.setHits((Integer) newValue));
        Label hits_label = new Label("Hits required:");
        installTooltip(hits_label, "Defines how often this ingredients must \nbe hit, before it is chopped");
        specificGridPane.add(hits_label, 0, 6);
        specificGridPane.add(hits, 1, 6);
    }

    //<!-- Stamp hat display time, disabling time, hits required -->
    public void displayStamping(IngredientFactory.StampBehavior stamping) {
        specificGridPane.getChildren().clear();
        specificGridPane.setHgap(5);
        specificGridPane.setVgap(5);
        specificGridPane.add(new Label("Type: "), 0, 0);
        specificGridPane.add(new Label("Stamping"), 1, 0);

        Button change = new Button();
        change.setText("Change to Chopping");
        change.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to change this to chopping?\nCurrent stamping behavior will be lost!");
            Optional<ButtonType> action = alert.showAndWait();
            if (action.isPresent() && action.get() == ButtonType.OK) {
                //delete StampBehavior for ingredient
                stamping.delete();
                //add FlightBehavior for ingredient
                IngredientFactory.getInstance().createFlightBehavior(idCurrentIngredient);
                //display cutting
                displaySpecific();
            }
        });
        specificGridPane.add(change, 2, 0);

        // Display Time
        IntegerField displayTime = new IntegerField();
        displayTime.setValue(stamping.getDisplayTime());
        displayTime.valueProperty().addListener((observable, oldValue, newValue) -> stamping.setDisplayTime((Integer) newValue));
        Label display_time_label = new Label("Display time [ms]:");
        installTooltip(display_time_label, "Defines how long this ingredient is shown \nin stamping-minigame");
        specificGridPane.add(display_time_label, 0, 2);
        specificGridPane.add(displayTime, 1, 2);

        // Disabling Time
        IntegerField disablingTime = new IntegerField();
        disablingTime.setValue(stamping.getDisablingTime());
        disablingTime.valueProperty().addListener((observable, oldValue, newValue) -> stamping.setDisablingTime((Integer) newValue));
        Label disabling_time_label = new Label("Disabling time [ms]:");
        installTooltip(disabling_time_label, "Defines how long the game is disabled \nafter hitting a distraction in stamping-minigame");
        specificGridPane.add(disabling_time_label, 0, 3);
        specificGridPane.add(disablingTime, 1, 3);

        // Hits
        IntegerField hits = new IntegerField();
        hits.setValue(stamping.getHits());
        hits.valueProperty().addListener((observable, oldValue, newValue) -> stamping.setHits((Integer) newValue));
        Label hits_label = new Label("Hits required:");
        installTooltip(hits_label, "Defines how often this ingredient must \nbe hit, before it is processed");
        specificGridPane.add(hits_label, 0, 4);
        specificGridPane.add(hits, 1, 4);
    }


    private void installTooltip(Node node, String text) {
        Tooltip tooltip = new Tooltip(text);
        Tooltip.install(node, tooltip);
    }
}
