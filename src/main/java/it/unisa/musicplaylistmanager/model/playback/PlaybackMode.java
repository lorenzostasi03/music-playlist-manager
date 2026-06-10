package it.unisa.musicplaylistmanager.model.playback;

/**
 * Modalita' disponibili per la riproduzione.
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
	LOOP_PLAYLIST
}
