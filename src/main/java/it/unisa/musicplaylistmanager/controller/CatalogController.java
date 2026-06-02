package it.unisa.musicplaylistmanager.controller;

import it.unisa.musicplaylistmanager.app.App;
import it.unisa.musicplaylistmanager.controller.song.SongFormController;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.util.AlertManager;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller responsabile della visualizzazione e gestione del catalogo musicale.
 * Consente all'utente di scorrere le tracce disponibili, filtrarle in base a
 * vari criteri, e accedere alle funzioni di aggiunta, modifica ed eliminazione.
 */
public class CatalogController {

    @FXML private TextField searchField;

    @FXML private ComboBox<String> genreFilter;
    @FXML private ComboBox<String> authorFilter;
    @FXML private ComboBox<String> yearFilter;
    @FXML private ComboBox<String> tagFilter;

    @FXML private Button addTrackButton;

    @FXML private ScrollPane scrollPane;

    private final VBox catalogRows = new VBox(6);

    /**
     * Inizializza il controller configurando i filtri di ricerca e
     * caricando la lista completa delle tracce dal catalogo musicale.
     */
    @FXML
    private void initialize() {
        scrollPane.setContent(catalogRows);

        genreFilter.getItems().setAll("Tutti", "Pop", "Rock", "Hip-Hop", "Jazz", "Classical",
            "Electronic", "R&B", "Country", "Metal", "Indie", "Folk", "Reggae", "Blues", "Altro");
        genreFilter.setValue("Tutti");

        tagFilter.getItems().setAll("Tutti", "Preferito", "Esplicito", "Nuova uscita");
        tagFilter.setValue("Tutti");

        refreshCatalog();
    }

    /**
     * Apre la finestra modale per l'inserimento di una nuova traccia nel catalogo.
     */
    @FXML
    private void onAddTrack() {
        openSongForm(null);
    }

    /**
     * Ricarica e ridisegna la lista delle tracce a schermo.
     * Aggiorna anche i menu a tendina dei filtri in base ai dati attuali.
     */
    private void refreshCatalog() {
        refreshFilterValues();

        List<Song> songs = App.getMusicLibrary().getAllSongs().stream()
            .toList();

        catalogRows.getChildren().clear();

        if (songs.isEmpty()) {
            Label emptyLabel = new Label("Nessuna traccia presente nel catalogo.");
            emptyLabel.getStyleClass().add("row-meta");
            catalogRows.getChildren().add(emptyLabel);
            return;
        }

        for (Song song : songs) {
            catalogRows.getChildren().add(createSongRow(song));
        }
    }

    private HBox createSongRow(Song song) {
        Label titleLabel = new Label(song.getTitle());
        titleLabel.getStyleClass().add("row-title");
        titleLabel.setPrefWidth(220);
        titleLabel.setMinWidth(220);

        Label authorLabel = createMetaLabel(song.getAuthor(), 95);
        Label genreLabel = createMetaLabel(formatGenre(song.getGenre()), 70);
        Label yearLabel = createMetaLabel(String.valueOf(song.getYear()), 55);
        Label durationLabel = createMetaLabel(song.getDurationFormatted(), 60);

        Button editButton = new Button("✎");
        editButton.getStyleClass().add("row-action");
        editButton.setTooltip(new javafx.scene.control.Tooltip("Modifica traccia"));
        editButton.setPrefWidth(28);
        editButton.setOnAction(event -> openSongForm(song));

        Button deleteButton = new Button("×");
        deleteButton.getStyleClass().add("row-action");
        deleteButton.setTooltip(new javafx.scene.control.Tooltip("Elimina traccia"));
        deleteButton.setPrefWidth(28);
        deleteButton.setOnAction(event -> deleteSong(song));

        Region spacer = new Region();
        spacer.setPrefWidth(5);

        HBox row = new HBox(10, titleLabel, authorLabel, genreLabel, yearLabel, durationLabel,
            spacer, editButton, deleteButton);
        row.getStyleClass().add("list-row");
        return row;
    }

    private Label createMetaLabel(String text, double width) {
        Label label = new Label(text);
        label.getStyleClass().add("row-meta");
        label.setPrefWidth(width);
        label.setMinWidth(width);
        return label;
    }

    private void deleteSong(Song song) {
        boolean confirmed = AlertManager.showConfirmation(
            "Vuoi eliminare definitivamente la traccia '" + song.getTitle() + "'?"
        );

        if (!confirmed) {
            return;
        }

        try {
            App.getMusicLibrary().removeSongFromCatalog(song);
            refreshCatalog();
            AlertManager.showInfo("Traccia eliminata correttamente.");
        } catch (IllegalArgumentException e) {
            AlertManager.showError(e.getMessage());
        }
    }

    private void openSongForm(Song song) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/SongFormView.fxml"));
            Parent root = loader.load();
            SongFormController controller = loader.getController();
            controller.setSongToEdit(song);
            controller.setOnSave(this::refreshCatalog);

            Stage stage = new Stage();
            stage.setTitle(song == null ? "Nuova traccia" : "Modifica traccia");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(addTrackButton.getScene().getWindow());
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            AlertManager.showError("Impossibile aprire il form traccia.");
        }
    }

    private void refreshFilterValues() {
        String selectedAuthor = authorFilter.getValue();
        String selectedYear = yearFilter.getValue();

        List<String> authors = App.getMusicLibrary().getAllSongs().stream()
            .map(Song::getAuthor)
            .distinct()
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .collect(Collectors.toList());
        authors.add(0, "Tutti");

        List<String> years = App.getMusicLibrary().getAllSongs().stream()
            .map(song -> String.valueOf(song.getYear()))
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        years.add(0, "Tutti");

        authorFilter.getItems().setAll(authors);
        yearFilter.getItems().setAll(years);
        authorFilter.setValue(authors.contains(selectedAuthor) ? selectedAuthor : "Tutti");
        yearFilter.setValue(years.contains(selectedYear) ? selectedYear : "Tutti");
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
}
