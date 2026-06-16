package it.unisa.musicplaylistmanager.controller;

import it.unisa.musicplaylistmanager.app.AppContext;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.playback.player.AudioPlayer;
import it.unisa.musicplaylistmanager.model.playback.events.EventListener;
import it.unisa.musicplaylistmanager.model.playback.events.EventType;
import it.unisa.musicplaylistmanager.model.playback.playable.Playable;
import it.unisa.musicplaylistmanager.model.playback.player.Player;
import it.unisa.musicplaylistmanager.model.playback.player.PlayerState;
import it.unisa.musicplaylistmanager.model.playback.mode.PlaybackMode;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

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
	private Button previousButton;
	@FXML
	private Button playPauseButton;
	@FXML
	private Button nextButton;

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

	private AppContext appContext = AppContext.getInstance();

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

	private void updatePlayPauseButton() {
		if (player.getState() == PlayerState.PLAYING) {
			playPauseButton.setText("Pausa");
		} else if (player.getState() == PlayerState.PAUSED) {
			playPauseButton.setText("Riprendi");
		} else {
			playPauseButton.setText("Play");
		}
	}

	private void setCurrentPlaybackMode(PlaybackMode mode) {
		if (currentPlayable == null) {
			sequentialModeButton.setSelected(true);
			return;
		}

		currentPlayable.setPlaybackMode(mode);
		updatePlaybackModeButtons();
	}

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

	private void updateQueueView() {
		queueView.getChildren().clear();

		Playable current = player.getCurrentPlayable();

		queueView.getChildren().add(createQueueHeaderLabel("In riproduzione"));

		if (current == null) {
			queueView.getChildren().add(createQueueItemLabel("Nessun elemento in riproduzione"));
		} else {
			queueView.getChildren().add(createCurrentQueueItemLabel(current.getTitle()));
		}

		queueView.getChildren().add(createQueueHeaderLabel("Coda"));

		List<Playable> queuedPlayables = player.getQueueSnapshot();

		if (queuedPlayables.isEmpty()) {
			queueView.getChildren().add(createQueueItemLabel("Coda vuota"));
			return;
		}

		for (int i = 0; i < queuedPlayables.size(); i++) {
			Playable playable = queuedPlayables.get(i);
			String text = (i + 1) + ". " + playable.getTitle();
			queueView.getChildren().add(createQueueItemLabel(text));
		}
	}

	private Label createQueueHeaderLabel(String text) {
		Label label = new Label(text);
		label.setMaxWidth(Double.MAX_VALUE);
		label.setStyle(
				"-fx-text-fill: #FFFFFF; -fx-font-size: 13px; -fx-font-weight: bold; " + "-fx-padding: 12 0 4 0;");
		return label;
	}

	private Label createCurrentQueueItemLabel(String text) {
		Label label = new Label(text);
		label.setWrapText(true);
		label.setPrefWidth(220);
		label.setMaxWidth(220);
		label.setStyle("-fx-text-fill: #1DB954; -fx-font-size: 12px; -fx-font-weight: bold; "
				+ "-fx-padding: 8 10 8 10; -fx-background-color: #121212; -fx-background-radius: 6;");
		return label;
	}

	private Label createQueueItemLabel(String text) {
		Label label = new Label(text);
		label.setWrapText(true);
		label.setPrefWidth(220);
		label.setMaxWidth(220);
		label.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 12px; "
				+ "-fx-padding: 8 10 8 10; -fx-background-color: #121212; -fx-background-radius: 6;");
		return label;
	}

	private void subscribeToCurrentPlayable() {
		if (currentPlayable != null && !subscribedToCurrentPlayable) {
			currentPlayable.getEvents().subscribe(EventType.PLAYABLE_COMPLETED, this);
			currentPlayable.getEvents().subscribe(EventType.CURRENT_SONG_CHANGED, this);
			subscribedToCurrentPlayable = true;
		}
	}

	private void unsubscribeFromCurrentPlayable() {
		if (currentPlayable != null && subscribedToCurrentPlayable) {
			currentPlayable.getEvents().unsubscribe(EventType.PLAYABLE_COMPLETED, this);
			currentPlayable.getEvents().unsubscribe(EventType.CURRENT_SONG_CHANGED, this);
			subscribedToCurrentPlayable = false;
		}
	}

	private void startProgressTimeline() {
		stopProgressTimeline();

		progressTimeline = new Timeline(new KeyFrame(Duration.millis(250), event -> updateProgress()));

		progressTimeline.setCycleCount(Timeline.INDEFINITE);
		progressTimeline.play();
	}

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

	private void pauseProgressTimeline() {
		if (progressTimeline != null) {
			progressTimeline.pause();
		}
	}

	private void resumeProgressTimeline() {
		if (progressTimeline == null) {
			startProgressTimeline();
		} else {
			progressTimeline.play();
		}
	}

	private void stopProgressTimeline() {
		if (progressTimeline != null) {
			progressTimeline.stop();
			progressTimeline = null;
		}
	}

	private void resetProgressTimeline() {
		stopProgressTimeline();
		resetProgressView();
	}

	private void resetProgressView() {
		progressSlider.setValue(0);
		currentTimeLabel.setText("0:00");
	}

	private String formatTime(int seconds) {
		int minutes = seconds / 60;
		int remainingSeconds = seconds % 60;
		return String.format("%d:%02d", minutes, remainingSeconds);
	}

	@FXML
	private void onPrevious() {
	}

	@FXML
	private void onNext() {
		player.skipSong();
	}

	@FXML
	private void onSkipPlayable() {
		player.skipPlayable();
	}

	@FXML
	private void onSequential() {
		setCurrentPlaybackMode(PlaybackMode.SEQUENTIAL);
	}

	@FXML
	private void onShuffle() {
		setCurrentPlaybackMode(PlaybackMode.SHUFFLE);
	}

	@FXML
	private void onLoop() {
		setCurrentPlaybackMode(PlaybackMode.LOOP);
	}

    @FXML
    public void onUndoCommand(ActionEvent actionEvent) {
    }
}
