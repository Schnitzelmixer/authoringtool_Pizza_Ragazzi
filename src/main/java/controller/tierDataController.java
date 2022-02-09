package controller;

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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.util.converter.IntegerStringConverter;
import model.TierFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class tierDataController implements Initializable {
    private final ObservableList<TierFactory.Tier> tierObservableList = FXCollections.observableArrayList();
    @FXML
    public Button tier_refresh;
    public Button tier_new;
    public Button button_menu;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TableView<TierFactory.Tier> table_tier;
    @FXML
    private TableColumn<TierFactory.Tier, Integer> column_tier_id;
    @FXML
    private TableColumn<TierFactory.Tier, String> column_tier_name;
    @FXML
    private TableColumn<TierFactory.Tier, Integer> column_tier_points;
    @FXML
    private Label tier_selected_label;
    @FXML
    private Button tier_delete_button;

    private int idCurrentTier = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        column_tier_id.setCellValueFactory(new PropertyValueFactory<>("idTier"));
        column_tier_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        column_tier_points.setCellValueFactory(new PropertyValueFactory<>("totalPoints"));

        makeTierTableEditable();
        table_tier.setItems(tierObservableList);
        tierObservableList.addAll(TierFactory.getInstance().getAllTiers());
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

    private void makeTierTableEditable() {
        column_tier_id.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        column_tier_id.setOnEditCommit(
                event -> {
                    (event.getTableView().getItems().get(
                            event.getTablePosition().getRow())
                    ).setIdTier(event.getNewValue());
                    refreshData();
                }
        );
        column_tier_name.setCellFactory(TextFieldTableCell.forTableColumn());
        column_tier_name.setOnEditCommit(
                event ->
                        (event.getTableView().getItems().get(
                                event.getTablePosition().getRow())
                        ).setName(event.getNewValue())
        );
        column_tier_points.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        column_tier_points.setOnEditCommit(event -> {
            TierFactory.Tier tierToEdit = event.getTableView().getItems().get(event.getTablePosition().getRow());
            tierToEdit.setTotalPoints(event.getNewValue());
        });
    }

    @FXML
    private void tierSelected() {
        idCurrentTier = table_tier.getSelectionModel().getSelectedItem().getIdTier();
        String nameCurrent = table_tier.getSelectionModel().getSelectedItem().getName();
        tier_selected_label.setText("Selected Tier:\n" + nameCurrent);
        tier_delete_button.setVisible(true);
    }

    @FXML
    private void tierDeselect() {
        table_tier.getSelectionModel().clearSelection();
        idCurrentTier = 0;
        tier_selected_label.setText("No Tier Selected");
        tier_delete_button.setVisible(false);
    }

    @FXML
    private void refreshData() {
        tierObservableList.clear();
        tierObservableList.addAll(TierFactory.getInstance().getAllTiers());
        tierDeselect();
    }

    @FXML
    private void createNewTier() {
        // Create the custom dialog.
        Dialog<Pair<String, Integer>> dialog = new Dialog<>();
        dialog.setTitle("New Tier");
        dialog.setHeaderText("Create a new Tier.\nMake sure to update the tier id later!");

        // Set the button types.
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField new_tier = new TextField();
        IntegerField totalPoints = new IntegerField();
        totalPoints.setValue(1);
        grid.add(new Label("Tier Name:"), 0, 0);
        grid.add(new_tier, 1, 0);
        grid.add(new Label("Total Points needed to unlock:"), 0, 1);
        grid.add(totalPoints, 1, 1);

        // Enable/Disable create button depending on whether a name was entered.
        Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        new_tier.textProperty().addListener((observable, oldValue, newValue) -> createButton.setDisable(newValue.trim().isEmpty()));

        dialog.getDialogPane().setContent(grid);

        // Request focus on the field by default.
        Platform.runLater(new_tier::requestFocus);

        // Convert the result to a pair when the create button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Pair<>(new_tier.getText(), totalPoints.getValue());
            }
            return null;
        });
        Optional<Pair<String, Integer>> result = dialog.showAndWait();

        result.ifPresent(nameTier -> {
            TierFactory.getInstance().createTier(nameTier.getKey(), nameTier.getValue());
            refreshData();
        });
    }

    @FXML
    private void deleteSelectedTier() {
        TierFactory.Tier tierToDelete = TierFactory.getInstance().getTierById(idCurrentTier);

        if (tierToDelete.getIdTier() == 1) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("You can not delete this tier!");
            alert.show();
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete this tier?");
            Optional<ButtonType> action = alert.showAndWait();

            if (action.isPresent() && action.get() == ButtonType.OK) {
                tierToDelete.delete();
                refreshData();
            }
        }
    }

}
