package it.unisa.musicplaylistmanager.model.playback;

import it.unisa.musicplaylistmanager.model.entity.Song;
import java.util.ArrayList;
import java.util.List;

/**
 * Iteratore configurabile per la riproduzione di una playlist.
 */
public class ConfigurablePlaylistIterator implements PlaylistIterator {

	private final List<Song> songsSnapshot;
	private int currentIndex;
	private PlaylistIteratorStrategy strategy;

	public ConfigurablePlaylistIterator(List<Song> songs, PlaylistIteratorStrategy strategy) {
		if (songs == null) {
			throw new IllegalArgumentException("Songs list cannot be null.");
		}

		if (strategy == null) {
			throw new IllegalArgumentException("Playlist iterator strategy cannot be null.");
		}

		this.songsSnapshot = new ArrayList<>(songs);
		this.strategy = strategy;
		this.currentIndex = -1;

		this.strategy.reset(songsSnapshot.size(), currentIndex);
	}

	@Override
	public Song next() {
		int nextIndex = strategy.nextIndex(currentIndex, songsSnapshot.size());

		if (nextIndex == -1) {
			return null;
		}

		currentIndex = nextIndex;
		return songsSnapshot.get(currentIndex);
	}

	@Override
	public void setStrategy(PlaylistIteratorStrategy strategy) {
		if (strategy == null) {
			throw new IllegalArgumentException("Playlist iterator strategy cannot be null.");
		}

		this.strategy = strategy;
		this.strategy.reset(songsSnapshot.size(), currentIndex);
	}

	@Override
	public int getCurrentIndex() {
		return currentIndex;
	}
}