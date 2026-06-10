package it.unisa.musicplaylistmanager.model.playback;

import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Iteratore configurabile per la riproduzione di una playlist.
 *
 * <p>
 * L'iteratore lavora sulla playlist aggiornata e mantiene lo stato della
 * sessione di riproduzione.
 * </p>
 */
public class ConfigurablePlaylistIterator implements PlaylistIterator {

	private final Playlist playlist;

	private int lastKnownIndex;
	private Song currentSong;
	private final Set<UUID> playedSongIds;

	private PlaylistIteratorStrategy strategy;

	public ConfigurablePlaylistIterator(Playlist playlist, PlaylistIteratorStrategy strategy) {
		if (playlist == null) {
			throw new IllegalArgumentException("Playlist cannot be null.");
		}

		if (strategy == null) {
			throw new IllegalArgumentException("Playlist iterator strategy cannot be null.");
		}

		this.playlist = playlist;
		this.strategy = strategy;
		this.lastKnownIndex = -1;
		this.currentSong = null;
		this.playedSongIds = new HashSet<>();
	}

	@Override
	public Song next() {
		List<Song> songs = playlist.getSongs();

		if (songs.isEmpty()) {
			return null;
		}

		int currentIndex = resolveCurrentIndex(songs);
		int nextIndex = strategy.nextIndex(currentIndex, songs, playedSongIds);

		if (nextIndex < 0 || nextIndex >= songs.size()) {
			return null;
		}

		currentSong = songs.get(nextIndex);
		lastKnownIndex = nextIndex;
		playedSongIds.add(currentSong.getId());

		return currentSong;
	}

	@Override
	public void setStrategy(PlaylistIteratorStrategy strategy) {
		if (strategy == null) {
			throw new IllegalArgumentException("Playlist iterator strategy cannot be null.");
		}

		this.strategy = strategy;
	}

	@Override
	public int getCurrentIndex() {
		return lastKnownIndex;
	}

	@Override
	public Song getCurrentSong() {
		return currentSong;
	}

	private int resolveCurrentIndex(List<Song> songs) {
		if (currentSong == null) {
			return -1;
		}

		for (int i = 0; i < songs.size(); i++) {
			if (songs.get(i).getId().equals(currentSong.getId())) {
				lastKnownIndex = i;
				return i;
			}
		}

		return Math.max(-1, lastKnownIndex - 1);
	}
}
