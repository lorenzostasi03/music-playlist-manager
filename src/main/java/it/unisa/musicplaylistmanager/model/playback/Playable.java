package it.unisa.musicplaylistmanager.model.playback;

import it.unisa.musicplaylistmanager.model.entity.Song;

/**
 * Rappresenta un elemento riproducibile dal sistema.
 *
 * <p>
 * La classe definisce le operazioni comuni a tutti gli oggetti che possono
 * essere riprodotti, come una singola traccia o una playlist. Ogni oggetto
 * riproducibile possiede inoltre un gestore di eventi per notificare il
 * completamento della riproduzione.
 */
public abstract class Playable implements EventListener {

	private final EventManager events;

	/**
	 * Crea un nuovo oggetto riproducibile inizializzando il relativo gestore di
	 * eventi.
	 */
	protected Playable() {
		this.events = new EventManager();
	}

	/**
	 * Restituisce il gestore degli eventi associato all'oggetto riproducibile.
	 *
	 * @return event manager dell'oggetto riproducibile
	 */
	public EventManager getEvents() {
		return events;
	}

	/**
	 * Avvia la riproduzione dell'oggetto.
	 */
	public abstract void play();

	/**
	 * Mette in pausa la riproduzione dell'oggetto.
	 */
	public abstract void pause();

	/**
	 * Riprende la riproduzione dell'oggetto.
	 */
	public abstract void resume();

	/**
	 * Interrompe la riproduzione dell'oggetto.
	 */
	public abstract void stop();

	/**
	 * Restituisce la traccia attualmente associata alla riproduzione.
	 *
	 * @return traccia corrente
	 */
	public abstract Song getCurrentSong();

	/**
	 * Passa manualmente alla traccia successiva, se l'oggetto riproducibile lo
	 * supporta.
	 */
	public void skipNext() {
	}

	/**
	 * Passa manualmente alla traccia precedente, se l'oggetto riproducibile lo
	 * supporta.
	 */
	public void skipPrevious() {
	}

	/**
	 * Imposta la modalita' di riproduzione, se supportata dall'oggetto corrente.
	 *
	 * @param playbackMode
	 *            modalita' da applicare
	 */
	public void setPlaybackMode(PlaybackMode playbackMode) {
	}

	/**
	 * Restituisce la modalita' di riproduzione corrente.
	 *
	 * @return modalita' corrente
	 */
	public PlaybackMode getPlaybackMode() {
		return PlaybackMode.SEQUENTIAL;
	}
}
