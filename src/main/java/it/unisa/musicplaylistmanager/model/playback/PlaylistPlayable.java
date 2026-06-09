package it.unisa.musicplaylistmanager.model.playback;

import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;

public class PlaylistPlayable extends Playable {

	private final Playlist playlist;
	private final AudioPlayer audioPlayer;

	private PlaylistIterator iterator;
	private PlaylistIteratorStrategy iteratorStrategy;
	private Song currentSong;
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
		this.iteratorStrategy = new SequentialIteratorStrategy();
		this.iterator = null;
		this.currentSong = null;
		this.subscribed = false;
	}

	@Override
	public void play() {
		subscribeToAudioPlayer();

		iterator = new ConfigurablePlaylistIterator(playlist.getSongs(), iteratorStrategy);
		playNextSong();
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
		iterator = null;
		currentSong = null;
	}

	@Override
	public Song getCurrentSong() {
		return currentSong;
	}

	@Override
	public void update(EventType eventType) {
		if (eventType != EventType.AUDIO_COMPLETED) {
			return;
		}

		playNextSong();
	}

	public void setIteratorStrategy(PlaylistIteratorStrategy strategy) {
		if (strategy == null) {
			throw new IllegalArgumentException("Playlist iterator strategy cannot be null.");
		}

		this.iteratorStrategy = strategy;

		if (iterator != null) {
			iterator.setStrategy(strategy);
		}
	}

	private void playNextSong() {
		if (iterator == null) {
			iterator = new ConfigurablePlaylistIterator(playlist.getSongs(), iteratorStrategy);
		}

		currentSong = iterator.next();

		if (currentSong == null) {
			unsubscribeFromAudioPlayer();
			getEvents().notifyListeners(EventType.PLAYABLE_COMPLETED);
			return;
		}

		audioPlayer.play(currentSong.getFilePath());
		getEvents().notifyListeners(EventType.CURRENT_SONG_CHANGED);
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