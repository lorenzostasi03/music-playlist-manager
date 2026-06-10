package it.unisa.musicplaylistmanager.model.playback;

import it.unisa.musicplaylistmanager.model.entity.Song;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Strategia usata per calcolare il prossimo indice durante
 * la riproduzione di una playlist.
 */
public interface PlaylistIteratorStrategy {

	int nextIndex(int currentIndex, List<Song> songs, Set<UUID> playedSongIds);
}