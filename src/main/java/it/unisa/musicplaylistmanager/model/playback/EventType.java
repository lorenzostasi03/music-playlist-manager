package it.unisa.musicplaylistmanager.model.playback;

/**
 * Definisce i tipi di evento generati durante la riproduzione.
 */
public enum EventType {

	/**
	 * Indica che il file audio attualmente riprodotto è terminato.
	 */
	AUDIO_COMPLETED,

	/**
	 * Indica che la canzone corrente dell'oggetto riproducibile è cambiata.
	 */
	CURRENT_SONG_CHANGED,

	/**
	 * Indica che l'oggetto riproducibile corrente del player è cambiato.
	 */
	CURRENT_PLAYABLE_CHANGED,

	/**
	 * 
	 * 
	 */
	QUEUE_CHANGED,

	/**
	 * Indica che l'oggetto riproducibile corrente ha completato la riproduzione.
	 */
	PLAYABLE_COMPLETED

}