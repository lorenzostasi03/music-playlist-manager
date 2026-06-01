package it.unisa.musicplaylistmanager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;

public class CatalogController {

    @FXML private TextField searchField;

    @FXML private ComboBox<String> genreFilter;
    @FXML private ComboBox<String> authorFilter;
    @FXML private ComboBox<String> yearFilter;
    @FXML private ComboBox<String> tagFilter;

    @FXML private Button addTrackButton;

    @FXML private ScrollPane scrollPane;

    @FXML
    private void initialize() {
    }

    @FXML
    private void onAddTrack() {
    }
}
