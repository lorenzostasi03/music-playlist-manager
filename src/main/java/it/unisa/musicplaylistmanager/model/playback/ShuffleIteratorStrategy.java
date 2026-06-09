package it.unisa.musicplaylistmanager.model.playback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Strategia di riproduzione casuale senza ripetizioni.
 */
public class ShuffleIteratorStrategy implements PlaylistIteratorStrategy {

	private final List<Integer> remainingIndexes = new ArrayList<>();

	@Override
	public void reset(int playlistSize, int currentIndex) {
		remainingIndexes.clear();

		for (int i = 0; i < playlistSize; i++) {
			if (i != currentIndex) {
				remainingIndexes.add(i);
			}
		}

		Collections.shuffle(remainingIndexes);
	}

	@Override
	public int nextIndex(int currentIndex, int playlistSize) {
		if (remainingIndexes.isEmpty()) {
			return -1;
		}

		return remainingIndexes.removeFirst();
	}
}