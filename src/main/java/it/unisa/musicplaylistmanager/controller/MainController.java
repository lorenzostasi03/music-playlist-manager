package it.unisa.musicplaylistmanager.controller;

import it.unisa.musicplaylistmanager.util.ViewSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

public class MainController {

    @FXML private BorderPane root;

    @FXML private Button homeButton;
    @FXML private Button catalogButton;
    @FXML private Button playbackButton;

    @FXML
    private void initialize() {
        ViewSwitcher.setMainRoot(root);
        ViewSwitcher.switchTo("HomeView.fxml");
    }

    @FXML
    private void onHome() {
        ViewSwitcher.switchTo("HomeView.fxml");
    }

    @FXML
    private void onCatalog() {
        ViewSwitcher.switchTo("CatalogView.fxml");
    }

    @FXML
    private void onPlayback() {
        ViewSwitcher.switchTo("PlaybackView.fxml");
    }
}
