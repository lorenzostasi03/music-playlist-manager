package it.unisa.musicplaylistmanager.controller.song;

import it.unisa.musicplaylistmanager.app.AppContext;
import it.unisa.musicplaylistmanager.exceptions.PersistenceException;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.util.AlertManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Controller per la finestra modale che permette di visualizzare i brani del
 * catalogo non ancora presenti in una specifica playlist, consentendone la
 * selezione multipla per l'aggiunta in blocco.
 */
public class SongPickerController {

	@FXML
	private TextField searchField;

	@FXML
	private Button selectAllButton;
	@FXML
	private Button deselectAllButton;

	@FXML
	private TableView<SelectableSong> tracksTable;

	@FXML
	private TableColumn<SelectableSong, Boolean> checkColumn;
	@FXML
	private TableColumn<SelectableSong, String> titleColumn;
	@FXML
	private TableColumn<SelectableSong, String> authorColumn;
	@FXML
	private TableColumn<SelectableSong, String> genreColumn;
	@FXML
	private TableColumn<SelectableSong, String> yearColumn;

	@FXML
	private VBox emptyStateBox;

	@FXML
	private Button cancelButton;
	@FXML
	private Button confirmButton;

	private Playlist playlist;
	private Runnable onSave;

	private final AppContext appContext = AppContext.getInstance();

	private final BooleanProperty hasSelection = new SimpleBooleanProperty(false);

	/**
	 * Inizializza il controller configurando le colonne della tabella, inclusa la
	 * colonna personalizzata con le CheckBox per la selezione.
	 */
	@FXML
	private void initialize() {
		configureCheckColumn();
		configureTextColumns();
		confirmButton.disableProperty().bind(hasSelection.not());
	}

	/**
	 * Imposta la playlist alla quale aggiungere le tracce selezionate.
	 *
	 * @param playlist
	 *            playlist destinazione
	 */
	public void setPlaylist(Playlist playlist) {
		this.playlist = playlist;
		refreshSongs();
	}

	/**
	 * Imposta l'azione da eseguire dopo l'aggiunta delle tracce.
	 *
	 * @param onSave
	 *            callback di aggiornamento della vista chiamante
	 */
	public void setOnSave(Runnable onSave) {
		this.onSave = onSave;
	}

	@FXML
	private void onSearchChanged() {
	}

	/**
	 * Seleziona automaticamente tutte le tracce attualmente caricate nella tabella.
	 */
	@FXML
	private void onSelectAll() {
		setAllSelected(true);
	}

	/**
	 * Deseleziona tutte le tracce attualmente presenti nella tabella.
	 */
	@FXML
	private void onDeselectAll() {
		setAllSelected(false);
	}

	@FXML
	private void onCancel() {
		closeWindow();
	}

	/**
	 * Recupera tutte le tracce selezionate dall'utente e le aggiunge alla playlist
	 * di destinazione, chiudendo infine la finestra.
	 */
	@FXML
	private void onConfirm() {
		if (playlist == null) {
			return;
		}

		List<Song> selectedSongs = tracksTable.getItems().stream().filter(SelectableSong::isSelected)
				.map(SelectableSong::getSong).toList();

		try {
			addSongsToPlaylist(selectedSongs);

			if (onSave != null) {
				onSave.run();
			}

			AlertManager.showInfo("Tracce aggiunte alla playlist.");
			closeWindow();

		} catch (IllegalArgumentException e) {
			AlertManager.showError(e.getMessage());
		}
	}

	/**
	 * Configura la colonna checkbox, incluso il comportamento di selezione e
	 * l'aggiornamento dello stato del pulsante di conferma.
	 */
	private void configureCheckColumn() {
		checkColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());

		checkColumn.setCellFactory(column -> new TableCell<>() {

			private final CheckBox checkBox = new CheckBox();

			{
				checkBox.setOnAction(event -> {
					SelectableSong item = getTableRow().getItem();
					if (item != null) {
						item.setSelected(checkBox.isSelected());
						updateSelectionState();
					}
				});
			}

			@Override
			protected void updateItem(Boolean selected, boolean empty) {
				super.updateItem(selected, empty);
				if (empty || getTableRow().getItem() == null) {
					setGraphic(null);
					return;
				}
				checkBox.setSelected(Boolean.TRUE.equals(selected));
				setGraphic(checkBox);
			}
		});
	}

	/** Configura le colonne testuali (titolo, autore, genere, anno). */
	private void configureTextColumns() {
		titleColumn
				.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getSong().getTitle()));
		authorColumn
				.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getSong().getAuthor()));
		genreColumn.setCellValueFactory(
				cellData -> new ReadOnlyStringWrapper(formatGenre(cellData.getValue().getSong().getGenre())));
		yearColumn.setCellValueFactory(
				cellData -> new ReadOnlyStringWrapper(String.valueOf(cellData.getValue().getSong().getYear())));
	}

	private void updateSelectionState() {
		boolean hasAnySelected = tracksTable.getItems().stream().anyMatch(SelectableSong::isSelected);

		hasSelection.set(hasAnySelected);
	}

	private void setAllSelected(boolean value) {
		tracksTable.getItems().forEach(s -> s.setSelected(value));
		updateSelectionState();
	}
	/**
	 * Recupera dal catalogo tutte le canzoni disponibili, escludendo quelle già
	 * presenti nella playlist di destinazione. Popola la tabella con i risultati o
	 * mostra lo stato vuoto se non ci sono tracce disponibili per l'aggiunta.
	 */
	private void refreshSongs() {
		if (playlist == null)
			return;

		List<SelectableSong> available = appContext.getMusicLibrary().getAllSongs().stream()
				.filter(song -> !playlist.contains(song)).map(SelectableSong::new).toList();

		tracksTable.getItems().setAll(available);
		updateSelectionState();

		boolean empty = available.isEmpty();
		emptyStateBox.setVisible(empty);
		emptyStateBox.setManaged(empty);
		tracksTable.setVisible(!empty);
		tracksTable.setManaged(!empty);
	}

	private void addSongsToPlaylist(List<Song> songs) {
		try {
			for (Song song : songs)
				appContext.getMusicLibrary().addSongToPlaylist(song, playlist);
		} catch (PersistenceException | IllegalArgumentException e) {
			AlertManager.showError(e.getMessage());
		}

	}

	/**
	 * Converte un'istanza dell'enumerazione {@link Genre} in una stringa testuale
	 * formattata per la visualizzazione nella colonna della tabella.
	 *
	 * @param genre
	 *            il genere musicale da formattare
	 * @return una stringa leggibile rappresentante il genere, o "Altro" se nullo
	 */
	private static String formatGenre(Genre genre) {
		return genre != null ? genre.getLabel() : "Altro";
	}

	private void closeWindow() {
		cancelButton.getScene().getWindow().hide();
	}

	/**
	 * Wrapper di {@link Song} che aggiunge una proprietà booleana osservabile per
	 * tracciare lo stato di selezione nella tabella.
	 */
	private static final class SelectableSong {

		private final Song song;
		private final BooleanProperty selected = new SimpleBooleanProperty(false);

		private SelectableSong(Song song) {
			this.song = song;
		}

		private Song getSong() {
			return song;
		}
		private boolean isSelected() {
			return selected.get();
		}
		private void setSelected(boolean value) {
			selected.set(value);
		}
		private BooleanProperty selectedProperty() {
			return selected;
		}
	}
}