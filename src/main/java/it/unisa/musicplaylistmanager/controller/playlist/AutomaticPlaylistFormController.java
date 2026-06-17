package it.unisa.musicplaylistmanager.controller.playlist;

import it.unisa.musicplaylistmanager.app.AppContext;
import it.unisa.musicplaylistmanager.controller.command.AddAutomaticPlaylistCommand;
import it.unisa.musicplaylistmanager.controller.command.Command;
import it.unisa.musicplaylistmanager.controller.command.CommandExecutor;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.entity.Tag;
import it.unisa.musicplaylistmanager.util.AlertManager;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller della finestra dedicata alla creazione automatica di una
 * playlist.
 *
 * <p>
 * Il controller recupera dal catalogo i generi, gli anni e i tag disponibili e
 * li mostra all'interno di menu a selezione multipla. L'utente può selezionare
 * uno o più criteri e generare una playlist contenente tutte le tracce che
 * soddisfano almeno uno dei criteri scelti.
 */
public class AutomaticPlaylistFormController {

	@FXML
	private TextField nameField;

	@FXML
	private MenuButton genresMenuButton;

	@FXML
	private MenuButton yearsMenuButton;

	@FXML
	private MenuButton tagsMenuButton;

	@FXML
	private Button cancelButton;

	private final AppContext appContext = AppContext.getInstance();
    private final CommandExecutor executor = CommandExecutor.getInstance();

	/*
	 * Associano ogni criterio alla relativa CheckBox, permettendo di recuperare
	 * facilmente i valori selezionati al momento della generazione.
	 */
	private final Map<Genre, CheckBox> genreCheckBoxes = new LinkedHashMap<>();
	private final Map<Integer, CheckBox> yearCheckBoxes = new LinkedHashMap<>();
	private final Map<Tag, CheckBox> tagCheckBoxes = new LinkedHashMap<>();

	private Runnable onSave;

	/**
	 * Inizializza il form recuperando le tracce presenti nel catalogo e popolando
	 * i menu dei criteri disponibili.
	 */
	@FXML
	private void initialize() {
		List<Song> songs = appContext.getMusicLibrary().getAllSongs();

		populateGenres(songs);
		populateYears(songs);
		populateTags(songs);
	}

	/**
	 * Imposta l'azione da eseguire dopo la corretta creazione della playlist.
	 *
	 * @param onSave
	 *            azione da eseguire dopo il salvataggio
	 */
	public void setOnSave(Runnable onSave) {
		this.onSave = onSave;
	}

	/**
	 * Estrae dal catalogo i generi disponibili, li ordina alfabeticamente e
	 * popola il relativo menu.
	 *
	 * @param songs
	 *            tracce presenti nel catalogo
	 */
	private void populateGenres(List<Song> songs) {
		List<Genre> genres = songs.stream()
				.map(Song::getGenre)
				.distinct()
				.sorted(Comparator.comparing(Genre::getLabel))
				.toList();

		populateCriteriaMenu(
				genresMenuButton,
				"Seleziona generi",
				genres,
				Genre::getLabel,
				genreCheckBoxes);
	}

	/**
	 * Estrae dal catalogo gli anni disponibili, li ordina dal più recente al più
	 * vecchio e popola il relativo menu.
	 *
	 * @param songs
	 *            tracce presenti nel catalogo
	 */
	private void populateYears(List<Song> songs) {
		List<Integer> years = songs.stream()
				.map(Song::getYear)
				.distinct()
				.sorted(Comparator.reverseOrder())
				.toList();

		populateCriteriaMenu(
				yearsMenuButton,
				"Seleziona anni",
				years,
				String::valueOf,
				yearCheckBoxes);
	}

	/**
	 * Estrae tutti i tag associati alle tracce, elimina i duplicati, li ordina
	 * alfabeticamente e popola il relativo menu.
	 *
	 * @param songs
	 *            tracce presenti nel catalogo
	 */
	private void populateTags(List<Song> songs) {
		List<Tag> tags = songs.stream()
				.flatMap(song -> song.getTags().stream())
				.distinct()
				.sorted(Comparator.comparing(Tag::getLabel))
				.toList();

		populateCriteriaMenu(
				tagsMenuButton,
				"Seleziona tag",
				tags,
				Tag::getLabel,
				tagCheckBoxes);
	}

	/**
	 * Popola un {@link MenuButton} con una lista scorrevole di checkbox.
	 *
	 * @param <T>
	 *            tipo dei valori mostrati nel menu
	 * @param menuButton
	 *            menu da popolare
	 * @param defaultText
	 *            testo mostrato quando non è selezionata alcuna opzione
	 * @param values
	 *            valori disponibili
	 * @param labelProvider
	 *            funzione che produce l'etichetta da visualizzare
	 * @param checkBoxes
	 *            mappa in cui registrare le checkbox create
	 */
	private <T> void populateCriteriaMenu(
			MenuButton menuButton,
			String defaultText,
			List<T> values,
			Function<T, String> labelProvider,
			Map<T, CheckBox> checkBoxes) {

		checkBoxes.clear();
		menuButton.getItems().clear();

		VBox optionsBox = new VBox(8);
		optionsBox.setPadding(new Insets(10));
		optionsBox.setPrefWidth(220);

		for (T value : values) {
			CheckBox checkBox = new CheckBox(labelProvider.apply(value));
			checkBox.getStyleClass().add("criteria-check-box");
			checkBox.setMaxWidth(Double.MAX_VALUE);

			checkBoxes.put(value, checkBox);

			/*
			 * Aggiorna il testo del MenuButton ogni volta che cambia il numero
			 * di criteri selezionati.
			 */
			checkBox.selectedProperty().addListener(
					(observable, oldValue, selected) ->
							updateMenuButtonText(menuButton, defaultText, checkBoxes));

			optionsBox.getChildren().add(checkBox);
		}

		if (values.isEmpty()) {
			Label emptyLabel = new Label("Nessuna opzione disponibile");
			emptyLabel.getStyleClass().add("criteria-empty-label");
			optionsBox.getChildren().add(emptyLabel);
		}

		ScrollPane scrollPane = new ScrollPane(optionsBox);
		scrollPane.setFitToWidth(true);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		scrollPane.setPrefViewportWidth(220);
		scrollPane.setPrefViewportHeight(calculateMenuHeight(values.size()));
		scrollPane.getStyleClass().add("criteria-menu-scroll");

		CustomMenuItem menuItem = new CustomMenuItem(scrollPane, false);
		menuButton.getItems().setAll(menuItem);
	}

	/**
	 * Calcola l'altezza della lista delle opzioni.
	 *
	 * @param optionCount
	 *            numero di opzioni presenti nel menu
	 * @return altezza preferita della lista
	 */
	private double calculateMenuHeight(int optionCount) {
		if (optionCount == 0) {
			return 50;
		}
		int MAX_HEIGHT = 220;
		return Math.min(MAX_HEIGHT, 18 + optionCount * 30);
	}

	/**
	 * Aggiorna il testo del menu mostrando il numero di criteri selezionati.
	 *
	 * @param menuButton
	 *            menu da aggiornare
	 * @param defaultText
	 *            testo mostrato quando non ci sono selezioni
	 * @param checkBoxes
	 *            checkbox associate al menu
	 */
	private void updateMenuButtonText(
			MenuButton menuButton,
			String defaultText,
			Map<?, CheckBox> checkBoxes) {

		long selectedCount = checkBoxes.values().stream()
				.filter(CheckBox::isSelected)
				.count();

		if (selectedCount == 0) {
			menuButton.setText(defaultText);
		} else {
			menuButton.setText(defaultText + " (" + selectedCount + ")");
		}
	}

	/**
	 * Gestisce la generazione della playlist automatica.
	 *
	 * <p>
	 * Il metodo valida il nome, raccoglie i criteri selezionati e delega alla
	 * libreria musicale la ricerca delle tracce e la creazione della playlist.
	 */
	@FXML
	private void onGenerate() {
		String name = nameField.getText() == null
				? ""
				: nameField.getText().trim();

		if (name.isBlank()) {
			AlertManager.showError("Inserire il nome della playlist.");
			return;
		}

		Set<Genre> selectedGenres = genreCheckBoxes.entrySet().stream()
				.filter(entry -> entry.getValue().isSelected())
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());

		Set<Integer> selectedYears = yearCheckBoxes.entrySet().stream()
				.filter(entry -> entry.getValue().isSelected())
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());

		Set<Tag> selectedTags = tagCheckBoxes.entrySet().stream()
				.filter(entry -> entry.getValue().isSelected())
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());

		if (selectedGenres.isEmpty()
				&& selectedYears.isEmpty()
				&& selectedTags.isEmpty()) {

			AlertManager.showError("Selezionare almeno un criterio.");
			return;
		}

		try {
            Command cmd = new AddAutomaticPlaylistCommand(appContext.getMusicLibrary(),
                                                                        name,
                                                                        selectedGenres,
                                                                        selectedYears,
                                                                        selectedTags);

            executor.execute(cmd);

			AlertManager.showInfo("Playlist automatica creata correttamente.");

			if (onSave != null) {
				onSave.run();
			}

			closeWindow();
		} catch (IllegalArgumentException e) {
			AlertManager.showError(e.getMessage());
		}
	}

	/**
	 * Annulla l'operazione e chiude il form senza creare alcuna playlist.
	 */
	@FXML
	private void onCancel() {
		closeWindow();
	}

	/**
	 * Chiude la finestra.
	 */
	private void closeWindow() {
		cancelButton.getScene().getWindow().hide();
	}
}
