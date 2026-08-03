package it.unisa.musicplaylistmanager.model.playback.playable;

import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.playback.events.EventListener;
import it.unisa.musicplaylistmanager.model.playback.events.EventManager;
import it.unisa.musicplaylistmanager.model.playback.mode.PlaybackMode;

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
	protected String id;
	private final EventManager events;

	/**
	 * Crea un nuovo playable inizializzando il relativo gestore di eventi.
	 */
	protected Playable() {
		this.events = new EventManager();
	}

	/**
	 * * Restituisce il titolo utilizzato per rappresentare il playable *
	 * nell'interfaccia utente. * * @return titolo del playable
	 */
	public abstract String getTitle();

	/**
	 * Restituisce il gestore degli eventi associato al playable.
	 *
	 * @return event manager del playable
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
	 * Restituisce la traccia attualmente riprodotta dal playable.
	 *
	 * @return traccia corrente, oppure {@code null} se non è disponibile
	 */
	public abstract Song getCurrentSong();

	/**
	 * Avanza al brano successivo all'interno del playable.
     * @return {@code true} se il playable ha gestito internamente l'avanzamento,
	 * {@code false} se non sono presenti altri brani e il player deve passare al
	 * playable successivo
	 */
	public abstract boolean skipToNextSong();

	/**
	 * Imposta la modalità di riproduzione del playable.
	 */
	public abstract void setPlaybackMode(PlaybackMode mode);

	/**
	 * Restituisce la modalità di riproduzione attualmente configurata.
	 *
	 * @return modalità di riproduzione corrente
	 */
	public abstract PlaybackMode getPlaybackMode();

	/**
	 * Aggiorna il numero di riproduzioni dell'entità associata al playable
	 */
	protected abstract void updatePlayCount();
}
