package it.unisa.musicplaylistmanager.model.playback.events;

/**
 * Definisce i tipi di evento generati durante la riproduzione.
 */
public enum EventType {

	/**
	 * Indica che il file audio attualmente riprodotto è terminato.
	 */
	AUDIO_COMPLETED,

	/**
	 * Indica che la canzone corrente del playable è cambiata.
	 */
	CURRENT_SONG_CHANGED,

	/**
	 * Indica che il playable corrente del player è cambiato.
	 */
	CURRENT_PLAYABLE_CHANGED,

	/**
	 *
	 *
	 */
	QUEUE_CHANGED,

	/**
	 * Indica che il playable corrente ha completato la riproduzione.
	 */
	PLAYABLE_COMPLETED

}
