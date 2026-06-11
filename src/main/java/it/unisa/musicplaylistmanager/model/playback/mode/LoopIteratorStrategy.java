package it.unisa.musicplaylistmanager.model.playback.mode;

import it.unisa.musicplaylistmanager.model.entity.Song;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Strategia di riproduzione ciclica della playlist.
 */
public class LoopIteratorStrategy implements PlaylistIteratorStrategy {

	@Override
	public int nextIndex(int currentIndex, List<Song> songs, Set<UUID> playedSongIds) {
		if (songs.isEmpty()) {
			return -1;
		}

		return (currentIndex + 1) % songs.size();
	}
}
