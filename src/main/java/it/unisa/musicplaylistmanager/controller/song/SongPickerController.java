package it.unisa.musicplaylistmanager.controller.song;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class SongPickerController {

    @FXML private TextField searchField;

    @FXML private Button selectAllButton;
    @FXML private Button deselectAllButton;

    @FXML private TableView<?> tracksTable;

    @FXML private TableColumn<?, ?> checkColumn;
    @FXML private TableColumn<?, ?> titleColumn;
    @FXML private TableColumn<?, ?> authorColumn;
    @FXML private TableColumn<?, ?> genreColumn;
    @FXML private TableColumn<?, ?> yearColumn;

    @FXML private VBox emptyStateBox;

    @FXML private Button cancelButton;
    @FXML private Button confirmButton;

    @FXML
    private void initialize() {
    }

    @FXML private void onSearchChanged() {
    }

    @FXML private void onSelectAll() {
    }

    @FXML private void onDeselectAll() {
    }

    @FXML private void onCancel() {
    }

    @FXML private void onConfirm() {
    }
}
