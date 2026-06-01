package it.unisa.musicplaylistmanager.controller;

import it.unisa.musicplaylistmanager.util.AlertManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class HomeController {

    @FXML private TextField searchBar;

    @FXML private MenuButton autoCreateBtn;

    @FXML private Button newPlaylistBtn;

    @FXML private ComboBox<?> sortComboBox;

    @FXML private Label countLabel;

    @FXML private VBox emptyStateBox;

    @FXML private Label emptyTitle;
    @FXML private Label emptySubtitle;

    @FXML private Button emptyCreateBtn;

    @FXML private ListView<?> playlistListView;

    @FXML
    private void initialize() {
    }

    @FXML
    private void onAutoPlaylist() {
    }

    @FXML
    private void onAutoCreateByGenre() {
    }

    @FXML
    private void onAutoCreateByYear() {
    }

    @FXML
    private void onAutoCreateByArtist() {
    }

    @FXML
    private void onAutoCreateByTag() {
    }

    @FXML
    private void onNewPlaylist() {
    }

    @FXML
    private void onSortChanged() {
    }

    @FXML
    private void onListViewClicked() {
    }
}
