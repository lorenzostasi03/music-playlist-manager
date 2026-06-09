package it.unisa.musicplaylistmanager.model.playback;

/**
 * Strategia di riproduzione ciclica della playlist.
 */
public class LoopIteratorStrategy implements PlaylistIteratorStrategy {

	@Override
	public void reset(int playlistSize, int currentIndex) {
	}

	@Override
	public int nextIndex(int currentIndex, int playlistSize) {
		if (playlistSize == 0) {
			return -1;
		}

		return (currentIndex + 1) % playlistSize;
	}
}