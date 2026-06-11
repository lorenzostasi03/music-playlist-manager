package it.unisa.musicplaylistmanager.controller;

import it.unisa.musicplaylistmanager.app.AppContext;
import it.unisa.musicplaylistmanager.controller.playlist.PlaylistFormController;
import it.unisa.musicplaylistmanager.exceptions.PersistenceException;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.util.AlertManager;
import it.unisa.musicplaylistmanager.util.DialogUtil;
import it.unisa.musicplaylistmanager.util.ViewSwitcher;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HomeController2 implements Initializable {

    @FXML private TextField searchBar;
    @FXML private MenuButton autoCreateBtn;
    @FXML private Button newPlaylistBtn;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Label countLabel;
    @FXML private VBox emptyStateBox;
    @FXML private VBox listsContainer;
    @FXML private ListView<PlaylistItem> playlistListView;
    @FXML private ListView<PlaylistItem> mostPlayedListView;

    private final AppContext appContext = AppContext.getInstance();

    private List<Playlist> playlists;
    private Playlist topSongs;
    private List<Playlist> topPlaylists;

    private static final int TOP_SONGS = 10;
    private static final int TOP_PLAYLISTS = 3;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sortComboBox.getItems().setAll("Nome", "Numero brani", "Riproduzioni");
        sortComboBox.setValue("Nome");

        initListView(playlistListView);
        initListView(mostPlayedListView);

        updateViews();
    }

    // ------------------------------------------------------------
    // LIST VIEW SETUP
    // ------------------------------------------------------------

    private void initListView(ListView<PlaylistItem> listView) {
        listView.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(PlaylistItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                setGraphic(createPlaylistRow(item));
            }
        });
    }

    // ------------------------------------------------------------
    // VIEW UPDATE
    // ------------------------------------------------------------

    private void updateViews() {
        refreshPlaylists();
        refreshMostPlayed();
    }

    private void refreshPlaylists() {
        playlists = appContext.getMusicLibrary().getAllPlaylists();

        List<PlaylistItem> items = new ArrayList<>();

        playlists.forEach(playlist -> items.add(new PlaylistItem(playlist, false, true)));

        playlistListView.getItems().setAll(items);
        updateEmptyState(playlistListView);
    }

    private void refreshMostPlayed() {
        List<PlaylistItem> items = new ArrayList<>();

        topSongs = buildTopSongsPlaylist();
        if (topSongs != null) {
            items.add(new PlaylistItem(topSongs, true, false));
        }

        topPlaylists = getTopPlaylists();
        if (topPlaylists != null) {
            topPlaylists.forEach(playlist -> items.add(new PlaylistItem(playlist, false, true)));
        }

        mostPlayedListView.getItems().setAll(items);
        updateEmptyState(mostPlayedListView);
    }

    private void updateEmptyState(ListView<?> listView) {
        boolean empty = listView.getItems().isEmpty();
        emptyStateBox.setVisible(empty);
        emptyStateBox.setManaged(empty);
        listView.setVisible(!empty);
        listView.setManaged(!empty);
        countLabel.setText(listView.getItems().size() + " playlist");
    }

    // ------------------------------------------------------------
    // TOP PLAYLISTS
    // ------------------------------------------------------------

    private Playlist buildTopSongsPlaylist() {
        List<Song> songs = appContext.getMusicLibrary().getTopSongs(TOP_SONGS);
        if (songs.isEmpty()) return null;

        Playlist playlist = new Playlist("Top " + TOP_SONGS);
        songs.forEach(playlist::addSong);
        return playlist;
    }

    private List<Playlist> getTopPlaylists() {
        List<Playlist> list = appContext.getMusicLibrary().getTopPlaylists(TOP_PLAYLISTS);
        return list.isEmpty() ? null : new ArrayList<>(list);
    }

    // ------------------------------------------------------------
    // ROW CREATION
    // ------------------------------------------------------------

    private HBox createPlaylistRow(PlaylistItem item) {
        Playlist playlist = item.playlist;
        boolean readOnly = item.readOnly;

        Label nameLabel = new Label(playlist.getName());
        nameLabel.getStyleClass().add("row-title");
        nameLabel.setPrefWidth(300);

        Label songsLabel = new Label(String.valueOf(playlist.size()));
        songsLabel.getStyleClass().add("row-meta");
        songsLabel.setPrefWidth(60);

        Label durationLabel = new Label(formatDuration(playlist));
        durationLabel.getStyleClass().add("row-meta");
        durationLabel.setPrefWidth(80);

        Label playCountLabel = new Label(
            item.showPlayCount ? String.valueOf(playlist.getPlayCount()) : ""
        );
        playCountLabel.getStyleClass().add("row-meta");
        playCountLabel.setPrefWidth(90);

        Button renameButton = createActionButton("✎", "Rinomina playlist", () -> openPlaylistForm(playlist));
        Button deleteButton = createActionButton("×", "Elimina playlist", () -> deletePlaylist(playlist));

        renameButton.setDisable(readOnly);
        deleteButton.setDisable(readOnly);

        HBox row = new HBox(0, nameLabel, songsLabel, durationLabel, playCountLabel);

        if (!readOnly) {
            row.getChildren().addAll(renameButton, deleteButton);
        }

        row.getStyleClass().add("list-row");
        row.setOnMouseClicked(event -> openPlaylistView(item));

        return row;
    }

    private Button createActionButton(String text, String tooltip, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("row-action");
        btn.setTooltip(new Tooltip(tooltip));
        btn.setPrefWidth(28);
        btn.setOnAction(e -> {
            e.consume();
            action.run();
        });
        return btn;
    }

    // ------------------------------------------------------------
    // PLAYLIST OPERATIONS
    // ------------------------------------------------------------

    private void deletePlaylist(Playlist playlist) {
        boolean confirmed = AlertManager.showConfirmation(
            "Vuoi eliminare la playlist '" + playlist.getName() + "'?"
        );

        if (!confirmed) return;

        try {
            appContext.getMusicLibrary().removePlaylist(playlist);
            updateViews(); // <-- aggiorna entrambe le listView
            AlertManager.showInfo("Playlist eliminata correttamente.");
        } catch (PersistenceException | IllegalArgumentException e) {
            AlertManager.showError(e.getMessage());
        }
    }

    private void openPlaylistView(PlaylistItem item) {
        appContext.setSelectedPlaylist(item.playlist);
        appContext.setSelectedPlaylistReadOnly(item.readOnly);

        ViewSwitcher.switchTo("PlaylistView.fxml");
    }

    private void openPlaylistForm(Playlist playlist) {
        String title = (playlist == null)
            ? "Nuova playlist"
            : "Rinomina playlist";

        DialogUtil.open(
            "PlaylistFormView.fxml",
            title,
            newPlaylistBtn.getScene().getWindow(),
            (PlaylistFormController controller) -> {
                controller.setPlaylistToEdit(playlist);
                controller.setOnSave(this::updateViews);
            }
        );
    }

    // ------------------------------------------------------------
    // UTILS
    // ------------------------------------------------------------

    private String formatDuration(Playlist playlist) {
        int totalSeconds = playlist.getSongs().stream().mapToInt(Song::getDuration).sum();
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    private static class PlaylistItem {
        final Playlist playlist;
        final boolean readOnly;
        final boolean showPlayCount;

        PlaylistItem(Playlist playlist, boolean readOnly, boolean showPlayCount) {
            this.playlist = playlist;
            this.readOnly = readOnly;
            this.showPlayCount = showPlayCount;
        }
    }

    // ------------------------------------------------------------
    // FXML HANDLERS
    // ------------------------------------------------------------

    @FXML private void onAutoCreateByGenre() {}
    @FXML private void onAutoCreateByYear() {}
    @FXML private void onAutoCreateByArtist() {}
    @FXML private void onAutoCreateByTag() {}
    @FXML private void onNewPlaylist() { openPlaylistForm(null); }
    @FXML private void onSortChanged() {}
    @FXML private void onListViewClicked() {}
}
