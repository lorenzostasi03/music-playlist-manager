package it.unisa.musicplaylistmanager.controller.playlist;

import it.unisa.musicplaylistmanager.app.AppContext;
import it.unisa.musicplaylistmanager.controller.command.*;
import it.unisa.musicplaylistmanager.controller.song.SongPickerController;
import it.unisa.musicplaylistmanager.exceptions.PersistenceException;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.entity.Tag;
import it.unisa.musicplaylistmanager.model.playback.playable.PlaylistPlayable;
import it.unisa.musicplaylistmanager.util.AlertManager;
import it.unisa.musicplaylistmanager.util.DialogUtil;
import it.unisa.musicplaylistmanager.util.ViewSwitcher;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.stream.Collectors;

/**
 * Controller responsabile della gestione della vista dei dettagli di una
 * playlist. Permette di visualizzare le tracce contenute, aggiungerne di nuove,
 * rimuoverle, oltre a gestire la rinomina o l'eliminazione dell'intera
 * playlist.
 */
public class PlaylistController {
	@FXML
	private Label playlistNameLabel;
	@FXML
	private Label trackCountLabel;

	@FXML
	private Button editNameButton;
	@FXML
	private Button deletePlaylistButton;
	@FXML
	private Button addTrackButton;
	@FXML
	private Button removeTrackButton;
	@FXML
	private Button undoCommandButton;

	@FXML
	private TextField searchField;

	@FXML
	private Button sortButton;
	@FXML
	private ComboBox<String> sortComboBox;

	@FXML
	private TableView<Song> tracksTable;
	@FXML
	private TableColumn<Song, Integer> indexColumn;
	@FXML
	private TableColumn<Song, String> titleColumn;
	@FXML
	private TableColumn<Song, String> authorColumn;
	@FXML
	private TableColumn<Song, String> durationColumn;
	@FXML
	private TableColumn<Song, String> genreColumn;
	@FXML
	private TableColumn<Song, Integer> yearColumn;
	@FXML
	private TableColumn<Song, String> tagsColumn;

	private final AppContext appContext = AppContext.getInstance();
	private final CommandExecutor executor = CommandExecutor.getInstance();

	private Playlist playlist;
	private boolean readOnly;

	/**
	 * Inizializza il controller recuperando la playlist selezionata dallo stato
	 * globale e configurando le colonne della tabella dei brani.
	 */

	@FXML
	private void initialize() {
		playlist = appContext.getSelectedPlaylist();
		readOnly = appContext.isSelectedPlaylistReadOnly();

		configureTable();

		initButtons(readOnly);
		initUndoButton();
		initSort();

		tracksTable.getSelectionModel().selectedItemProperty()
				.addListener((observable, oldValue, selectedSong) -> updateSelectionButtons(selectedSong));

		refreshPlaylist();
	}

	@FXML
	private void onPlay() {
		if (playlist == null || playlist.isEmpty()) {
			AlertManager.showError("La playlist è vuota.");
			return;
		}

		appContext.playPlayable(new PlaylistPlayable(playlist));
		ViewSwitcher.switchTo("PlaybackView.fxml");
	}

	@FXML
	private void onEnqueue() {
		if (playlist == null || playlist.isEmpty()) {
			AlertManager.showError("La playlist è vuota.");
			return;
		}

		Command cmd = new AddPlayableToQueueCommand(appContext.getPlayer(), new PlaylistPlayable(playlist));
		executor.execute(cmd);
		AlertManager.showInfo("Playlist aggiunta alla coda.");
	}

	/**
	 * Apre la finestra modale per modificare il nome della playlist corrente.
	 */
	@FXML
	private void onRename() {
		if (playlist == null) {
			return;
		}
		openPlaylistForm();
	}

	/**
	 * Gestisce l'eliminazione della playlist corrente, richiedendo prima una
	 * conferma all'utente. In caso di successo, reindirizza l'utente alla schermata
	 * Home.
	 */
	@FXML
	private void onDeletePlaylist() {
		if (playlist == null) {
			return;
		}

		boolean confirmed = AlertManager.showConfirmation("Vuoi eliminare la playlist '" + playlist.getName() + "'?");

		if (!confirmed) {
			return;
		}

		try {
			Command cmd = new RemovePlaylistCommand(appContext.getMusicLibrary(), appContext.getPlayer(), playlist);
			executor.execute(cmd);
			appContext.setSelectedPlaylist(null);
			ViewSwitcher.switchTo("HomeView.fxml");
			AlertManager.showInfo("Playlist eliminata correttamente.");
		} catch (IllegalArgumentException | PersistenceException e) {
			AlertManager.showError(e.getMessage());
		}
	}

	@FXML
	private void onSearchChanged() {
		refreshPlaylist();
	}

	/**
	 * * Ordina le tracce della playlist secondo il criterio selezionato. * *
	 * <p>
	 * * La selezione del criterio nel menu non applica immediatamente *
	 * l'ordinamento, che viene eseguito solamente alla pressione del pulsante.
	 */
	@FXML
	private void onSort() {
		if (playlist == null || sortComboBox.getValue() == null) {
			return;
		}
		switch (sortComboBox.getValue()) {
			case "Titolo" -> appContext.getMusicLibrary().sortPlaylistSongsByTitle(playlist);
			case "Autore" -> appContext.getMusicLibrary().sortPlaylistSongsByAuthor(playlist);
			default -> {
				return;
			}


		}
		refreshPlaylist();
	}

	/**
	 * Apre la finestra modale che permette di cercare e aggiungere nuovi brani alla
	 * playlist.
	 */
	@FXML
	private void onAddTrack() {
		openSongPicker();
	}

	/**
	 * Rimuove il brano attualmente selezionato nella tabella dalla playlist
	 * corrente, previa conferma da parte dell'utente.
	 */
	@FXML
	private void onRemoveTrack() {
		Song selectedSong = tracksTable.getSelectionModel().getSelectedItem();
		if (playlist == null || selectedSong == null) {
			return;
		}

		boolean confirmed = AlertManager
				.showConfirmation("Vuoi rimuovere '" + selectedSong.getTitle() + "' dalla playlist?");

		if (!confirmed) {
			return;
		}

		try {
			Command cmd = new RemoveSongFromPlaylistCommand(appContext.getMusicLibrary(), playlist, selectedSong);
			executor.execute(cmd);
			refreshPlaylist();
			AlertManager.showInfo("Traccia rimossa dalla playlist.");
		} catch (IllegalArgumentException | PersistenceException e) {
			AlertManager.showError(e.getMessage());
		}
	}

	private void initButtons(boolean readOnly) {
		boolean showButton = playlist != null && !readOnly;

		addTrackButton.setDisable(!showButton);
		addTrackButton.setVisible(showButton);

		editNameButton.setDisable(!showButton);
		editNameButton.setVisible(showButton);

		deletePlaylistButton.setDisable(!showButton);
		deletePlaylistButton.setVisible(showButton);

		removeTrackButton.setDisable(!showButton);
		removeTrackButton.setVisible(showButton);
	}

	/**
	 * Inizializza il pulsante per annullare l'ultima operazione effettuata. Il
	 * pulsante è visibile e cliccabile solo se sono presenti operazioni da
	 * annullare.
	 */
	private void initUndoButton() {
		ReadOnlyBooleanProperty canUndo = executor.canUndoProperty();

		undoCommandButton.visibleProperty().bind(canUndo);
		undoCommandButton.managedProperty().bind(canUndo);
		undoCommandButton.disableProperty().bind(canUndo.not());
	}

	/**
	 * Inizializza i criteri disponibili per l'ordinamento automatico della
	 * playlist.
	 */
	private void initSort() {
		sortComboBox.getItems().setAll("Titolo", "Autore");
		sortComboBox.setValue("Titolo");
		updateSortState();
	}

	/** * Aggiorna lo stato dei controlli di ordinamento. */
	private void updateSortState() {
		boolean disabled = playlist == null || playlist.isEmpty() || readOnly;
		sortComboBox.setDisable(disabled);
		sortButton.setDisable(disabled);
	}

	/**
	 * Configura le proprietà della TableView.
	 */
	private void configureTable() {
		configureColumnProperties();
		configureCellFactories();
		configureDragAndDrop();
	}

	private void configureColumnProperties() {
		indexColumn.setSortable(false);
		titleColumn.setSortable(false);
		authorColumn.setSortable(false);
		durationColumn.setSortable(false);
		genreColumn.setSortable(false);
		yearColumn.setSortable(false);
		tagsColumn.setSortable(false);

		tracksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

		indexColumn.setPrefWidth(35);
		titleColumn.setPrefWidth(120);
		authorColumn.setPrefWidth(100);
		durationColumn.setPrefWidth(60);
		genreColumn.setPrefWidth(80);
		yearColumn.setPrefWidth(45);
		tagsColumn.setPrefWidth(220);
	}

	private void configureCellFactories() {
		indexColumn.setCellValueFactory(
				cellData -> new ReadOnlyObjectWrapper<>(tracksTable.getItems().indexOf(cellData.getValue()) + 1));

		titleColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getTitle()));

		authorColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getAuthor()));

		durationColumn
				.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getDurationFormatted()));

		genreColumn.setCellValueFactory(
				cellData -> new ReadOnlyStringWrapper(formatGenre(cellData.getValue().getGenre())));

		yearColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getYear()));

		tagsColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(formatTags(cellData.getValue())));
		tagsColumn.setCellFactory(column -> new TableCell<>() {
			private final Label label = new Label();

			{
				label.setWrapText(true);
			}

			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);

				if (empty || item == null) {
					setGraphic(null);
					return;
				}

				label.setText(item);
				label.setMaxWidth(tagsColumn.getWidth() - 12);
				setGraphic(label);
			}
		});
	}

	/**
	 * Ricarica i dati della playlist selezionata e aggiorna l'interfaccia. Aggiorna
	 * le etichette descrittive, popola la tabella e gestisce la visualizzazione del
	 * pannello di avviso se la playlist risulta vuota.
	 */
	private void refreshPlaylist() {
		if (playlist == null) {
			showNoPlaylistSelectedState();
			return;
		}

		updatePlaylistInfo();
		updateTracksTable();
		updateSortState();
	}

	private void showNoPlaylistSelectedState() {
		playlistNameLabel.setText("Nessuna playlist selezionata");
		trackCountLabel.setText("0 brani");
		tracksTable.getItems().clear();
		addTrackButton.setDisable(true);
		removeTrackButton.setDisable(true);
	}

	private void updatePlaylistInfo() {
		playlistNameLabel.setText(playlist.getName());
		trackCountLabel.setText(playlist.size() + " brani");
	}

	private void updateTracksTable() {
		tracksTable.getItems().setAll(playlist.searchSongs(searchField.getText()));
	}

	private void updateSelectionButtons(Song selectedSong) {
		boolean disabled = selectedSong == null || readOnly;
		removeTrackButton.setDisable(disabled);
	}

	private void configureDragAndDrop() {
		tracksTable.setRowFactory(table -> {
			TableRow<Song> row = new TableRow<>();

			row.setOnDragDetected(event -> {
				if (row.isEmpty() || !canMoveTracks()) {
					return;
				}

				Dragboard dragboard = row.startDragAndDrop(TransferMode.MOVE);
				ClipboardContent content = new ClipboardContent();
				content.putString(row.getItem().getId().toString());
				dragboard.setContent(content);
				event.consume();
			});

			row.setOnDragOver(event -> {
				Dragboard dragboard = event.getDragboard();
				if (!row.isEmpty() && dragboard.hasString() && canMoveTracks()
						&& !row.getItem().getId().toString().equals(dragboard.getString())) {
					event.acceptTransferModes(TransferMode.MOVE);
				}

				event.consume();
			});

			row.setOnDragDropped(event -> {
				boolean completed = false;
				Dragboard dragboard = event.getDragboard();

				if (!row.isEmpty() && dragboard.hasString() && canMoveTracks()) {
					completed = moveDraggedSong(dragboard.getString(), row.getItem());
				}

				event.setDropCompleted(completed);
				event.consume();
			});

			return row;
		});
	}

	private boolean moveDraggedSong(String draggedSongId, Song targetSong) {
		if (playlist == null || targetSong == null) {
			return false;
		}

		Song draggedSong = findSongById(draggedSongId);
		if (draggedSong == null || draggedSong.equals(targetSong)) {
			return false;
		}

		int targetPosition = playlist.getSongs().indexOf(targetSong);
		if (targetPosition < 0) {
			return false;
		}

		try {
			appContext.getMusicLibrary().moveSongInPlaylist(draggedSong, playlist, targetPosition);
			refreshPlaylist();
			tracksTable.getSelectionModel().select(draggedSong);
			return true;
		} catch (IllegalArgumentException | PersistenceException e) {
			AlertManager.showError(e.getMessage());
			return false;
		}
	}

	private Song findSongById(String songId) {
		return playlist.getSongs().stream().filter(song -> song.getId().toString().equals(songId)).findFirst()
				.orElse(null);
	}

	private boolean canMoveTracks() {
		String query = searchField.getText();
		return !readOnly && (query == null || query.isBlank());
	}

	/**
	 * Istanzia e visualizza la finestra per la selezione dei brani.
	 */
	private void openSongPicker() {
		if (playlist == null) {
			return;
		}

		DialogUtil.open("SongPickerView.fxml", "Aggiungi tracce", addTrackButton.getScene().getWindow(),
				(SongPickerController c) -> {
					c.setPlaylist(playlist);
					c.setOnSave(this::refreshPlaylist);
				});
	}

	/**
	 * Istanzia e visualizza la finestra per la modifica della playlist.
	 */
	private void openPlaylistForm() {
		if (playlist == null) {
			return;
		}

		DialogUtil.open("PlaylistFormView.fxml", "Rinomina playlist", editNameButton.getScene().getWindow(),
				(PlaylistFormController c) -> {
					c.setPlaylistToEdit(playlist);
					c.setOnSave(this::refreshPlaylist);
				});
	}

	private String formatGenre(Genre genre) {
		return genre != null ? genre.getLabel() : "Altro";
	}

	private String formatTags(Song song) {
		return song.getTags().stream().map(this::formatTag).collect(Collectors.joining(", "));
	}

	private String formatTag(Tag tag) {
		return tag != null ? tag.getLabel() : "";
	}

	@FXML
	public void onUndoCommand() {
		executor.undo();
		AlertManager.showInfo("L'operazione è stata annullata.");
		refreshPlaylist();
	}
}
