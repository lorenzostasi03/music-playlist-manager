package it.unisa.musicplaylistmanager.model.playback;

import it.unisa.musicplaylistmanager.model.entity.Song;

/**
 * Iteratore usato per navigare tra le tracce di una playlist.
 */
public interface PlaylistIterator {

	/**
	 * Indica se esiste una prossima traccia secondo la strategia corrente.
	 *
	 * @return true se e' disponibile una prossima traccia
	 */
	boolean hasNext();

	/**
	 * Restituisce la prossima traccia secondo la strategia corrente.
	 *
	 * @return prossima traccia
	 */
	Song next();

	/**
	 * Cambia la strategia usata per calcolare la prossima traccia.
	 *
	 * @param strategy
	 *            nuova strategia dell'iteratore
	 */
	void setStrategy(PlaylistIteratorStrategy strategy);
}
