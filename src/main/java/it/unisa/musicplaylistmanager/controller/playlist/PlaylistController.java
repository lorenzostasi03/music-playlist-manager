package it.unisa.musicplaylistmanager.controller.playlist;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class PlaylistController {

    @FXML private Label playlistNameLabel;
    @FXML private Label trackCountLabel;

    @FXML private Button playButton;
    @FXML private Button editNameButton;
    @FXML private Button deletePlaylistButton;

    @FXML private TextField searchField;
    @FXML private ComboBox<?> sortComboBox;

    @FXML private Button addTrackButton;

    @FXML private TableView<?> tracksTable;

    @FXML private TableColumn<?, ?> indexColumn;
    @FXML private TableColumn<?, ?> titleColumn;
    @FXML private TableColumn<?, ?> authorColumn;
    @FXML private TableColumn<?, ?> durationColumn;
    @FXML private TableColumn<?, ?> genreColumn;
    @FXML private TableColumn<?, ?> yearColumn;
    @FXML private TableColumn<?, ?> tagsColumn;

    @FXML private VBox emptyStateBox;

    @FXML private Button removeTrackButton;

    @FXML
    private void initialize() {
    }

    @FXML private void onPlay() {
    }

    @FXML private void onRename() {
    }

    @FXML private void onDeletePlaylist() {
    }

    @FXML private void onSearchChanged() {
    }

    @FXML private void onSortChanged() {
    }

    @FXML private void onAddTrack() {
    }

    @FXML private void onAddTrackClicked() {
    }

    @FXML private void onRemoveTrack() {
    }
}
