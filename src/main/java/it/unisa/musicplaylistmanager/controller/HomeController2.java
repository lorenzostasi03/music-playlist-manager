package it.unisa.musicplaylistmanager.controller;

import it.unisa.musicplaylistmanager.app.AppContext;
import it.unisa.musicplaylistmanager.controller.playlist.PlaylistFormController;
import it.unisa.musicplaylistmanager.exceptions.PersistenceException;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.playback.PlaylistPlayable;
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
import javafx.scene.layout.Region;
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

    private HBox createPlaylistRow(PlaylistItem item) {
        Playlist playlist = item.playlist;
        boolean readOnly = item.readOnly;

        Label nameLabel = new Label(playlist.getName());
        nameLabel.getStyleClass().add("row-title");
        nameLabel.setPrefWidth(140);

        Label songsLabel = new Label(String.valueOf(playlist.size()));
        songsLabel.getStyleClass().add("row-meta");
        songsLabel.setPrefWidth(50);

        Label durationLabel = new Label(formatDuration(playlist));
        durationLabel.getStyleClass().add("row-meta");
        durationLabel.setPrefWidth(80);

        Label playCountLabel = new Label(
            item.showPlayCount ? String.valueOf(playlist.getPlayCount()) : ""
        );
        playCountLabel.getStyleClass().add("row-meta");
        playCountLabel.setPrefWidth(90);

        Button playButton = createButton("▶", "Riproduci playlist", () -> playPlaylist(playlist));
        Button enqueueButton = createButton("+", "Aggiungi playlist alla coda", () -> enqueuePlaylist(playlist));

        HBox row;

        if (!readOnly) {
            Button renameButton = createButton("✎", "Rinomina playlist", () -> openPlaylistForm(playlist));
            Button deleteButton = createButton("×", "Elimina playlist", () -> deletePlaylist(playlist));

            row = new HBox(8, nameLabel, songsLabel, durationLabel, playCountLabel, playButton, enqueueButton,
                renameButton, deleteButton);
        } else {
            Region actionPlaceholder = new Region();
            actionPlaceholder.setMinWidth(72);
            actionPlaceholder.setPrefWidth(72);
            actionPlaceholder.setMaxWidth(72);

            row = new HBox(8, nameLabel, songsLabel, durationLabel, playCountLabel, playButton, enqueueButton,
                actionPlaceholder);
        }

        row.getStyleClass().add("list-row");
        row.setMaxWidth(Double.MAX_VALUE);
        row.setOnMouseClicked(event -> openPlaylistView(item));

        return row;
    }

    private Button createButton(String text, String tooltip, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("row-action");
        button.setTooltip(new Tooltip(tooltip));
        button.setMinWidth(32);
        button.setPrefWidth(32);
        button.setMaxWidth(32);
        button.setOnAction(event -> {
            event.consume();
            action.run();
        });
        button.setFocusTraversable(false);

        return button;
    }

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

    private void playPlaylist(Playlist playlist) {
        if (playlist == null || playlist.size() == 0) {
            AlertManager.showError("La playlist è vuota.");
            return;
        }

        appContext.playPlayable(new PlaylistPlayable(playlist));
        ViewSwitcher.switchTo("PlaybackView.fxml");
    }

    private void enqueuePlaylist(Playlist playlist) {
        if (playlist == null || playlist.size() == 0) {
            AlertManager.showError("La playlist è vuota.");
            return;
        }

        appContext.enqueuePlayable(new PlaylistPlayable(playlist));
        AlertManager.showInfo("Playlist aggiunta alla coda.");
    }

    @FXML private void onAutoCreateByGenre() {}
    @FXML private void onAutoCreateByYear() {}
    @FXML private void onAutoCreateByArtist() {}
    @FXML private void onAutoCreateByTag() {}
    @FXML private void onNewPlaylist() { openPlaylistForm(null); }
    @FXML private void onSortChanged() {}
    @FXML private void onListViewClicked() {}
}
