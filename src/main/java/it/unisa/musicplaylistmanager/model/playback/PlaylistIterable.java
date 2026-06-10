package it.unisa.musicplaylistmanager.model.playback;

/**
 * Indica che un oggetto puo' creare un iteratore per scorrere le sue tracce.
 */
public interface PlaylistIterable {

	/**
	 * Crea un iteratore per la playlist.
	 *
	 * @return iteratore della playlist
	 */
	PlaylistIterator createIterator();
}
