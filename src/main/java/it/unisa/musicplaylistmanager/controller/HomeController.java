package it.unisa.musicplaylistmanager.controller;

import it.unisa.musicplaylistmanager.app.AppContext;
import it.unisa.musicplaylistmanager.controller.playlist.PlaylistFormController;
import it.unisa.musicplaylistmanager.exceptions.PersistenceException;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.playback.PlaylistPlayable;
import it.unisa.musicplaylistmanager.util.AlertManager;
import it.unisa.musicplaylistmanager.util.ViewSwitcher;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller responsabile della schermata principale. Gestisce la
 * visualizzazione, la creazione e l'eliminazione delle playlist, oltre a
 * permettere la navigazione verso i dettagli di una playlist specifica.
 */
public class HomeController {

	@FXML
	private TextField searchBar;

	@FXML
	private MenuButton autoCreateBtn;

	@FXML
	private Button newPlaylistBtn;

	@FXML
	private ComboBox<String> sortComboBox;

	@FXML
	private Label countLabel;

	@FXML
	private VBox emptyStateBox;

	@FXML
	private Label emptyTitle;
	@FXML
	private Label emptySubtitle;

	@FXML
	private Button emptyCreateBtn;

	@FXML
	private ListView<Playlist> playlistListView;

	private final AppContext appContext = AppContext.getInstance();

	private List<Playlist> playlists;

	private Playlist topSongs;
	private final int TOP_SONGS = 10;

	private List<Playlist> topPlaylists;
	private final int TOP_PLAYLISTS = 3;

	/**
	 * Inizializza il controller configurando la lista delle playlist e caricando i
	 * dati attualmente presenti nel catalogo.
	 */
	@FXML
	private void initialize() {
		sortComboBox.getItems().setAll("Nome", "Numero brani", "Riproduzioni");
		sortComboBox.setValue("Nome");

		playlistListView.setCellFactory(listView -> new ListCell<>() {
			@Override
			protected void updateItem(Playlist playlist, boolean empty) {
				super.updateItem(playlist, empty);

				if (empty || playlist == null) {
					setText(null);
					setGraphic(null);
					return;
				}

				setText(null);
				setGraphic(createPlaylistRow(playlist));
			}
		});

		refreshPlaylists();
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

	/**
	 * Apre la finestra modale per la creazione di una nuova playlist vuota.
	 */
	@FXML
	private void onNewPlaylist() {
		openPlaylistForm(null);
	}

	@FXML
	private void onSortChanged() {
	}

	@FXML
	private void onListViewClicked() {
	}

	/**
	 * Crea un componente grafico che rappresenta visivamente una singola playlist
	 * all'interno della ListView, includendo metadati e pulsanti di azione.
	 *
	 * @param playlist
	 *            la playlist da visualizzare nella riga
	 * @return un oggetto HBox configurato con le informazioni della playlist
	 */
	private HBox createPlaylistRow(Playlist playlist) {
		boolean storedPlaylist = playlists.contains(playlist);

		Label nameLabel = new Label(playlist.getName());
		nameLabel.getStyleClass().add("row-title");
		nameLabel.setMinWidth(0);
		nameLabel.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(nameLabel, Priority.ALWAYS);

		Label songsLabel = new Label(String.valueOf(playlist.size()));
		songsLabel.getStyleClass().add("row-meta");
		songsLabel.setMinWidth(60);
		songsLabel.setPrefWidth(60);
		songsLabel.setMaxWidth(60);

		Label durationLabel = new Label(formatDuration(playlist));
		durationLabel.getStyleClass().add("row-meta");
		durationLabel.setMinWidth(80);
		durationLabel.setPrefWidth(80);
		durationLabel.setMaxWidth(80);

		Label playCountLabel = new Label(String.valueOf(playlist.getPlayCount()));
		playCountLabel.getStyleClass().add("row-meta");
		playCountLabel.setMinWidth(90);
		playCountLabel.setPrefWidth(90);
		playCountLabel.setMaxWidth(90);

		if (!storedPlaylist) {
			playCountLabel.setText("");
		}

		Button playButton = createButton("▶", "Riproduci playlist", () -> playPlaylist(playlist));
		Button enqueueButton = createButton("+", "Aggiungi playlist alla coda", () -> enqueuePlaylist(playlist));

		HBox row;

		if (storedPlaylist) {
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
		row.setOnMouseClicked(event -> openPlaylistView(playlist));

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
	/**
	 * Ricarica la lista delle playlist dal catalogo e aggiorna l'interfaccia.
	 */
	private void refreshPlaylists() {
		playlists = appContext.getMusicLibrary().getAllPlaylists();
		topSongs = refreshTopSongs();

		playlistListView.getItems().clear();

		if (topSongs != null) {
			playlistListView.getItems().add(topSongs);
		}

		playlistListView.getItems().addAll(playlists);

		countLabel.setText(playlistListView.getItems().size() + " playlist");

		boolean empty = playlistListView.getItems().isEmpty();
		emptyStateBox.setVisible(empty);
		emptyStateBox.setManaged(empty);
		playlistListView.setVisible(!empty);
		playlistListView.setManaged(!empty);
	}

	/**
	 * Calcola la durata totale di una playlist sommando la durata dei singoli brani
	 * e la formatta in una stringa (minuti:secondi).
	 *
	 * @param playlist
	 *            la playlist di cui calcolare la durata
	 * @return una stringa che rappresenta la durata totale nel formato "mm:ss"
	 */
	private String formatDuration(Playlist playlist) {
		int totalSeconds = playlist.getSongs().stream().mapToInt(Song::getDuration).sum();
		int minutes = totalSeconds / 60;
		int seconds = totalSeconds % 60;
		return String.format("%d:%02d", minutes, seconds);
	}

	/**
	 * Gestisce il processo di eliminazione di una playlist dalla libreria. Richiede
	 * una conferma da parte dell'utente prima di effettuare l'operazione.
	 *
	 * @param playlist
	 *            la playlist da eliminare definitivamente
	 */
	private void deletePlaylist(Playlist playlist) {
		boolean confirmed = AlertManager.showConfirmation("Vuoi eliminare la playlist '" + playlist.getName() + "'?");

		if (!confirmed) {
			return;
		}

		try {
			appContext.getMusicLibrary().removePlaylist(playlist);
			refreshPlaylists();
			AlertManager.showInfo("Playlist eliminata correttamente.");
		} catch (PersistenceException | IllegalArgumentException e) {
			AlertManager.showError(e.getMessage());
		}
	}

	/**
	 * Ricrea la playlist contenente i brani più riprodotti.
	 *
	 * @return playlist con i brani più riprodotti.
	 */
	private Playlist refreshTopSongs() {
		List<Song> songs = appContext.getMusicLibrary().getTopSongs(TOP_SONGS);
		if (songs.isEmpty())
			return null;

		Playlist playlist = new Playlist("Top " + TOP_SONGS);

		for (Song song : songs)
			playlist.addSong(song);

		return playlist;
	}

	/**
	 * Ricrea la lista delle playlist più riprodotte.
	 *
	 * @return lista delle playlist più riprodotte.
	 */
	private List<Playlist> refreshTopPlaylists() {
		List<Playlist> playlists = appContext.getMusicLibrary().getTopPlaylists(TOP_PLAYLISTS);
		if (playlists.isEmpty())
			return null;

		return new ArrayList<>(playlists);
	}

	/**
	 * Naviga verso la schermata della playlist specificata. Memorizza la playlist
	 * selezionata nello stato globale dell'applicazione prima del cambio vista.
	 *
	 * @param playlist
	 *            la playlist di cui visualizzare i dettagli
	 */
	private void openPlaylistView(Playlist playlist) {
		appContext.setSelectedPlaylist(playlist);
		ViewSwitcher.switchTo("PlaylistView.fxml");
	}

	/**
	 * Apre la finestra relativa al form di gestione della playlist. Questo metodo
	 * viene utilizzato sia per creare una nuova playlist sia per rinominarne una
	 * esistente.
	 *
	 * @param playlist
	 *            l'istanza della playlist da modificare, oppure null per una nuova
	 *            creazione
	 */
	private void openPlaylistForm(Playlist playlist) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/PlaylistFormView.fxml"));
			Parent root = loader.load();
			PlaylistFormController controller = loader.getController();
			controller.setPlaylistToEdit(playlist);
			controller.setOnSave(this::refreshPlaylists);

			Stage stage = new Stage();
			stage.setTitle(playlist == null ? "Nuova playlist" : "Rinomina playlist");
			stage.initModality(Modality.WINDOW_MODAL);
			stage.initOwner(newPlaylistBtn.getScene().getWindow());
			stage.setResizable(false);
			stage.setScene(new Scene(root));
			stage.showAndWait();
		} catch (IOException e) {
			AlertManager.showError("Impossibile aprire il form playlist.");
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
}
