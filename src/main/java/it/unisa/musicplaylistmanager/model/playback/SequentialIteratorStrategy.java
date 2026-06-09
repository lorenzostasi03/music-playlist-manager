package it.unisa.musicplaylistmanager.model.playback;

/**
 * Strategia di riproduzione sequenziale.
 */
public class SequentialIteratorStrategy implements PlaylistIteratorStrategy {

	@Override
	public void reset(int playlistSize, int currentIndex) {
	}

	@Override
	public int nextIndex(int currentIndex, int playlistSize) {
		int nextIndex = currentIndex + 1;

		if (nextIndex >= playlistSize) {
			return -1;
		}

		return nextIndex;
	}
}