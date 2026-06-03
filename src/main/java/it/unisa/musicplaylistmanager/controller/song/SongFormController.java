package it.unisa.musicplaylistmanager.controller.song;

import it.unisa.musicplaylistmanager.app.App;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.entity.Tag;
import it.unisa.musicplaylistmanager.util.AlertManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

/**
 * Controller per la finestra modale dedicata alla creazione di una nuova traccia
 * o alla modifica di una traccia esistente nel catalogo.
 * Gestisce la validazione dei campi di input e l'aggiornamento dei dati.
 */
public class SongFormController {

    @FXML private Label formTitleLabel;

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private ComboBox<String> genreComboBox;
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
    @FXML private Button chooseFileButton;
    @FXML private Label filePathLabel;

    private Song songToEdit;
    private Runnable onSave;
    private String selectedFilePath;

    /**
     * Inizializza il form nascondendo preventivamente tutte le etichette di errore.
     */
    @FXML
    private void initialize() {
        hideErrors();
    }

    /**
     * Imposta la traccia da modificare.
     *
     * @param song traccia esistente da modificare
     */
    public void setSongToEdit(Song song) {
        this.songToEdit = song;

        if (song == null) {
            return;
        }

        formTitleLabel.setText("Modifica Traccia");
        confirmButton.setText("Salva modifiche");
        titleField.setText(song.getTitle());
        authorField.setText(song.getAuthor());
        genreComboBox.setValue(toViewGenre(song.getGenre()));
        yearField.setText(String.valueOf(song.getYear()));
        durationField.setText(String.valueOf(song.getDuration()));
        favouriteCheckBox.setSelected(song.hasTag(Tag.FAVOURITE));
        explicitCheckBox.setSelected(song.hasTag(Tag.EXPLICIT));
        newReleaseCheckBox.setSelected(song.hasTag(Tag.NEW_RELEASE));
        selectedFilePath = song.getFilePath();
        updateFilePathLabel();
    }

    /**
     * Imposta l'azione da eseguire dopo un salvataggio corretto.
     *
     * @param onSave callback di aggiornamento della vista chiamante
     */
    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    /**
     * Apre un selettore di sistema (FileChooser) per permettere all'utente
     * di cercare e selezionare il file audio da associare alla traccia.
     */
    @FXML private void onChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona file audio");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("File audio", "*.mp3", "*.wav", "*.aac", "*.flac", "*.ogg")
        );

        File file = fileChooser.showOpenDialog(getWindow());
        if (file != null) {
            selectedFilePath = file.getAbsolutePath();
            updateFilePathLabel();
        }
    }

    @FXML private void onCancel() {
        closeWindow();
    }

    /**
     * Valida i dati inseriti dall'utente nel form. In caso di validazione superata,
     * crea una nuova traccia oppure aggiorna la traccia esistente nel catalogo,
     * per poi chiudere la finestra.
     */
    @FXML private void onConfirm() {
        hideErrors();

        try {
            String title = titleField.getText();
            String author = authorField.getText();
            Genre genre = parseGenre(genreComboBox.getValue());
            int year = parseYear();
            int duration = parseDuration();

            if (selectedFilePath == null || selectedFilePath.isBlank()) {
                showGlobalError("Seleziona un file audio.");
                return;
            }

            if (songToEdit == null) {
                Song song = new Song(title, author, genre, year, duration, selectedFilePath);
                applyTags(song);
                App.getMusicLibrary().addSongToCatalog(song);
                AlertManager.showInfo("Traccia aggiunta al catalogo.");
            } else {
                songToEdit.setTitle(title);
                songToEdit.setAuthor(author);
                songToEdit.setGenre(genre);
                songToEdit.setYear(year);
                songToEdit.setDuration(duration);
                songToEdit.setFilePath(selectedFilePath);
                applyTags(songToEdit);
                AlertManager.showInfo("Traccia modificata correttamente.");
            }

            if (onSave != null) {
                onSave.run();
            }
            closeWindow();
        } catch (IllegalArgumentException e) {
            showGlobalError(e.getMessage());
        }
    }
    /**
     * Analizza e valida il testo inserito nel campo dell'anno di pubblicazione.
     *
     * @return l'anno di pubblicazione convertito in formato numerico intero
     * @throws IllegalArgumentException se il testo inserito non è un numero valido
     */
    private int parseYear() {
        try {
            return Integer.parseInt(yearField.getText().trim());
        } catch (NumberFormatException e) {
            yearErrorLabel.setVisible(true);
            yearErrorLabel.setManaged(true);
            throw new IllegalArgumentException("L'anno deve essere un numero valido.");
        }
    }
    /**
     * Analizza e valida il testo inserito nel campo della durata.
     * Supporta sia l'inserimento diretto in secondi, sia il formato classico "minuti:secondi".
     *
     * @return la durata totale calcolata in secondi
     * @throws IllegalArgumentException se il formato della durata non è riconosciuto
     */
    private int parseDuration() {
        String text = durationField.getText().trim();

        try {
            if (text.contains(":")) {
                String[] parts = text.split(":");
                if (parts.length != 2) {
                    throw new NumberFormatException();
                }
                return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
            }

            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            durationErrorLabel.setVisible(true);
            durationErrorLabel.setManaged(true);
            throw new IllegalArgumentException("La durata deve essere un numero di secondi oppure nel formato mm:ss.");
        }
    }

    /**
     * Converte la stringa selezionata dei generi nel corrispondente valore enumerato.
     *
     * @param value la stringa del genere selezionata dall'utente
     * @return l'istanza dell'enumerazione corrispondente
     * @throws IllegalArgumentException se nessun valore valido è stato selezionato
     */
    private Genre parseGenre(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Seleziona un genere.");
        }

        return switch (value) {
            case "Pop" -> Genre.POP;
            case "Rock" -> Genre.ROCK;
            case "Hip-Hop" -> Genre.HIP_HOP;
            case "Jazz" -> Genre.JAZZ;
            case "Classical" -> Genre.CLASSICAL;
            case "Electronic" -> Genre.ELECTRONIC;
            case "R&B" -> Genre.RNB;
            case "Country" -> Genre.COUNTRY;
            case "Metal" -> Genre.METAL;
            case "Indie" -> Genre.INDIE;
            case "Folk" -> Genre.FOLK;
            case "Reggae" -> Genre.REGGAE;
            case "Blues" -> Genre.BLUES;
            default -> Genre.ALTRO;
        };
    }

    /**
     * Converte un valore enumerato del genere nella corrispondente stringa formattata
     * per essere visualizzata correttamente all'interno dell'interfaccia.
     *
     * @param genre l'istanza dell'enumerazione da formattare
     * @return una stringa leggibile che rappresenta il genere, oppure null se inesistente
     */
    private String toViewGenre(Genre genre) {
        if (genre == null) {
            return null;
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

    /**
     * Applica i tag selezionati alla traccia.
     *
     * @param song la traccia a cui applicare i tag selezionati
     */
    private void applyTags(Song song) {
        for (Tag tag : Tag.values()) {
            song.removeTag(tag);
        }
        if (favouriteCheckBox.isSelected()) {
            song.addTag(Tag.FAVOURITE);
        }
        if (explicitCheckBox.isSelected()) {
            song.addTag(Tag.EXPLICIT);
        }
        if (newReleaseCheckBox.isSelected()) {
            song.addTag(Tag.NEW_RELEASE);
        }
    }

    private void hideErrors() {
        titleErrorLabel.setVisible(false);
        titleErrorLabel.setManaged(false);
        authorErrorLabel.setVisible(false);
        authorErrorLabel.setManaged(false);
        yearErrorLabel.setVisible(false);
        yearErrorLabel.setManaged(false);
        durationErrorLabel.setVisible(false);
        durationErrorLabel.setManaged(false);
        globalErrorLabel.setVisible(false);
        globalErrorLabel.setManaged(false);
    }

    private void showGlobalError(String message) {
        globalErrorLabel.setText(message);
        globalErrorLabel.setVisible(true);
        globalErrorLabel.setManaged(true);
        AlertManager.showError(message);
    }

    private void updateFilePathLabel() {
        if (selectedFilePath == null || selectedFilePath.isBlank()) {
            filePathLabel.setText("Nessun file selezionato");
        } else {
            filePathLabel.setText(selectedFilePath);
        }
    }

    private Window getWindow() {
        return chooseFileButton.getScene().getWindow();
    }

    private void closeWindow() {
        cancelButton.getScene().getWindow().hide();
    }
}
