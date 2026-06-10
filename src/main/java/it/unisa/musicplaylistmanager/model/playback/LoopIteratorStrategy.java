package it.unisa.musicplaylistmanager.model.playback;

/**
 * Strategia loop playlist: dopo l'ultima traccia torna alla prima.
 */
public class LoopIteratorStrategy implements PlaylistIteratorStrategy {

	@Override
	public int nextIndex(int currentIndex, int playlistSize) {
		if (playlistSize <= 0) {
			return -1;
		}

		return (currentIndex + 1) % playlistSize;
	}
}
