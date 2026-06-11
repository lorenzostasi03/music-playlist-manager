package it.unisa.musicplaylistmanager.model.playback.player;

/**
 * Definisce i possibili stati del player.
 */
public enum PlayerState {

	/**
	 * Il player non ha alcuna riproduzione attiva.
	 */
	STOPPED,

	/**
	 * Il player sta riproducendo un oggetto.
	 */
	PLAYING,

	/**
	 * La riproduzione corrente è in pausa.
	 */
	PAUSED
}
