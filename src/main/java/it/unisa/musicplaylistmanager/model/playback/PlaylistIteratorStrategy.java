package it.unisa.musicplaylistmanager.model.playback;

/**
 * Strategia per calcolare il prossimo indice da riprodurre in una playlist.
 */
public interface PlaylistIteratorStrategy {

	/**
	 * Calcola il prossimo indice a partire dall'indice corrente.
	 *
	 * @param currentIndex
	 *            indice corrente
	 * @param playlistSize
	 *            numero di tracce nella playlist
	 * @return prossimo indice, oppure -1 se non esiste una prossima traccia
	 */
	int nextIndex(int currentIndex, int playlistSize);
}
