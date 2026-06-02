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
	 * Indica che l'oggetto riproducibile corrente ha completato la riproduzione.
	 */
	PLAYABLE_COMPLETED
}
