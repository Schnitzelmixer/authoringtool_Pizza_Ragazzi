package controller;

import com.sun.javafx.scene.control.IntegerField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import model.IngredientFactory;
import model.PizzaFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class PizzaDataController implements Initializable {

    // Data for pizza table
    private final ObservableList<PizzaFactory.Pizza> pizzaObservableList = FXCollections.observableArrayList();
    // Data for ingredient list
    private final ObservableList<IngredientFactory.Ingredient> ingredientObservableList = FXCollections.observableArrayList();
    public Button edit_ingredients;
    public Button pizza_create_button;
    public Button refresh_button;
    @FXML
    private TableView<PizzaFactory.Pizza> table_pizzas;
    @FXML
    private ListView<IngredientFactory.Ingredient> list_ingredients;
    @FXML
    private StackPane stackPane;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TableColumn<PizzaFactory.Pizza, String> column_name;
    @FXML
    private TableColumn<PizzaFactory.Pizza, Integer> column_points;
    @FXML
    private TableColumn<PizzaFactory.Pizza, Integer> column_order_time;
    private int idCurrentPizza = -1;
    @FXML
    private Label pizza_selected_label;
    @FXML
    private Button pizza_delete_button;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Pizza-Table
        column_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        column_points.setCellValueFactory(new PropertyValueFactory<>("points"));
        column_order_time.setCellValueFactory(new PropertyValueFactory<>("order_time"));
        makePizzaTableEditable();

        // Ingredient-List
        list_ingredients.setCellFactory(param -> new IngredientCell());

        // Tells table/list where to get its data from
        table_pizzas.setItems(pizzaObservableList);
        list_ingredients.setItems(ingredientObservableList);

        // Initially fill lists with data
        refreshAll();

        // Select the first row on Startup
        table_pizzas.requestFocus();
        table_pizzas.getSelectionModel().select(0);
        table_pizzas.getFocusModel().focus(0);
        pizzaSelect();
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

    public void openIngredientWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/view/ingredientData.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Verlinkt die beiden Controller der Fenster miteinander
            IngredientDataController ingredientDataController = fxmlLoader.getController();
            ingredientDataController.back_to_menu.setVisible(false);
            ingredientDataController.setPizzaDataController(this);

            Stage stage = new Stage();
            stage.setTitle("Edit Ingredients");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void pizzaSelect() {
        if (table_pizzas.getSelectionModel().getSelectedItem() == null) {
            return;
        }
        idCurrentPizza = table_pizzas.getSelectionModel().getSelectedItem().getId();
        String nameCurrent = table_pizzas.getSelectionModel().getSelectedItem().getName();
        pizza_selected_label.setText(nameCurrent);
        pizza_delete_button.setVisible(true);

        updatePictures();
        refreshData_list_ingredients();
    }

    private void pizzaDeselect() {
        table_pizzas.getSelectionModel().clearSelection();
        idCurrentPizza = -1;
        pizza_selected_label.setText("Selected Pizza: None");
        pizza_delete_button.setVisible(false);

        updatePictures();
        refreshData_list_ingredients();
    }

    private void makePizzaTableEditable() {
        column_name.setCellFactory(
                TextFieldTableCell.forTableColumn());
        column_name.setOnEditCommit(
                event ->
                        (event.getTableView().getItems().get(
                                event.getTablePosition().getRow())
                        ).setName(event.getNewValue()));
        column_points.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        column_points.setOnEditCommit(event -> (event.getTableView().getItems().get(event.getTablePosition().getRow())).setPoints(event.getNewValue()));
        column_order_time.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        column_order_time.setOnEditCommit(event -> (event.getTableView().getItems().get(event.getTablePosition().getRow())).setOrder_time(event.getNewValue()));
    }

    public void refreshAll() {
        updatePictures();
        refreshData_table_pizzas();
        refreshData_list_ingredients();
        updateZIndices();
        pizzaDeselect();
    }

    private void refreshData_table_pizzas() {
        pizzaObservableList.clear();
        pizzaObservableList.addAll(PizzaFactory.getInstance().getAllPizzas());
    }

    public void refreshData_list_ingredients() {
        ingredientObservableList.clear();

        List<IngredientFactory.Ingredient> ingredients = IngredientFactory.getInstance().getAllIngredients();
        Predicate<IngredientFactory.Ingredient> isImpasto = ingredient -> (ingredient.getName().equals("Impasto"));
        ingredients.removeIf(isImpasto);

        ingredientObservableList.addAll(ingredients);
        ingredientObservableList.sort(Comparator.comparing(IngredientFactory.Ingredient::getzIndex));
    }

    private void updateZIndices() {

        for (int i = 0; i < ingredientObservableList.size(); i++) {

            ingredientObservableList.get(i).setZIndex(12 + i); // Impasto has zIndex = 11
        }
    }

    public void updatePictures() {

        clearPictures();

        if (idCurrentPizza == -1) {
            return;
        }

        Map<Integer, InputStream> picMap = PizzaFactory.getInstance().getPizzaPicturesById(idCurrentPizza);

        // TreeMaps are naturally sorted by key (key = zIndex)
        TreeMap<Integer, InputStream> sortedPicMap = new TreeMap<>(picMap);

        AtomicBoolean isFirst = new AtomicBoolean(true);
        sortedPicMap.forEach((zIndex, inputStream) -> {
            try {
                Image image = new Image(inputStream);
                ImageView imageview = new ImageView(image);
                imageview.setPreserveRatio(true);
                imageview.setFitHeight(isFirst.get() ? 170 : 150);
                imageview.setFitWidth(isFirst.get() ? 170 : 150);
                stackPane.getChildren().add(imageview);
                isFirst.set(false);
            } catch (NullPointerException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText("Some image-data is missing!\nPlease make sure that you define images for all ingredients");
                alert.show();
            }

        });
    }

    private void clearPictures() {

        stackPane.getChildren().clear();
    }

    public void newPizza() {

        // Create the custom dialog.
        Dialog<PizzaFactory.Pizza> dialog = new Dialog<>();
        dialog.setTitle("New Pizza");
        dialog.setHeaderText("Create a new Pizza\nYou can specify the ingredients later");

        // Set the button types.
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField pizza_name = new TextField("New Pizza");
        IntegerField points = new IntegerField();
        points.setValue(100);
        IntegerField order_time = new IntegerField();
        order_time.setValue(90);
        grid.add(new Label("Pizza Name:"), 0, 0);
        grid.add(pizza_name, 1, 0);
        grid.add(new Label("Points:"), 0, 1);
        grid.add(points, 1, 1);
        grid.add(new Label("Time to make:"), 0, 2);
        grid.add(order_time, 1, 2);

        // Enable/Disable login button depending on whether a name was entered.
        Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        pizza_name.textProperty().addListener((observable, oldValue, newValue) -> createButton.setDisable(newValue.trim().isEmpty()));

        dialog.getDialogPane().setContent(grid);

        // Request focus on the name-field by default.
        Platform.runLater(pizza_name::requestFocus);

        dialog.setResultConverter(result -> {
            if (result == createButtonType) {
                return new PizzaFactory.Pizza(pizza_name.getText(), points.getValue(), order_time.getValue());
            }
            return null;
        });
        Optional<PizzaFactory.Pizza> result = dialog.showAndWait();

        result.ifPresent(resultPizza -> {
            PizzaFactory.getInstance().createPizza(resultPizza.getName(), resultPizza.getPoints(), resultPizza.getOrder_time());
            refreshData_table_pizzas();
        });
    }

    public void deletePizza() {
        PizzaFactory.Pizza pizzaToDelete = PizzaFactory.getInstance().getPizzaById(idCurrentPizza);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(null);
        alert.setContentText("Are your sure you want to delete this?");
        Optional<ButtonType> action = alert.showAndWait();

        if (action.isPresent() && action.get() == ButtonType.OK) {
            pizzaToDelete.delete();
            pizzaDeselect();
            refreshData_table_pizzas();
        }
    }


    private class IngredientCell extends ListCell<IngredientFactory.Ingredient> {
        private final HBox content;
        private final Label nameField = new Label();
        private final CheckBox checkBox = new CheckBox();

        boolean checkingDisabled = true;

        IngredientCell() {
            IngredientCell thisCell = this;

            nameField.setAlignment(Pos.CENTER_RIGHT);
            Pane spaceBetween = new Pane();
            content = new HBox(nameField, spaceBetween, checkBox);
            content.setSpacing(10);

            HBox.setHgrow(spaceBetween, Priority.ALWAYS);

            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (!checkingDisabled) {
                    if (newValue) {
                        PizzaFactory.getInstance().addIngredientToPizza(getItem().getId(), idCurrentPizza);
                    } else {
                        PizzaFactory.getInstance().removeIngredientFromPizza(getItem().getId(), idCurrentPizza);
                    }
                }

                updatePictures();
            });

            // DRAG AND DROP --------------------------------------------
            setOnDragDetected(event -> {
                if (getItem() == null) {
                    return;
                }

                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(Integer.toString(getItem().getId()));
                dragboard.setContent(content);

                event.consume();
            });

            setOnDragOver(event -> {
                if (event.getGestureSource() != thisCell &&
                        event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }

                event.consume();
            });

            setOnDragEntered(event -> {
                if (event.getGestureSource() != thisCell &&
                        event.getDragboard().hasString()) {
                    setStyle("-fx-border-color : black transparent transparent transparent");
                }
            });

            setOnDragExited(event -> {
                if (event.getGestureSource() != thisCell &&
                        event.getDragboard().hasString()) {
                    setStyle("-fx-border-color : transparent");
                    setStyle("-fx-border-width: 0");
                }
            });

            setOnDragDropped(event -> {

                Dragboard dragboard = event.getDragboard();
                boolean success = false;

                if (dragboard.hasString()) {
                    ObservableList<IngredientFactory.Ingredient> items = getListView().getItems();

                    int originalId = -1;
                    for (IngredientFactory.Ingredient current : getListView().getItems()) {
                        if (current.getId() == Integer.parseInt(dragboard.getString())) {
                            originalId = items.indexOf(current);
                            break;
                        }
                    }

                    int destinationId = getItem() == null ? items.size() : items.indexOf(getItem());

                    IngredientFactory.Ingredient temp = ingredientObservableList.get(originalId);

                    if (originalId > destinationId) { // Moving element up
                        ingredientObservableList.remove(temp);
                        ingredientObservableList.add(destinationId, temp);
                    } else { // Moving element down
                        ingredientObservableList.add(destinationId, temp);
                        ingredientObservableList.remove(temp);
                    }

                    List<IngredientFactory.Ingredient> itemscopy = new ArrayList<>(getListView().getItems());
                    getListView().getItems().setAll(itemscopy);

                    updateZIndices();
                    updatePictures();

                    success = true;
                }
                event.setDropCompleted(success);

                event.consume();
            });

            setOnDragDone(DragEvent::consume);


        }


        @Override
        protected void updateItem(IngredientFactory.Ingredient item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
            } else {
                nameField.setText(item.getName());

                if (idCurrentPizza != -1) {
                    checkBox.setVisible(true);
                    checkingDisabled = true;
                    checkBox.setSelected(PizzaFactory.getInstance().isIngredientOnPizza(getItem().getId(), idCurrentPizza));
                    checkingDisabled = false;
                } else {
                    checkBox.setVisible(false);
                }

                setGraphic(content);
            }
        }
    }
}
