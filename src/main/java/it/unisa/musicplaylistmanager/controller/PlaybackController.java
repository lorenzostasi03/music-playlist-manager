package it.unisa.musicplaylistmanager.controller;

import it.unisa.musicplaylistmanager.app.AppContext;
import it.unisa.musicplaylistmanager.controller.command.Command;
import it.unisa.musicplaylistmanager.controller.command.CommandExecutor;
import it.unisa.musicplaylistmanager.controller.command.RemovePlayableFromQueueCommand;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.playback.events.EventListener;
import it.unisa.musicplaylistmanager.model.playback.events.EventType;
import it.unisa.musicplaylistmanager.model.playback.mode.PlaybackMode;
import it.unisa.musicplaylistmanager.model.playback.playable.Playable;
import it.unisa.musicplaylistmanager.model.playback.player.AudioPlayer;
import it.unisa.musicplaylistmanager.model.playback.player.Player;
import it.unisa.musicplaylistmanager.model.playback.player.PlayerState;
import it.unisa.musicplaylistmanager.util.AlertManager;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controller della vista dedicata alla riproduzione musicale.
 *
 * <p>
 * Gestisce i controlli di riproduzione, la modalità di avanzamento, la
 * visualizzazione del brano corrente, la barra di avanzamento e la coda dei
 * playable.
 * </p>
 */
public class PlaybackController implements EventListener {

	@FXML
	private Label trackTitleLabel;
	@FXML
	private Label trackArtistLabel;
	@FXML
	private Slider progressSlider;
	@FXML
	private Label currentTimeLabel;
	@FXML
	private Label totalTimeLabel;
	@FXML
	private Button playPauseButton;

	@FXML
	private ToggleButton sequentialModeButton;
	@FXML
	private ToggleButton shuffleModeButton;
	@FXML
	private ToggleButton loopModeButton;

	@FXML
	private Label playCountLabel;

	@FXML
	private VBox queueView;

	private Player player;
	private AudioPlayer audioPlayer;
	private Playable currentPlayable;
	private boolean subscribedToCurrentPlayable;

	private Timeline progressTimeline;

	private final AppContext appContext = AppContext.getInstance();
	private final CommandExecutor executor = CommandExecutor.getInstance();

	/**
	 * Inizializza il controller e configura lo stato iniziale della vista.
	 *
	 * <p>
	 * Recupera il player e il playable corrente dall' {@link AppContext}, si
	 * registra agli eventi del player, configura i controlli grafici e aggiorna le
	 * informazioni relative alla riproduzione e alla coda.
	 * </p>
	 */
	@FXML
	private void initialize() {
		player = appContext.getPlayer();
		audioPlayer = AudioPlayer.getInstance();
		currentPlayable = appContext.getCurrentPlayable();
		subscribedToCurrentPlayable = false;

		player.getEvents().subscribe(EventType.CURRENT_PLAYABLE_CHANGED, this);
		player.getEvents().subscribe(EventType.QUEUE_CHANGED, this);

		currentTimeLabel.setText("0:00");
		totalTimeLabel.setText("0:00");
		playPauseButton.setText("Play");

		progressSlider.setMin(0);
		progressSlider.setMax(1);
		progressSlider.setValue(0);
		progressSlider.setMouseTransparent(true);
		progressSlider.setFocusTraversable(false);

		queueView.setSpacing(8);
		playCountLabel.setText("0 riproduzioni totali");

		if (currentPlayable != null) {
			subscribeToCurrentPlayable();
			updatePlayableInfo(currentPlayable);
			updatePlayPauseButton();

			updateProgress();

			if (player.getState() == PlayerState.PLAYING) {
				startProgressTimeline();
			}
		}

		updatePlaybackModeButtons();
		updateQueueView();
	}

	/**
	 * Avvia, mette in pausa oppure riprende la riproduzione corrente in base allo
	 * stato del player.
	 */
	@FXML
	private void onPlayPause() {
		if (currentPlayable == null) {
			return;
		}

		if (player.getState() == PlayerState.STOPPED) {
			subscribeToCurrentPlayable();
			resetProgressView();
			player.play(currentPlayable);
			startProgressTimeline();
			playPauseButton.setText("Pausa");
		} else if (player.getState() == PlayerState.PLAYING) {
			player.pause();
			pauseProgressTimeline();
			playPauseButton.setText("Riprendi");
		} else if (player.getState() == PlayerState.PAUSED) {
			player.resume();
			resumeProgressTimeline();
			playPauseButton.setText("Pausa");
		}
	}

	/**
	 * Gestisce gli eventi notificati dal player o dal playable corrente.
	 *
	 * <p>
	 * In base al tipo di evento aggiorna il playable corrente, le informazioni del
	 * brano, la barra di avanzamento oppure la visualizzazione della coda.
	 * </p>
	 *
	 * @param eventType
	 *            tipo di evento ricevuto
	 */
	@Override
	public void update(EventType eventType) {
		if (eventType == EventType.CURRENT_PLAYABLE_CHANGED) {
			handleCurrentPlayableChanged();
			updateQueueView();
			return;
		}

		if (eventType == EventType.QUEUE_CHANGED) {
			updateQueueView();
			return;
		}

		if (eventType == EventType.CURRENT_SONG_CHANGED) {
			updatePlayableInfo(currentPlayable);
			updateProgress();
			updateQueueView();
			return;
		}

		if (eventType == EventType.PLAYABLE_COMPLETED) {
			unsubscribeFromCurrentPlayable();
			resetProgressTimeline();
			playPauseButton.setText("Play");
			updateQueueView();
		}
	}

	/**
	 * Aggiorna le informazioni grafiche relative al brano corrente del playable.
	 *
	 * @param playable
	 *            playable di cui visualizzare il brano corrente
	 */
	private void updatePlayableInfo(Playable playable) {
		Song currentSong = playable.getCurrentSong();

		if (currentSong == null) {
			trackTitleLabel.setText("Nessuna traccia");
			trackArtistLabel.setText("");
			totalTimeLabel.setText("0:00");
			currentTimeLabel.setText("0:00");
			progressSlider.setMax(1);
			progressSlider.setValue(0);
			playCountLabel.setText("0 riproduzioni totali");
			return;
		}

		trackTitleLabel.setText(currentSong.getTitle());
		trackArtistLabel.setText(currentSong.getAuthor());

		currentTimeLabel.setText("0:00");
		totalTimeLabel.setText("0:00");

		progressSlider.setMax(1);
		progressSlider.setValue(0);

		playCountLabel.setText(currentSong.getPlayCount() + " riproduzioni totali");
	}

	/**
	 * Aggiorna il testo del pulsante play/pausa in base allo stato corrente del
	 * player.
	 */
	private void updatePlayPauseButton() {
		if (player.getState() == PlayerState.PLAYING) {
			playPauseButton.setText("Pausa");
		} else if (player.getState() == PlayerState.PAUSED) {
			playPauseButton.setText("Riprendi");
		} else {
			playPauseButton.setText("Play");
		}
	}

	/**
	 * Imposta la modalità di riproduzione del playable corrente.
	 *
	 * @param mode
	 *            modalità di riproduzione da applicare
	 */
	private void setCurrentPlaybackMode(PlaybackMode mode) {
		if (currentPlayable == null) {
			sequentialModeButton.setSelected(true);
			return;
		}

		currentPlayable.setPlaybackMode(mode);
		updatePlaybackModeButtons();
	}

	/**
	 * Aggiorna lo stato dei pulsanti relativi alle modalità di riproduzione.
	 */
	private void updatePlaybackModeButtons() {
		if (currentPlayable == null) {
			sequentialModeButton.setSelected(true);
			return;
		}

		PlaybackMode mode = currentPlayable.getPlaybackMode();

		if (mode == PlaybackMode.SEQUENTIAL) {
			sequentialModeButton.setSelected(true);
		} else if (mode == PlaybackMode.SHUFFLE) {
			shuffleModeButton.setSelected(true);
		} else if (mode == PlaybackMode.LOOP) {
			loopModeButton.setSelected(true);
		}
	}

	/**
	 * Gestisce la sostituzione del playable corrente.
	 *
	 * <p>
	 * Rimuove le sottoscrizioni dal precedente playable, recupera quello nuovo e
	 * aggiorna tutte le informazioni della vista. Se non è presente alcun playable,
	 * ripristina lo stato iniziale dei controlli.
	 * </p>
	 */
	private void handleCurrentPlayableChanged() {
		unsubscribeFromCurrentPlayable();

		currentPlayable = player.getCurrentPlayable();
		updatePlaybackModeButtons();

		if (currentPlayable == null) {
			resetProgressTimeline();
			trackTitleLabel.setText("Nessuna traccia");
			trackArtistLabel.setText("");
			totalTimeLabel.setText("0:00");
			currentTimeLabel.setText("0:00");
			playCountLabel.setText("0 riproduzioni totali");
			playPauseButton.setText("Play");
			return;
		}

		subscribeToCurrentPlayable();
		updatePlaybackModeButtons();
		updatePlayableInfo(currentPlayable);
		updatePlayPauseButton();
		updateProgress();

		if (player.getState() == PlayerState.PLAYING) {
			startProgressTimeline();
		} else {
			stopProgressTimeline();
		}
	}

	/**
	 * Ricostruisce la sezione grafica contenente il playable corrente e gli
	 * elementi presenti nella coda.
	 */
	private void updateQueueView() {
		queueView.getChildren().clear();

		Playable current = player.getCurrentPlayable();

		queueView.getChildren().add(createQueueHeaderLabel("In riproduzione"));

		if (current == null) {
			queueView.getChildren().add(createPlaceholderLabel("Nessun elemento in riproduzione"));
		} else {
			queueView.getChildren().add(createCurrentQueueItemLabel(current.getTitle()));
		}

		queueView.getChildren().add(createQueueHeaderLabel("Coda"));

		List<Playable> queuedPlayables = player.getQueueSnapshot();

		if (queuedPlayables.isEmpty()) {
			queueView.getChildren().add(createPlaceholderLabel("Coda vuota"));
			return;
		}

		for (int i = 0; i < queuedPlayables.size(); i++) {
			Playable playable = queuedPlayables.get(i);
			queueView.getChildren().add(createQueueItemLabel(i, playable));
		}
	}

	/**
	 * Crea un'etichetta utilizzata come intestazione di una sezione della coda.
	 *
	 * @param text
	 *            testo dell'intestazione
	 * @return etichetta configurata
	 */
	private Label createQueueHeaderLabel(String text) {
		Label label = new Label(text);
		label.setMaxWidth(Double.MAX_VALUE);
		label.setStyle(
				"-fx-text-fill: #FFFFFF; -fx-font-size: 13px; -fx-font-weight: bold; " + "-fx-padding: 12 0 4 0;");
		return label;
	}

	/**
	 * Crea l'etichetta che rappresenta il playable attualmente in riproduzione.
	 *
	 * @param text
	 *            titolo del playable corrente
	 * @return etichetta configurata
	 */
	private Label createCurrentQueueItemLabel(String text) {
		Label label = new Label(text);
		label.setWrapText(true);
		label.setPrefWidth(220);
		label.setMaxWidth(220);
		label.setStyle("-fx-text-fill: #1DB954; -fx-font-size: 12px; -fx-font-weight: bold; "
				+ "-fx-padding: 8 10 8 10; -fx-background-color: #121212; " + "-fx-background-radius: 6;");
		return label;
	}

	/**
	 * Crea una riga della coda contenente il titolo del playable e il pulsante per
	 * rimuoverlo.
	 *
	 * @param index
	 *            posizione del playable nella coda
	 * @param playable
	 *            playable rappresentato dalla riga
	 * @return contenitore grafico della riga
	 */
	private HBox createQueueItemLabel(int index, Playable playable) {
		Label label = new Label((index + 1) + ". " + playable.getTitle());
		label.setPrefWidth(150);
		label.setMaxWidth(150);

		Button deleteButton = new Button("x");
		deleteButton.getStyleClass().add("row-action");
		deleteButton.setMinWidth(25);
		deleteButton.setPrefWidth(25);
		deleteButton.setMaxWidth(25);
		deleteButton.setFocusTraversable(false);

		deleteButton.setOnAction(e -> removeFromQueue(playable));

		HBox box = new HBox(4, label, deleteButton);
		HBox.setHgrow(label, Priority.ALWAYS);
		box.setMaxWidth(Double.MAX_VALUE);

		return box;
	}

	/**
	 * Crea un'etichetta segnaposto utilizzata quando non sono presenti elementi da
	 * mostrare.
	 *
	 * @param text
	 *            testo da visualizzare
	 * @return etichetta segnaposto
	 */
	private Label createPlaceholderLabel(String text) {
		Label label = new Label(text);
		label.setMaxWidth(Double.MAX_VALUE);

		return label;
	}

	/**
	 * Registra il controller agli eventi del playable corrente, evitando
	 * sottoscrizioni duplicate.
	 */
	private void subscribeToCurrentPlayable() {
		if (currentPlayable != null && !subscribedToCurrentPlayable) {
			currentPlayable.getEvents().subscribe(EventType.PLAYABLE_COMPLETED, this);
			currentPlayable.getEvents().subscribe(EventType.CURRENT_SONG_CHANGED, this);
			subscribedToCurrentPlayable = true;
		}
	}

	/**
	 * Rimuove il controller dagli eventi del playable corrente.
	 */
	private void unsubscribeFromCurrentPlayable() {
		if (currentPlayable != null && subscribedToCurrentPlayable) {
			currentPlayable.getEvents().unsubscribe(EventType.PLAYABLE_COMPLETED, this);
			currentPlayable.getEvents().unsubscribe(EventType.CURRENT_SONG_CHANGED, this);
			subscribedToCurrentPlayable = false;
		}
	}

	/**
	 * Avvia l'aggiornamento periodico della barra di avanzamento.
	 *
	 * <p>
	 * La posizione viene aggiornata ogni 250 millisecondi.
	 * </p>
	 */
	private void startProgressTimeline() {
		stopProgressTimeline();

		progressTimeline = new Timeline(new KeyFrame(Duration.millis(250), event -> updateProgress()));

		progressTimeline.setCycleCount(Timeline.INDEFINITE);
		progressTimeline.play();
	}

	/**
	 * Aggiorna la barra di avanzamento e le etichette temporali utilizzando i dati
	 * forniti dall'audio player.
	 */
	private void updateProgress() {
		double currentSeconds = audioPlayer.getCurrentTimeSeconds();
		double totalSeconds = audioPlayer.getTotalDurationSeconds();

		if (totalSeconds <= 0) {
			return;
		}

		progressSlider.setMax(totalSeconds);
		progressSlider.setValue(currentSeconds);

		currentTimeLabel.setText(formatTime((int) currentSeconds));
		totalTimeLabel.setText(formatTime((int) totalSeconds));
	}

	/**
	 * Mette in pausa l'aggiornamento periodico della barra di avanzamento.
	 */
	private void pauseProgressTimeline() {
		if (progressTimeline != null) {
			progressTimeline.pause();
		}
	}

	/**
	 * Riprende l'aggiornamento periodico della barra di avanzamento oppure crea una
	 * nuova timeline se non è ancora presente.
	 */
	private void resumeProgressTimeline() {
		if (progressTimeline == null) {
			startProgressTimeline();
		} else {
			progressTimeline.play();
		}
	}

	/**
	 * Arresta e rimuove la timeline utilizzata per aggiornare l'avanzamento.
	 */
	private void stopProgressTimeline() {
		if (progressTimeline != null) {
			progressTimeline.stop();
			progressTimeline = null;
		}
	}

	/**
	 * Arresta la timeline e ripristina la visualizzazione dell'avanzamento.
	 */
	private void resetProgressTimeline() {
		stopProgressTimeline();
		resetProgressView();
	}

	/**
	 * Riporta la barra di avanzamento e il tempo corrente ai valori iniziali.
	 */
	private void resetProgressView() {
		progressSlider.setValue(0);
		currentTimeLabel.setText("0:00");
	}

	/**
	 * Converte un tempo espresso in secondi nel formato minuti e secondi.
	 *
	 * @param seconds
	 *            numero totale di secondi
	 * @return tempo formattato nel formato {@code m:ss}
	 */
	private String formatTime(int seconds) {
		int minutes = seconds / 60;
		int remainingSeconds = seconds % 60;
		return String.format("%d:%02d", minutes, remainingSeconds);
	}

	/**
	 * Rimuove un playable dalla coda attraverso un comando annullabile.
	 *
	 * @param playable
	 *            playable da rimuovere
	 */
	private void removeFromQueue(Playable playable) {
		Command cmd = new RemovePlayableFromQueueCommand(player, playable);
		executor.execute(cmd);
		AlertManager.showInfo("L'elemento è stato rimosso dalla coda.");
		updateQueueView();
	}

	/**
	 * Avanza al brano successivo del playable corrente.
	 */
	@FXML
	private void onNext() {
		player.skipSong();
	}

	/**
	 * Interrompe il playable corrente e avvia il successivo presente nella coda.
	 */
	@FXML
	private void onSkipPlayable() {
		player.skipPlayable();
	}

	/**
	 * Imposta la modalità di riproduzione sequenziale.
	 */
	@FXML
	private void onSequential() {
		setCurrentPlaybackMode(PlaybackMode.SEQUENTIAL);
	}

	/**
	 * Imposta la modalità di riproduzione casuale.
	 */
	@FXML
	private void onShuffle() {
		setCurrentPlaybackMode(PlaybackMode.SHUFFLE);
	}

	/**
	 * Imposta la modalità di riproduzione ciclica.
	 */
	@FXML
	private void onLoop() {
		setCurrentPlaybackMode(PlaybackMode.LOOP);
	}

	/**
	 * Svuota completamente la coda di riproduzione.
	 */
	@FXML
	private void onClearQueue() {
		player.clearQueue();
		AlertManager.showInfo("La coda è stata svuotata.");
		updateQueueView();
	}
}
