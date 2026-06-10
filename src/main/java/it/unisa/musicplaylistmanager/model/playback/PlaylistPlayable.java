package it.unisa.musicplaylistmanager.model.playback;

import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;

public class PlaylistPlayable extends Playable {

	private final Playlist playlist;
	private PlaybackMode playbackMode;
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
		this.playbackMode = PlaybackMode.SEQUENTIAL;
		this.audioPlayer = AudioPlayer.getInstance();
		this.iteratorStrategy = new SequentialIteratorStrategy();
		this.iterator = new ConfigurablePlaylistIterator(playlist, iteratorStrategy);
		this.currentSong = null;
		this.subscribed = false;
	}


	@Override
	public String getTitle() {
		return playlist.getName();
	}


	@Override
	public void play() {
		subscribeToAudioPlayer();

		this.iterator = new ConfigurablePlaylistIterator(playlist, iteratorStrategy);
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

		this.iterator = new ConfigurablePlaylistIterator(playlist, iteratorStrategy);
		this.currentSong = null;
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

		if (!playNextSong()) {
			unsubscribeFromAudioPlayer();
			getEvents().notifyListeners(EventType.PLAYABLE_COMPLETED);
		}
	}

	@Override
	public void setPlaybackMode(PlaybackMode mode) {
		if (mode == null) {
			throw new IllegalArgumentException("Playback mode cannot be null.");
		}

		this.playbackMode = mode;

		if (mode == PlaybackMode.SEQUENTIAL) {
			setIteratorStrategy(new SequentialIteratorStrategy());
		} else if (mode == PlaybackMode.LOOP) {
			setIteratorStrategy(new LoopIteratorStrategy());
		} else if (mode == PlaybackMode.SHUFFLE) {
			setIteratorStrategy(new ShuffleIteratorStrategy());
		}
	}

	@Override
	public PlaybackMode getPlaybackMode() {
		return playbackMode;
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

	private boolean playNextSong() {
		currentSong = iterator.next();

		if (currentSong == null) {
			return false;
		}

		audioPlayer.play(currentSong.getFilePath());
		getEvents().notifyListeners(EventType.CURRENT_SONG_CHANGED);

		return true;
	}

	@Override
	public boolean skipToNextSong() {
		return playNextSong();
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