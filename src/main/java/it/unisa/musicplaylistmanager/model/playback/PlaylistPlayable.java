package it.unisa.musicplaylistmanager.model.playback;

import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;

public class PlaylistPlayable extends Playable {

	private final Playlist playlist;
	private final AudioPlayer audioPlayer;

	private int currentIndex;
	private boolean subscribed;

	public PlaylistPlayable(Playlist playlist) {
		if (playlist == null) {
			throw new IllegalArgumentException("Playlist cannot be null.");
		}

		if (playlist.size() == 0) {
			throw new IllegalArgumentException("Playlist cannot be empty.");
		}

		this.playlist = playlist;
		this.audioPlayer = AudioPlayer.getInstance();
		this.currentIndex = 0;
		this.subscribed = false;
	}

	@Override
	public void play() {
		subscribeToAudioPlayer();

		currentIndex = 0;
		playCurrentSong();
	}

	@Override
	public void pause() {
		audioPlayer.pause();
	}

	@Override
	public void resume() {
		audioPlayer.resume();
	}

	@Override
	public void stop() {
		audioPlayer.stop();
		unsubscribeFromAudioPlayer();
		currentIndex = 0;
	}

	@Override
	public Song getCurrentSong() {
		if (playlist.size() == 0 || currentIndex < 0 || currentIndex >= playlist.size()) {
			return null;
		}

		return playlist.getSongAt(currentIndex);
	}

	@Override
	public void update(EventType eventType) {
		if (eventType != EventType.AUDIO_COMPLETED) {
			return;
		}

		if (hasNextSong()) {
			currentIndex++;
			playCurrentSong();
			getEvents().notifyListeners(EventType.CURRENT_SONG_CHANGED);
		} else {
			unsubscribeFromAudioPlayer();
			getEvents().notifyListeners(EventType.PLAYABLE_COMPLETED);
		}
	}

	private void playCurrentSong() {
		Song currentSong = getCurrentSong();

		if (currentSong == null) {
			unsubscribeFromAudioPlayer();
			getEvents().notifyListeners(EventType.PLAYABLE_COMPLETED);
			return;
		}

		audioPlayer.play(currentSong.getFilePath());
	}

	private boolean hasNextSong() {
		return currentIndex + 1 < playlist.size();
	}

	private void subscribeToAudioPlayer() {
		if (!subscribed) {
			audioPlayer.getEvents().subscribe(EventType.AUDIO_COMPLETED, this);
			subscribed = true;
		}
	}

	private void unsubscribeFromAudioPlayer() {
		if (subscribed) {
			audioPlayer.getEvents().unsubscribe(EventType.AUDIO_COMPLETED, this);
			subscribed = false;
		}
	}
}
