package it.unisa.musicplaylistmanager.controller.song;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SongFormController {

    @FXML private Label formTitleLabel;

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private ComboBox<?> genreComboBox;
    @FXML private TextField yearField;
    @FXML private TextField durationField;

    @FXML private CheckBox favouriteCheckBox;
    @FXML private CheckBox explicitCheckBox;
    @FXML private CheckBox newReleaseCheckBox;

    @FXML private Label titleErrorLabel;
    @FXML private Label authorErrorLabel;
    @FXML private Label yearErrorLabel;
    @FXML private Label durationErrorLabel;
    @FXML private Label globalErrorLabel;

    @FXML private Button cancelButton;
    @FXML private Button confirmButton;

    @FXML
    private void initialize() {
    }

    @FXML private void onCancel() {
    }

    @FXML private void onConfirm() {
    }
}
