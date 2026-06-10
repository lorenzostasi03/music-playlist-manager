package it.unisa.musicplaylistmanager.model.playback;

import it.unisa.musicplaylistmanager.model.entity.Song;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Strategia di riproduzione sequenziale.
 */
public class SequentialIteratorStrategy implements PlaylistIteratorStrategy {

	@Override
	public int nextIndex(int currentIndex, List<Song> songs, Set<UUID> playedSongIds) {
		int nextIndex = currentIndex + 1;

		if (nextIndex >= songs.size()) {
			return -1;
		}

		return nextIndex;
	}
}