package it.unisa.musicplaylistmanager.model.playback;

/**
 * Strategia sequenziale: avanza di una traccia e termina dopo l'ultima.
 */
public class SequentialIteratorStrategy implements PlaylistIteratorStrategy {

	@Override
	public int nextIndex(int currentIndex, int playlistSize) {
		int nextIndex = currentIndex + 1;

		if (nextIndex >= playlistSize) {
			return -1;
		}

		return nextIndex;
	}
}
