package it.unisa.musicplaylistmanager.controller;

import it.unisa.musicplaylistmanager.app.AppContext;
import it.unisa.musicplaylistmanager.controller.command.AddPlayableToQueueCommand;
import it.unisa.musicplaylistmanager.controller.command.Command;
import it.unisa.musicplaylistmanager.controller.command.CommandExecutor;
import it.unisa.musicplaylistmanager.controller.command.RemoveSongFromCatalogCommand;
import it.unisa.musicplaylistmanager.controller.song.SongFormController;
import it.unisa.musicplaylistmanager.exceptions.PersistenceException;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.entity.Tag;
import it.unisa.musicplaylistmanager.model.playback.playable.SongPlayable;
import it.unisa.musicplaylistmanager.util.AlertManager;
import it.unisa.musicplaylistmanager.util.DialogUtil;
import it.unisa.musicplaylistmanager.util.ViewSwitcher;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller responsabile della visualizzazione e gestione del catalogo
 * musicale. Consente all'utente di scorrere le tracce disponibili, filtrarle in
 * base a vari criteri, e accedere alle funzioni di aggiunta, modifica,
 * eliminazione e riproduzione.
 */
public class CatalogController {

	@FXML
	private Button undoCommandButton;
	@FXML
	private TextField searchField;
	@FXML
	private ComboBox<String> genreFilter;
	@FXML
	private ComboBox<String> authorFilter;
	@FXML
	private ComboBox<String> yearFilter;
	@FXML
	private ComboBox<String> tagFilter;
	@FXML
	private Button addTrackButton;
	@FXML
	private ScrollPane scrollPane;

	private final VBox catalogRows = new VBox(6);

	private final AppContext appContext = AppContext.getInstance();
	private final CommandExecutor executor = CommandExecutor.getInstance();

	private final String ALL = "TUTTI";
	private boolean updatingFilters;

	/**
	 * Inizializza il controller configurando i filtri di ricerca e caricando la
	 * lista completa delle tracce dal catalogo musicale.
	 */
	@FXML
	private void initialize() {
		scrollPane.setContent(catalogRows);

		initGenreFilter();
		initTagFilter();

		searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshCatalog());
		genreFilter.setOnAction(event -> refreshCatalogIfReady());
		authorFilter.setOnAction(event -> refreshCatalogIfReady());
		yearFilter.setOnAction(event -> refreshCatalogIfReady());
		tagFilter.setOnAction(event -> refreshCatalogIfReady());

		initUndoButton();

		refreshCatalog();
	}

	/**
	 * Apre la schermata del form per l'inserimento di una nuova traccia nel
	 * catalogo.
	 */
	@FXML
	private void onAddSong() {
		openSongForm(null);
	}

	/**
	 * Inizializza il combo box per il filtraggio per genere.
	 */
	private void initGenreFilter() {
		genreFilter.getItems().addFirst(ALL);
		genreFilter.getItems().addAll(Arrays.stream(Genre.values()).map(Genre::getLabel).toList());
		genreFilter.setValue(ALL);
	}

	/**
	 * Inizializza il combo box per il filtraggio per tag.
	 */
	private void initTagFilter() {
		tagFilter.getItems().addFirst(ALL);
		tagFilter.getItems().addAll(Arrays.stream(Tag.values()).map(Tag::getLabel).toList());
		tagFilter.setValue(ALL);
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
	 * Ricarica e ridisegna la lista delle tracce a schermo. Aggiorna anche i menu a
	 * tendina dei filtri in base ai dati attuali.
	 */
	private void refreshCatalog() {
		refreshFilterValues();

		List<Song> songs = getSongs();

		catalogRows.getChildren().clear();

		if (songs.isEmpty()) {
			catalogRows.getChildren().add(createEmptyLabel());
			return;
		}

		for (Song song : songs) {
			catalogRows.getChildren().add(createSongRow(song));
		}
	}

	private Label createEmptyLabel() {
		Label label = new Label("Nessuna traccia presente nel catalogo.");
		label.getStyleClass().add("row-meta");
		return label;
	}

	/**
	 * Crea una riga per rappresentare visivamente una singola traccia nel catalogo,
	 * popolandola con i metadati della canzone e i pulsanti di riproduzione,
	 * modifica ed eliminazione.
	 *
	 * @param song
	 *            la traccia musicale da visualizzare nella riga
	 * @return un oggetto HBox formattato contenente le informazioni e i comandi
	 *         della traccia
	 */
	private VBox createSongRow(Song song) {
		Button playButton = createButton("▶", "Riproduci traccia", () -> playSong(song));

		Button enqueueButton = createButton("+", "Aggiungi traccia alla coda", () -> enqueueSong(song));

		Button editButton = createButton("✎", "Modifica traccia", () -> openSongForm(song));

		Button deleteButton = createButton("x", "Elimina traccia", () -> deleteSong(song));

		Label titleLabel = new Label(song.getTitle());
		titleLabel.getStyleClass().add("row-title");
		titleLabel.setMinWidth(125);
		titleLabel.setPrefWidth(125);
		titleLabel.setMaxWidth(125);
		titleLabel.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);

		Label authorLabel = createMetaLabel(song.getAuthor(), 60);
		Label genreLabel = createMetaLabel(song.getGenre().getLabel(), 55);
		Label yearLabel = createMetaLabel(String.valueOf(song.getYear()), 40);
		Label durationLabel = createMetaLabel(song.getDurationFormatted(), 60);
		Label playCountLabel = createMetaLabel(String.valueOf(song.getPlayCount()), 90);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox mainRow = new HBox(6, titleLabel, authorLabel, genreLabel, yearLabel, durationLabel, playCountLabel,
				spacer, playButton, enqueueButton, editButton, deleteButton);

		mainRow.setMaxWidth(Double.MAX_VALUE);

		Label tagsLabel = new Label("Tag: " + formatTags(song));
		tagsLabel.getStyleClass().add("row-meta");
		tagsLabel.setWrapText(true);
		tagsLabel.setMaxWidth(Double.MAX_VALUE);
		tagsLabel.setStyle("-fx-text-fill: #1DB954; -fx-font-size: 11px; -fx-padding: 0 0 0 0;");

		VBox row = new VBox(4, mainRow, tagsLabel);
		row.getStyleClass().add("list-row");
		row.setMaxWidth(Double.MAX_VALUE);

		return row;
	}

	private Button createButton(String text, String tooltip, Runnable action) {
		Button button = new Button(text);
		button.getStyleClass().add("row-action");
		button.setTooltip(new javafx.scene.control.Tooltip(tooltip));
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
	 * Metodo per creare una Label dedicata ai metadati della canzone.
	 *
	 * @param text
	 *            il testo da visualizzare all'interno della Label
	 * @param width
	 *            la larghezza preferita e minima da assegnare alla Label
	 * @return un oggetto Label formattato secondo le specifiche indicate
	 */
	private Label createMetaLabel(String text, double width) {
		Label label = new Label(text);
		label.setTextAlignment(TextAlignment.CENTER);
		label.getStyleClass().add("row-meta");
		label.setMinWidth(width);
		label.setPrefWidth(width);
		label.setMaxWidth(width);
		label.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);

		return label;
	}

	/**
	 * Gestisce il processo di eliminazione di una traccia dal catalogo.
	 *
	 * @param song
	 *            la traccia musicale da eliminare definitivamente dal catalogo
	 */
	private void deleteSong(Song song) {
		boolean confirmed = AlertManager
				.showConfirmation("Vuoi eliminare definitivamente la traccia '" + song.getTitle() + "'?");

		if (!confirmed) {
			return;
		}

		try {
			Command cmd = new RemoveSongFromCatalogCommand(appContext.getMusicLibrary(), appContext.getPlayer(), song);
			executor.execute(cmd);
			refreshCatalog();
			AlertManager.showInfo("Traccia eliminata correttamente.");
		} catch (PersistenceException | IllegalArgumentException e) {
			AlertManager.showError(e.getMessage());
		}
	}

	/**
	 * Apre la finestra relativa al form di gestione della traccia.
	 *
	 * @param song
	 *            l'istanza della traccia da modificare, oppure null se si tratta di
	 *            un inserimento
	 */
	private void openSongForm(Song song) {
		String title = (song == null) ? "Nuova traccia" : "Modifica traccia";

		DialogUtil.open("SongFormView.fxml", title, addTrackButton.getScene().getWindow(),
				(SongFormController controller) -> {
					controller.setSongToEdit(song);
					controller.setOnSave(this::refreshCatalog);
				});
	}

	/**
	 * Aggiorna dinamicamente i valori selezionabili all'interno dei ComboBox dei
	 * filtri relativi agli autori e agli anni di pubblicazione, basandosi sui brani
	 * effettivamente presenti. Mantiene la selezione utente precedente se ancora
	 * valida, altrimenti reimposta su "TUTTI".
	 */
	private void refreshFilterValues() {
		updatingFilters = true;

		List<Song> songs = appContext.getMusicLibrary().getAllSongs();

		String selectedAuthor = authorFilter.getValue();
		String selectedYear = yearFilter.getValue();

		List<String> authors = extractAuthors(songs);
		List<String> years = extractYears(songs);

		updateComboBox(authorFilter, authors, selectedAuthor);
		updateComboBox(yearFilter, years, selectedYear);

		updatingFilters = false;
	}

	private List<String> extractAuthors(List<Song> songs) {
		List<String> result = songs.stream().map(Song::getAuthor).distinct().sorted(String.CASE_INSENSITIVE_ORDER)
				.collect(Collectors.toCollection(ArrayList::new));
		result.addFirst(ALL);

		return result;
	}

	private List<String> extractYears(List<Song> songs) {
		List<String> result = songs.stream().map(s -> String.valueOf(s.getYear())).distinct().sorted()
				.collect(Collectors.toCollection(ArrayList::new));
		result.addFirst(ALL);

		return result;
	}

	private void updateComboBox(ComboBox<String> combo, List<String> values, String previousSelection) {
		combo.getItems().setAll(values);

		if (values.contains(previousSelection)) {
			combo.setValue(previousSelection);
		} else {
			combo.setValue(ALL);
		}
	}

	private void refreshCatalogIfReady() {
		if (!updatingFilters) {
			refreshCatalog();
		}
	}

	/**
	 * Apre la schermata di riproduzione e avvia la traccia selezionata.
	 *
	 * @param song
	 *            traccia da riprodurre
	 */
	private void playSong(Song song) {
		appContext.playPlayable(new SongPlayable(song));
		ViewSwitcher.switchTo("PlaybackView.fxml");
	}

	private void enqueueSong(Song song) {
		Command cmd = new AddPlayableToQueueCommand(appContext.getPlayer(), new SongPlayable(song));
		executor.execute(cmd);
		AlertManager.showInfo("Traccia aggiunta alla coda.");
	}

	/**
	 * Recupera la lista dei brani dal catalogo.
	 *
	 * @return lista dei brani.
	 */
	private List<Song> getSongs() {
		return appContext.getMusicLibrary().filterSongs(searchField.getText(), parseSelectedGenre(),
				parseSelectedAuthor(), parseSelectedYear(), parseSelectedTag());
	}

	private Genre parseSelectedGenre() {
		String value = genreFilter.getValue();
		if (value == null || ALL.equals(value)) {
			return null;
		}

		return Genre.fromLabel(value);
	}

	private String parseSelectedAuthor() {
		String value = authorFilter.getValue();
		if (value == null || ALL.equals(value)) {
			return null;
		}

		return value;
	}

	private Integer parseSelectedYear() {
		String value = yearFilter.getValue();
		if (value == null || ALL.equals(value)) {
			return null;
		}

		return Integer.parseInt(value);
	}

	private Tag parseSelectedTag() {
		String value = tagFilter.getValue();
		if (value == null || ALL.equals(value)) {
			return null;
		}

		return Arrays.stream(Tag.values()).filter(tag -> tag.getLabel().equals(value)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Tag non valido: " + value));
	}

	private String formatTags(Song song) {
		if (song.getTags().isEmpty()) {
			return "-";
		}

		return song.getTags().stream().map(Tag::getLabel).collect(Collectors.joining(", "));
	}

	@FXML
	public void onUndoCommand() {
		executor.undo();
		AlertManager.showInfo("L'operazione è stata annullata.");
		refreshCatalog();
	}
}
