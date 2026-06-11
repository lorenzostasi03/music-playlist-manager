package it.unisa.musicplaylistmanager.controller.playlist;

import it.unisa.musicplaylistmanager.app.AppContext;
import it.unisa.musicplaylistmanager.controller.song.SongPickerController;
import it.unisa.musicplaylistmanager.exceptions.PersistenceException;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.entity.Tag;
import it.unisa.musicplaylistmanager.model.playback.PlaylistPlayable;
import it.unisa.musicplaylistmanager.util.AlertManager;
import it.unisa.musicplaylistmanager.util.DialogUtil;
import it.unisa.musicplaylistmanager.util.ViewSwitcher;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

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
	private Button playButton;
    @FXML
    private Button enqueueButton;
	@FXML
	private Button editNameButton;
	@FXML
	private Button deletePlaylistButton;

	@FXML
	private TextField searchField;
	@FXML
	private ComboBox<String> sortComboBox;

	@FXML
	private Button addTrackButton;

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

	@FXML
	private Button removeTrackButton;

	private Playlist playlist;

	private final AppContext appContext = AppContext.getInstance();

	/**
	 * Inizializza il controller recuperando la playlist selezionata dallo stato
	 * globale e configurando le colonne della tabella dei brani.
	 */

	@FXML
	private void initialize() {
        playlist = appContext.getSelectedPlaylist();
        boolean readOnly = appContext.isSelectedPlaylistReadOnly();

        configureTable();

        initButtons(readOnly);

        // gestione selezione tabella
        tracksTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, selectedSong) ->
                removeTrackButton.setDisable(selectedSong == null || readOnly)
        );

        refreshPlaylist();
	}

	@FXML
	private void onPlay() {
        if (playlist == null || playlist.size() == 0) {
            AlertManager.showError("La playlist è vuota.");
            return;
        }

        appContext.playPlayable(new PlaylistPlayable(playlist));
        ViewSwitcher.switchTo("PlaybackView.fxml");
	}

    @FXML
    private void onEnqueue() {
        if (playlist == null || playlist.size() == 0) {
            AlertManager.showError("La playlist è vuota.");
            return;
        }

        appContext.enqueuePlayable(new PlaylistPlayable(playlist));
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
			appContext.getMusicLibrary().removePlaylist(playlist);
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

	@FXML
	private void onSortChanged() {
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
			appContext.getMusicLibrary().removeSongFromPlaylist(selectedSong, playlist);
			refreshPlaylist();
			AlertManager.showInfo("Traccia rimossa dalla playlist.");
		} catch (IllegalArgumentException | PersistenceException e) {
			AlertManager.showError(e.getMessage());
		}
	}

    private void initButtons(boolean readOnly) {
        boolean showButton = (playlist == null || readOnly) ? false : true;

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
	 * Configura le proprietà della TableView.
	 */
	private void configureTable() {
		configureColumnProperties();
		configureCellFactories();
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
	 * Ricarica i dati della playlist selezionata e aggiorna l'interfaccia .
	 * Aggiorna le etichette descrittive, popola la tabella e gestisce la
	 * visualizzazione del pannello di avviso se la playlist risulta vuota.
	 */
	private void refreshPlaylist() {
		if (playlist == null) {
			showNoPlaylistSelectedState();
			return;
		}

		updatePlaylistInfo();
		updateTracksTable();
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

	/**
	 * Instanzia e visualizza la finestra per la selezione dei brani.
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
	 * Instanzia e visualizza la finestra per la modifica della playlist.
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

	// TO DO: da rivedere quando implementeremo i Tags
	private String formatTag(Tag tag) {
		return switch (tag) {
			case FAVOURITE -> "Preferito";
			case EXPLICIT -> "Esplicito";
			case NEW_RELEASE -> "Nuova uscita";
		};
	}
}
