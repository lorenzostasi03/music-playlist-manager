package it.unisa.musicplaylistmanager.controller.song;

import it.unisa.musicplaylistmanager.app.App;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.util.AlertManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Controller per la finestra modale che permette di visualizzare i brani del catalogo
 * non ancora presenti in una specifica playlist, consentendone la selezione multipla
 * per l'aggiunta in blocco.
 */
public class SongPickerController {

    @FXML private TextField searchField;

    @FXML private Button selectAllButton;
    @FXML private Button deselectAllButton;

    @FXML private TableView<SelectableSong> tracksTable;

    @FXML private TableColumn<SelectableSong, Boolean> checkColumn;
    @FXML private TableColumn<SelectableSong, String> titleColumn;
    @FXML private TableColumn<SelectableSong, String> authorColumn;
    @FXML private TableColumn<SelectableSong, String> genreColumn;
    @FXML private TableColumn<SelectableSong, String> yearColumn;

    @FXML private VBox emptyStateBox;

    @FXML private Button cancelButton;
    @FXML private Button confirmButton;

    private Playlist playlist;
    private Runnable onSave;

    /**
     * Inizializza il controller configurando le colonne della tabella,
     * inclusa la colonna personalizzata con le CheckBox per la selezione.
     */
    @FXML
    private void initialize() {
        configureTable();
    }

    /**
     * Imposta la playlist alla quale aggiungere le tracce selezionate.
     *
     * @param playlist playlist destinazione
     */
    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        refreshSongs();
    }

    /**
     * Imposta l'azione da eseguire dopo l'aggiunta delle tracce.
     *
     * @param onSave callback di aggiornamento della vista chiamante
     */
    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    @FXML private void onSearchChanged() {
    }

    /**
     * Seleziona automaticamente tutte le tracce attualmente caricate nella tabella.
     */
    @FXML private void onSelectAll() {
        tracksTable.getItems().forEach(selectableSong -> selectableSong.setSelected(true));
        updateConfirmButton();
    }

    /**
     * Deseleziona tutte le tracce attualmente presenti nella tabella.
     */
    @FXML private void onDeselectAll() {
        tracksTable.getItems().forEach(selectableSong -> selectableSong.setSelected(false));
        updateConfirmButton();
    }

    @FXML private void onCancel() {
        closeWindow();
    }

    /**
     * Recupera tutte le tracce selezionate dall'utente e le aggiunge
     * alla playlist di destinazione, chiudendo infine la finestra.
     */
    @FXML private void onConfirm() {
        if (playlist == null) {
            return;
        }

        List<Song> selectedSongs = tracksTable.getItems().stream()
            .filter(SelectableSong::isSelected)
            .map(SelectableSong::getSong)
            .toList();

        try {
            for (Song song : selectedSongs) {
                App.getMusicLibrary().addSongToPlaylist(song, playlist);
            }

            if (onSave != null) {
                onSave.run();
            }
            AlertManager.showInfo("Tracce aggiunte alla playlist.");
            closeWindow();
        } catch (IllegalArgumentException e) {
            AlertManager.showError(e.getMessage());
        }
    }

    private void configureTable() {
        checkColumn.setCellValueFactory(cellData ->
            cellData.getValue().selectedProperty()
        );
        checkColumn.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(event -> {
                    SelectableSong selectableSong = getTableRow().getItem();
                    if (selectableSong != null) {
                        selectableSong.setSelected(checkBox.isSelected());
                        updateConfirmButton();
                    }
                });
            }

            @Override
            protected void updateItem(Boolean selected, boolean empty) {
                super.updateItem(selected, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                checkBox.setSelected(Boolean.TRUE.equals(selected));
                setGraphic(checkBox);
            }
        });
        titleColumn.setCellValueFactory(cellData ->
            new ReadOnlyStringWrapper(cellData.getValue().getSong().getTitle())
        );
        authorColumn.setCellValueFactory(cellData ->
            new ReadOnlyStringWrapper(cellData.getValue().getSong().getAuthor())
        );
        genreColumn.setCellValueFactory(cellData ->
            new ReadOnlyStringWrapper(formatGenre(cellData.getValue().getSong().getGenre()))
        );
        yearColumn.setCellValueFactory(cellData ->
            new ReadOnlyStringWrapper(String.valueOf(cellData.getValue().getSong().getYear()))
        );
    }

    private void refreshSongs() {
        if (playlist == null) {
            return;
        }

        List<SelectableSong> availableSongs = App.getMusicLibrary().getAllSongs().stream()
            .filter(song -> !playlist.contains(song))
            .map(SelectableSong::new)
            .toList();

        tracksTable.getItems().setAll(availableSongs);
        boolean empty = availableSongs.isEmpty();
        emptyStateBox.setVisible(empty);
        emptyStateBox.setManaged(empty);
        tracksTable.setVisible(!empty);
        tracksTable.setManaged(!empty);
        confirmButton.setDisable(true);
    }

    private void updateConfirmButton() {
        boolean hasSelection = tracksTable.getItems().stream()
            .anyMatch(SelectableSong::isSelected);
        confirmButton.setDisable(!hasSelection);
    }

    private String formatGenre(Genre genre) {
        if (genre == null) {
            return "Altro";
        }

        return switch (genre) {
            case POP -> "Pop";
            case ROCK -> "Rock";
            case HIP_HOP -> "Hip-Hop";
            case JAZZ -> "Jazz";
            case CLASSICAL -> "Classical";
            case ELECTRONIC -> "Electronic";
            case RNB -> "R&B";
            case COUNTRY -> "Country";
            case METAL -> "Metal";
            case INDIE -> "Indie";
            case FOLK -> "Folk";
            case REGGAE -> "Reggae";
            case BLUES -> "Blues";
            case ALTRO -> "Altro";
        };
    }

    private void closeWindow() {
        cancelButton.getScene().getWindow().hide();
    }

    private static final class SelectableSong {
        private final Song song;
        private final BooleanProperty selected;

        private SelectableSong(Song song) {
            this.song = song;
            this.selected = new SimpleBooleanProperty(false);
        }

        private Song getSong() {
            return song;
        }

        private boolean isSelected() {
            return selected.get();
        }

        private void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        private BooleanProperty selectedProperty() {
            return selected;
        }
    }
}
