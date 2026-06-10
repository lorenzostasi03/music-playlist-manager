package it.unisa.musicplaylistmanager.model.playback;

/**
 * Definisce le modalita' di riproduzione applicabili a un oggetto riproducibile.
 */
public enum PlaybackMode {
	/**
	 * Riproduce le tracce in ordine e si ferma alla fine.
	 */
	SEQUENTIAL,

	/**
	 * Ripete indefinitamente la traccia corrente.
	 */
	LOOP_TRACK,

	/**
	 * Riparte dalla prima traccia dopo la fine della playlist.
	 */
	LOOP_PLAYLIST,

	/**
	 * Riproduce i brani della playlist in ordine casuale.
	 */
	SHUFFLE
}
