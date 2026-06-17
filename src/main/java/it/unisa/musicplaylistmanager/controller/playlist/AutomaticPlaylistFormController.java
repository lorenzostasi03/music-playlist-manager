package it.unisa.musicplaylistmanager.controller.playlist;

import it.unisa.musicplaylistmanager.app.AppContext;
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

	private final Map<Genre, CheckBox> genreCheckBoxes = new LinkedHashMap<>();
	private final Map<Integer, CheckBox> yearCheckBoxes = new LinkedHashMap<>();
	private final Map<Tag, CheckBox> tagCheckBoxes = new LinkedHashMap<>();

	private Runnable onSave;

	@FXML
	private void initialize() {
		List<Song> songs = appContext.getMusicLibrary().getAllSongs();

		populateGenres(songs);
		populateYears(songs);
		populateTags(songs);
	}

	public void setOnSave(Runnable onSave) {
		this.onSave = onSave;
	}

	private void populateGenres(List<Song> songs) {
		List<Genre> genres = songs.stream().map(Song::getGenre).distinct().sorted(Comparator.comparing(Genre::getLabel))
				.toList();

		populateCriteriaMenu(genresMenuButton, "Seleziona generi", genres, Genre::getLabel, genreCheckBoxes);
	}

	private void populateYears(List<Song> songs) {
		List<Integer> years = songs.stream().map(Song::getYear).distinct().sorted(Comparator.reverseOrder()).toList();

		populateCriteriaMenu(yearsMenuButton, "Seleziona anni", years, String::valueOf, yearCheckBoxes);
	}

	private void populateTags(List<Song> songs) {
		List<Tag> tags = songs.stream().flatMap(song -> song.getTags().stream()).distinct()
				.sorted(Comparator.comparing(Tag::getDisplayName)).toList();

		populateCriteriaMenu(tagsMenuButton, "Seleziona tag", tags, Tag::getDisplayName, tagCheckBoxes);
	}

	private <T> void populateCriteriaMenu(MenuButton menuButton, String defaultText, List<T> values,
			Function<T, String> labelProvider, Map<T, CheckBox> checkBoxes) {

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

			checkBox.selectedProperty().addListener(
					(observable, oldValue, selected) -> updateMenuButtonText(menuButton, defaultText, checkBoxes));

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

	private double calculateMenuHeight(int optionCount) {
		if (optionCount == 0) {
			return 50;
		}

		return Math.min(220, 18 + optionCount * 30);
	}

	private void updateMenuButtonText(MenuButton menuButton, String defaultText, Map<?, CheckBox> checkBoxes) {

		long selectedCount = checkBoxes.values().stream().filter(CheckBox::isSelected).count();

		if (selectedCount == 0) {
			menuButton.setText(defaultText);
		} else {
			menuButton.setText(defaultText + " (" + selectedCount + ")");
		}
	}

	@FXML
	private void onGenerate() {
		String name = nameField.getText() == null ? "" : nameField.getText().trim();

		if (name.isBlank()) {
			AlertManager.showError("Inserire il nome della playlist.");
			return;
		}

		Set<Genre> selectedGenres = genreCheckBoxes.entrySet().stream().filter(entry -> entry.getValue().isSelected())
				.map(Map.Entry::getKey).collect(Collectors.toSet());

		Set<Integer> selectedYears = yearCheckBoxes.entrySet().stream().filter(entry -> entry.getValue().isSelected())
				.map(Map.Entry::getKey).collect(Collectors.toSet());

		Set<Tag> selectedTags = tagCheckBoxes.entrySet().stream().filter(entry -> entry.getValue().isSelected())
				.map(Map.Entry::getKey).collect(Collectors.toSet());

		if (selectedGenres.isEmpty() && selectedYears.isEmpty() && selectedTags.isEmpty()) {

			AlertManager.showError("Selezionare almeno un criterio.");
			return;
		}

		try {
			appContext.getMusicLibrary().createAutomaticPlaylist(name, selectedGenres, selectedYears, selectedTags);

			AlertManager.showInfo("Playlist automatica creata correttamente.");

			if (onSave != null) {
				onSave.run();
			}

			closeWindow();
		} catch (IllegalArgumentException e) {
			AlertManager.showError(e.getMessage());
		}
	}

	@FXML
	private void onCancel() {
		closeWindow();
	}

	private void closeWindow() {
		cancelButton.getScene().getWindow().hide();
	}
}
