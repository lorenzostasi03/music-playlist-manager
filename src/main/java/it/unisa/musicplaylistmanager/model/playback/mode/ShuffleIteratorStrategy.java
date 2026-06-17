package it.unisa.musicplaylistmanager.model.playback.mode;

import it.unisa.musicplaylistmanager.model.entity.Song;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Strategia di riproduzione casuale senza ripetizioni.
 */
public class ShuffleIteratorStrategy implements PlaylistIteratorStrategy {

	private final Random random = new Random();

	@Override
	public int nextIndex(int currentIndex, List<Song> songs, Set<UUID> playedSongIds) {
		List<Integer> availableIndexes = new ArrayList<>();

		// Costruisce l'elenco degli indici dei brani non ancora riprodotti.
		for (int i = 0; i < songs.size(); i++) {
			Song song = songs.get(i);

			if (!playedSongIds.contains(song.getId())) {
				availableIndexes.add(i);
			}
		}

		if (availableIndexes.isEmpty()) {
			return -1;
		}

		// Seleziona casualmente uno degli indici ancora disponibili.
		return availableIndexes.get(random.nextInt(availableIndexes.size()));
	}
}
