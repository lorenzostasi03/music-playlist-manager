package it.unisa.musicplaylistmanager.controller;

import it.unisa.musicplaylistmanager.app.AppContext;
import it.unisa.musicplaylistmanager.controller.command.*;
import it.unisa.musicplaylistmanager.controller.playlist.PlaylistFormController;
import it.unisa.musicplaylistmanager.exceptions.PersistenceException;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.playback.playable.PlaylistPlayable;
import it.unisa.musicplaylistmanager.util.AlertManager;
import it.unisa.musicplaylistmanager.util.DialogUtil;
import it.unisa.musicplaylistmanager.util.ViewSwitcher;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller della schermata principale dell'applicazione.
 *
 * Gestisce la visualizzazione delle playlist presenti nella libreria,
 * delle playlist più riprodotte e della playlist contenente i brani
 * più ascoltati. Inoltre consente la creazione, modifica, eliminazione,
 * riproduzione e accodamento delle playlist.
 */
public class HomeController implements Initializable {

    @FXML public Button undoCommandButton;
    @FXML private TextField searchBar;
    @FXML private MenuButton autoCreateBtn;
    @FXML private Button newPlaylistBtn;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Label countLabel;
    @FXML private Label emptyPlaylistLabel;
    @FXML private Label emptyTopPlaylistLabel;
    @FXML private ListView<PlaylistItem> playlistListView;
    @FXML private ListView<PlaylistItem> mostPlayedListView;

    private final AppContext appContext = AppContext.getInstance();
    private final CommandExecutor executor = CommandExecutor.getInstance();

    private List<Playlist> playlists;
    private Playlist topSongs;
    private List<Playlist> topPlaylists;

    private static final int TOP_SONGS = 10;
    private static final int TOP_PLAYLISTS = 3;

    /**
     * Inizializza i componenti grafici della schermata.
     *
     * Configura i controlli, inizializza le {@link ListView},
     * imposta i messaggi visualizzati in assenza di contenuti
     * e aggiorna i dati mostrati all'utente.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sortComboBox.getItems().setAll("Nome", "Numero brani", "Riproduzioni");
        sortComboBox.setValue("Nome");

        initListView(playlistListView);
        initListView(mostPlayedListView);

        initEmptyLabels();
        initSearch();

        initUndoButton();

        updateViews();
    }

    @FXML private void onAutoCreateByGenre() {

    }

    @FXML private void onAutoCreateByYear() {

    }

    @FXML private void onAutoCreateByArtist() {

    }

    @FXML private void onAutoCreateByTag() {

    }

    /**
     * Apre la finestra per la creazione di una nuova playlist.
     */
    @FXML private void onNewPlaylist() { openPlaylistForm(null); }

    @FXML private void onSortChanged() {
        refreshPlaylists();
    }

    @FXML private void onListViewClicked() {

    }

    /**
     * Aggiorna tutte le informazioni visualizzate nella schermata.
     *
     * Ricarica l'elenco delle playlist, aggiorna la sezione delle playlist
     * più riprodotte e il contatore totale delle playlist.
     */
    private void updateViews() {
        refreshPlaylists();
        refreshMostPlayed();
        countLabel.setText(playlists.size() + " playlist");
    }

    /**
     * Apre la schermata di dettaglio della playlist selezionata.
     *
     * @param item elemento selezionato
     */
    private void openPlaylistView(PlaylistItem item) {
        appContext.setSelectedPlaylist(item.playlist);
        appContext.setSelectedPlaylistReadOnly(item.readOnly);

        ViewSwitcher.switchTo("PlaylistView.fxml");
    }

    /**
     * Apre la finestra per la creazione di una nuova playlist
     * oppure per la modifica di una playlist esistente.
     *
     * @param playlist playlist da modificare; {@code null} per
     *                 creare una nuova playlist
     */
    private void openPlaylistForm(Playlist playlist) {
        String title = (playlist == null)
            ? "Nuova playlist"
            : "Rinomina playlist";

        DialogUtil.open(
            "PlaylistFormView.fxml",
            title,
            newPlaylistBtn.getScene().getWindow(),
            (PlaylistFormController controller) -> {
                controller.setPlaylistToEdit(playlist);
                controller.setOnSave(this::updateViews);
            }
        );
    }

    /**
     * Avvia la riproduzione della playlist selezionata.
     *
     * @param playlist playlist da riprodurre
     */
    private void playPlaylist(Playlist playlist) {
        if (playlist == null || playlist.isEmpty()) {
            AlertManager.showError("La playlist è vuota.");
            return;
        }

        appContext.playPlayable(new PlaylistPlayable(playlist));
        ViewSwitcher.switchTo("PlaybackView.fxml");
    }

    /**
     * Aggiunge una playlist alla coda di riproduzione.
     *
     * @param playlist playlist da accodare
     */
    private void enqueuePlaylist(Playlist playlist) {
        if (playlist == null || playlist.isEmpty()) {
            AlertManager.showError("La playlist è vuota.");
            return;
        }

        Command cmd = new AddPlayableToQueueCommand(appContext.getPlayer(), new PlaylistPlayable(playlist));
        executor.execute(cmd);
        AlertManager.showInfo("Playlist aggiunta alla coda.");
    }

    /**
     * Elimina la playlist selezionata dopo conferma dell'utente.
     *
     * @param playlist la playlist da eliminare
     */
    private void deletePlaylist(Playlist playlist) {
        if (playlist == null) return;

        boolean confirmed = AlertManager.showConfirmation(
            "Vuoi eliminare la playlist '" + playlist.getName() + "'?"
        );

        if (!confirmed) return;

        try {
            Command cmd = new RemovePlaylistCommand(appContext.getMusicLibrary(), appContext.getPlayer(), playlist);
            executor.execute(cmd);
            updateViews();
            AlertManager.showInfo("Playlist eliminata correttamente.");
        } catch (PersistenceException | IllegalArgumentException e) {
            AlertManager.showError(e.getMessage());
        }
    }

    /**
     * Ricarica dalla libreria musicale l'elenco completo delle playlist
     * e aggiorna la relativa list view.
     */
    private void refreshPlaylists() {
        playlists = new ArrayList<>(appContext.getMusicLibrary().searchPlaylists(searchBar.getText()));
        sortPlaylists();

        List<PlaylistItem> items = new ArrayList<>();

        playlists.forEach(playlist -> items.add(new PlaylistItem(playlist, false, true)));

        playlistListView.getItems().setAll(items);
    }

    /**
     * Ordina le playlist visualizzate secondo il criterio selezionato nella Home.
     */
    private void sortPlaylists() {
        String selectedSort = sortComboBox.getValue();

        if (selectedSort == null) {
            return;
        }

        Comparator<Playlist> byName =
            Comparator.comparing(Playlist::getName, String.CASE_INSENSITIVE_ORDER);

        Comparator<Playlist> comparator = switch (selectedSort) {
            case "Numero brani" -> Comparator.comparingInt(Playlist::size).reversed().thenComparing(byName);
            case "Riproduzioni" -> Comparator.comparingInt(Playlist::getPlayCount).reversed().thenComparing(byName);
            default -> byName;
        };

        playlists.sort(comparator);
    }

    /**
     * Collega la barra di ricerca all'elenco delle playlist visualizzate.
     */
    private void initSearch() {
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            refreshPlaylists();
            countLabel.setText(playlists.size() + " playlist");
        });
    }

    /**
     * Inizializza il pulsante per annullare l'ultima operazione effettuata.
     * Il pulsante è visibile e cliccabile solo se sono presenti operazioni da annullare.
     */
    private void initUndoButton() {
        ReadOnlyBooleanProperty canUndo = executor.canUndoProperty();

        undoCommandButton.visibleProperty().bind(canUndo);
        undoCommandButton.managedProperty().bind(canUndo);
        undoCommandButton.disableProperty().bind(canUndo.not());
    }

    /**
     * Aggiorna la sezione dedicata ai contenuti più riprodotti.
     *
     * Include la playlist dei {@value HomeController#TOP_SONGS} brani più ascoltati
     * e le {@value HomeController#TOP_PLAYLISTS} playlist più riprodotte.
     */
    private void refreshMostPlayed() {
        List<PlaylistItem> items = new ArrayList<>();

        topSongs = buildTopSongsPlaylist();
        if (topSongs != null) {
            items.add(new PlaylistItem(topSongs, true, false));
        }

        topPlaylists = getTopPlaylists();
        if (topPlaylists != null) {
            topPlaylists.forEach(playlist -> items.add(new PlaylistItem(playlist, false, true)));
        }

        mostPlayedListView.getItems().setAll(items);
    }

    /**
     * Costruisce una playlist temporanea contenente i brani
     * più riprodotti presenti nella libreria.
     *
     * @return una playlist contenente i primi {@value HomeController#TOP_SONGS}
     *         brani più ascoltati oppure {@code null} se non esistono
     */
    private Playlist buildTopSongsPlaylist() {
        List<Song> songs = appContext.getMusicLibrary().getTopSongs(TOP_SONGS);
        if (songs.isEmpty()) return null;

        Playlist playlist = new Playlist("Top " + TOP_SONGS);
        songs.forEach(playlist::addSong);
        return playlist;
    }

    /**
     * Recupera le playlist più riprodotte presenti nella libreria.
     *
     * @return una lista contenente le prime {@code TOP_PLAYLISTS}
     *         playlist più ascoltate oppure {@code null} se non esistono
     */
    private List<Playlist> getTopPlaylists() {
        List<Playlist> list = appContext.getMusicLibrary().getTopPlaylists(TOP_PLAYLISTS);
        return list.isEmpty() ? null : new ArrayList<>(list);
    }

    /**
     * Configura le etichette visualizzate quando le liste risultano vuote.
     */
    private void initEmptyLabels() {
        emptyPlaylistLabel.setText("Nessuna playlist trovata.");
        emptyTopPlaylistLabel.setText("Riproduci un brano o una playlist.");

        emptyPlaylistLabel.visibleProperty().bind(Bindings.isEmpty(playlistListView.getItems()));
        emptyPlaylistLabel.managedProperty().bind(emptyPlaylistLabel.visibleProperty());

        emptyTopPlaylistLabel.visibleProperty().bind(Bindings.isEmpty(mostPlayedListView.getItems()));
        emptyTopPlaylistLabel.managedProperty().bind(emptyTopPlaylistLabel.visibleProperty());
    }

    /**
     * Configura le liste di playlist da visualizzare.
     *
     * @param listView lista da inizializzare.
     */
    private void initListView(ListView<PlaylistItem> listView) {
        listView.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(PlaylistItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                setGraphic(createPlaylistRow(item));
            }
        });
    }

    /**
     * Crea una riga grafica per rappresentare una playlist
     * all'interno di una list view.
     *
     * @param item elemento da rappresentare
     * @return un oggetto HBox formattato contenente le informazioni
     *          e i comandi della playlist
     */
    private HBox createPlaylistRow(PlaylistItem item) {
        Playlist playlist = item.playlist;
        boolean readOnly = item.readOnly;

        Label nameLabel = new Label(playlist.getName());
        nameLabel.getStyleClass().add("row-title");
        nameLabel.setPrefWidth(140);

        Label songsLabel = new Label(String.valueOf(playlist.size()));
        songsLabel.getStyleClass().add("row-meta");
        songsLabel.setPrefWidth(50);

        Label durationLabel = new Label(formatDuration(playlist));
        durationLabel.getStyleClass().add("row-meta");
        durationLabel.setPrefWidth(80);

        Label playCountLabel = new Label(
            item.showPlayCount ? String.valueOf(playlist.getPlayCount()) : ""
        );
        playCountLabel.getStyleClass().add("row-meta");
        playCountLabel.setPrefWidth(90);

        Button playButton = createButton("▶", "Riproduci playlist", () -> playPlaylist(playlist));
        Button enqueueButton = createButton("+", "Aggiungi playlist alla coda", () -> enqueuePlaylist(playlist));

        HBox row;

        if (!readOnly) {
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
        row.setOnMouseClicked(event -> openPlaylistView(item));

        return row;
    }

    /**
     * Crea un pulsante di azione standard utilizzato nelle righe
     * delle playlist.
     *
     * @param text testo visualizzato sul pulsante
     * @param tooltip descrizione mostrata al passaggio del mouse
     * @param action operazione eseguita alla pressione del pulsante
     * @return pulsante configurato
     */
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
     * Calcola e formatta la durata complessiva di una playlist.
     *
     * @param playlist playlist di cui calcolare la durata
     * @return durata espressa nel formato {@code mm:ss}
     */
    private String formatDuration(Playlist playlist) {
        int totalSeconds = playlist.getSongs().stream().mapToInt(Song::getDuration).sum();
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    @FXML
    public void onUndoCommand(ActionEvent actionEvent) {
        executor.undo();
        AlertManager.showInfo("L'operazione è stata annullata.");
        updateViews();
    }

    /**
     * Rappresenta un elemento visualizzato nelle liste della schermata Home.
     *
     * Associa una playlist alle informazioni necessarie per determinarne
     * il comportamento nell'interfaccia grafica, come la possibilità di
     * modifica e la visualizzazione del numero di riproduzioni.
     */
    private static class PlaylistItem {
        final Playlist playlist;
        final boolean readOnly;
        final boolean showPlayCount;

        /**
         * Crea un nuovo elemento per la visualizzazione di una playlist.
         *
         * @param playlist playlist associata all'elemento
         * @param readOnly {@code true} se la playlist non può essere modificata,
         *                 {@code false} altrimenti
         * @param showPlayCount {@code true} se deve essere mostrato il numero
         *                      di riproduzioni della playlist,
         *                      {@code false} altrimenti
         */
        PlaylistItem(Playlist playlist, boolean readOnly, boolean showPlayCount) {
            this.playlist = playlist;
            this.readOnly = readOnly;
            this.showPlayCount = showPlayCount;
        }
    }

}
