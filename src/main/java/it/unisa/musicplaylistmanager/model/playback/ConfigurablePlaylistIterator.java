package it.unisa.musicplaylistmanager.model.playback;

import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;

/**
 * Iteratore della playlist configurabile con diverse strategie di navigazione.
 */
public class ConfigurablePlaylistIterator implements PlaylistIterator {

	private final Playlist playlist;
	private int currentIndex;
	private PlaylistIteratorStrategy strategy;

	/**
	 * Crea un iteratore sulla playlist indicata usando la strategia sequenziale.
	 *
	 * @param playlist
	 *            playlist da scorrere
	 */
	public ConfigurablePlaylistIterator(Playlist playlist) {
		this(playlist, new SequentialIteratorStrategy());
	}

	/**
	 * Crea un iteratore sulla playlist indicata usando la strategia specificata.
	 *
	 * @param playlist
	 *            playlist da scorrere
	 * @param strategy
	 *            strategia iniziale
	 */
	public ConfigurablePlaylistIterator(Playlist playlist, PlaylistIteratorStrategy strategy) {
		if (playlist == null) {
			throw new IllegalArgumentException("Playlist cannot be null.");
		}

		if (strategy == null) {
			throw new IllegalArgumentException("Strategy cannot be null.");
		}

		this.playlist = playlist;
		this.currentIndex = playlist.isEmpty() ? -1 : 0;
		this.strategy = strategy;
	}

	@Override
	public boolean hasNext() {
		return strategy.nextIndex(currentIndex, playlist.size()) != -1;
	}

	@Override
	public Song next() {
		int nextIndex = strategy.nextIndex(currentIndex, playlist.size());

		if (nextIndex == -1) {
			return null;
		}

		currentIndex = nextIndex;
		return getCurrentSong();
	}

	@Override
	public void setStrategy(PlaylistIteratorStrategy strategy) {
		if (strategy == null) {
			throw new IllegalArgumentException("Strategy cannot be null.");
		}

		this.strategy = strategy;
	}

	/**
	 * Restituisce la traccia corrente dell'iteratore.
	 *
	 * @return traccia corrente, oppure {@code null} se la playlist e' vuota
	 */
	public Song getCurrentSong() {
		if (playlist.isEmpty() || currentIndex < 0 || currentIndex >= playlist.size()) {
			return null;
		}

		return playlist.getSongAt(currentIndex);
	}

	/**
	 * Torna alla traccia precedente. Con il loop attivo, dalla prima torna
	 * all'ultima.
	 *
	 * @param loopPlaylist
	 *            true se il precedente deve rispettare il loop playlist
	 * @return traccia precedente
	 */
	public Song previous(boolean loopPlaylist) {
		if (playlist.isEmpty()) {
			return null;
		}

		if (currentIndex > 0) {
			currentIndex--;
		} else if (loopPlaylist) {
			currentIndex = playlist.size() - 1;
		}

		return getCurrentSong();
	}

}
