package it.unisa.musicplaylistmanager.model.playback;

/**
 * Strategia usata per calcolare il prossimo indice durante
 * la riproduzione di una playlist.
 */
public interface PlaylistIteratorStrategy {

	void reset(int playlistSize, int currentIndex);

	int nextIndex(int currentIndex, int playlistSize);
}