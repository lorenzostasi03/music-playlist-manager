package it.unisa.musicplaylistmanager.controller;

import it.unisa.musicplaylistmanager.app.AppContext;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.playback.AudioPlayer;
import it.unisa.musicplaylistmanager.model.playback.EventListener;
import it.unisa.musicplaylistmanager.model.playback.EventType;
import it.unisa.musicplaylistmanager.model.playback.Playable;
import it.unisa.musicplaylistmanager.model.playback.Player;
import it.unisa.musicplaylistmanager.model.playback.PlayerState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
	private ToggleButton loopTrackButton;
	@FXML
	private ToggleButton loopPlaylistButton;

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

		currentTimeLabel.setText("0:00");
		totalTimeLabel.setText("0:00");
		playPauseButton.setText("Play");

		progressSlider.setMin(0);
		progressSlider.setMax(1);
		progressSlider.setValue(0);
		progressSlider.setMouseTransparent(true);
		progressSlider.setFocusTraversable(false);

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
		if (eventType == EventType.PLAYABLE_COMPLETED) {
			unsubscribeFromCurrentPlayable();
			resetProgressTimeline();
			playPauseButton.setText("Play");
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

        playCountLabel.setText(currentSong.getPlayCount() + " riproduzioni totali.");
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

	private void subscribeToCurrentPlayable() {
		if (currentPlayable != null && !subscribedToCurrentPlayable) {
			currentPlayable.getEvents().subscribe(EventType.PLAYABLE_COMPLETED, this);
			subscribedToCurrentPlayable = true;
		}
	}

	private void unsubscribeFromCurrentPlayable() {
		if (currentPlayable != null && subscribedToCurrentPlayable) {
			currentPlayable.getEvents().unsubscribe(EventType.PLAYABLE_COMPLETED, this);
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
	}

	@FXML
	private void onSequential() {
	}

	@FXML
	private void onShuffle() {
	}

	@FXML
	private void onLoopTrack() {
	}

	@FXML
	private void onLoopPlaylist() {
	}
}
