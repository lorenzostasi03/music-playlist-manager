package it.unisa.musicplaylistmanager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;

public class PlaybackController {

    @FXML private Label trackTitleLabel;
    @FXML private Label trackArtistLabel;

    @FXML private Slider progressSlider;

    @FXML private Label currentTimeLabel;
    @FXML private Label totalTimeLabel;

    @FXML private Button previousButton;
    @FXML private Button playPauseButton;
    @FXML private Button nextButton;

    @FXML private ToggleButton sequentialModeButton;
    @FXML private ToggleButton shuffleModeButton;
    @FXML private ToggleButton loopTrackButton;
    @FXML private ToggleButton loopPlaylistButton;

    @FXML private Label playCountLabel;

    @FXML private VBox queueView;

    @FXML
    private void initialize() {
    }

    @FXML private void onPrevious() {
    }

    @FXML private void onPlayPause() {
    }

    @FXML private void onNext() {
    }

    @FXML private void onSequential() {
    }

    @FXML private void onShuffle() {
    }

    @FXML private void onLoopTrack() {
    }

    @FXML private void onLoopPlaylist() {
    }
}
