package it.unisa.musicplaylistmanager.controller.playlist;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PlaylistFormController {

    @FXML private Label formTitleLabel;

    @FXML private TextField nameField;

    @FXML private Label nameErrorLabel;
    @FXML private Label globalErrorLabel;

    @FXML private Button cancelButton;
    @FXML private Button confirmButton;

    @FXML
    private void initialize() {
    }

    @FXML
    private void onCancel() {
    }

    @FXML
    private void onConfirm() {
    }
}
