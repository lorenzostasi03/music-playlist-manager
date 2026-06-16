package it.unisa.musicplaylistmanager.model.playback.player;

import it.unisa.musicplaylistmanager.model.playback.events.EventListener;
import it.unisa.musicplaylistmanager.model.playback.events.EventManager;
import it.unisa.musicplaylistmanager.model.playback.events.EventType;
import it.unisa.musicplaylistmanager.model.playback.playable.Playable;

import java.util.List;

/**
 * Gestisce la riproduzione corrente dell'applicazione.
 *
 * <p>
 * Il player mantiene il riferimento all'oggetto riproducibile attualmente in
 * esecuzione e coordina le operazioni di avvio, pausa, ripresa, interruzione e
 * avanzamento della coda di riproduzione.
 */
public class Player implements EventListener {

	private Playable currentPlayable;
	private PlayerState state;

	private final PlaybackQueue queue;
	private final EventManager events;

	/**
	 * Crea un nuovo player senza alcun oggetto in riproduzione.
	 */
	public Player() {
		this.currentPlayable = null;
		this.state = PlayerState.STOPPED;
		this.queue = new PlaybackQueue();
		this.events = new EventManager();
	}

	/**
	 * Avvia immediatamente la riproduzione dell'oggetto specificato.
	 *
	 * <p>
	 * Se un altro oggetto è già in riproduzione, questo viene interrotto prima di
	 * avviare il nuovo oggetto. La coda di riproduzione non viene svuotata.
	 *
	 * @param playable
	 *            oggetto da riprodurre
	 * @throws IllegalArgumentException
	 *             se l'oggetto riproducibile è {@code null}
	 */
	public void play(Playable playable) {
		if (playable == null) {
			throw new IllegalArgumentException("Playable cannot be null.");
		}

		stopCurrentPlayable();
		startPlayable(playable);
	}

	/**
	 * Aggiunge un oggetto riproducibile alla coda.
	 *
	 * @param playable
	 *            oggetto da aggiungere alla coda
	 * @throws IllegalArgumentException
	 *             se l'oggetto riproducibile è {@code null}
	 */
	public void enqueue(Playable playable) {
		queue.enqueue(playable);
		notifyQueueChanged();
	}

    /**
     * Rimuove l'ultimo Playable aggiunto alla coda.
     * @return l'ultimo Playable aggiunto alla coda, {@code null} se la coda è vuota
     */
    public void removeLast() {
        queue.removeLast();
        notifyQueueChanged();
    }

    /**
     * Rimuove tutte le occorrenze del Playable specificato dalla coda di riproduzione.
     *
     * @param playable
     *              Playable da rimuovere dalla coda
     * @throws IllegalArgumentException
     *               se il Playable è {@code null}
     */
    public void removeFromQueue(Playable playable) {
        if (playable == null) throw new IllegalArgumentException("Playable non può essere null.");

        queue.remove(playable);
        notifyQueueChanged();
    }

	/**
	 * Svuota la coda di riproduzione.
	 */
	public void clearQueue() {
		queue.clear();
		notifyQueueChanged();
	}

	/**
	 * Restituisce una copia della coda corrente.
	 *
	 * @return lista degli elementi in coda
	 */
	public List<Playable> getQueueSnapshot() {
		return queue.getSnapshot();
	}

	/**
	 * Mette in pausa la riproduzione corrente, se il player è in stato
	 * {@link PlayerState#PLAYING}.
	 */
	public void pause() {
		if (currentPlayable != null && state == PlayerState.PLAYING) {
			currentPlayable.pause();
			state = PlayerState.PAUSED;
		}
	}

	/**
	 * Riprende la riproduzione corrente, se il player è in stato
	 * {@link PlayerState#PAUSED}.
	 */
	public void resume() {
		if (currentPlayable != null && state == PlayerState.PAUSED) {
			currentPlayable.resume();
			state = PlayerState.PLAYING;
		}
	}

	/**
	 * Interrompe la riproduzione corrente. La coda non viene svuotata.
	 */
	public void stop() {
		stopCurrentPlayable();
		state = PlayerState.STOPPED;
		notifyCurrentPlayableChanged();
	}

	public void skipSong() {
		if (currentPlayable == null) {
			playNextFromQueue();
			return;
		}

		boolean skippedInsidePlayable = currentPlayable.skipToNextSong();

		if (!skippedInsidePlayable) {
			stopCurrentPlayable();
			playNextFromQueue();
		}
	}

	public void skipPlayable() {
		stopCurrentPlayable();
		playNextFromQueue();
	}

	/**
	 * Restituisce lo stato corrente del player.
	 *
	 * @return stato corrente del player
	 */
	public PlayerState getState() {
		return state;
	}

	/**
	 * Restituisce l'oggetto attualmente gestito dal player.
	 *
	 * @return oggetto riproducibile corrente, oppure {@code null} se non è presente
	 *         alcuna riproduzione
	 */
	public Playable getCurrentPlayable() {
		return currentPlayable;
	}

	/**
	 * Restituisce il gestore eventi del player.
	 *
	 * @return event manager del player
	 */
	public EventManager getEvents() {
		return events;
	}

	/**
	 * Gestisce gli eventi ricevuti dall'oggetto riproducibile corrente.
	 *
	 * @param eventType
	 *            tipo di evento ricevuto
	 */
	@Override
	public void update(EventType eventType) {
		if (eventType == EventType.PLAYABLE_COMPLETED) {
			finishCurrentPlayable();
			playNextFromQueue();
		}
	}

	private void startPlayable(Playable playable) {
		currentPlayable = playable;
		currentPlayable.getEvents().subscribe(EventType.PLAYABLE_COMPLETED, this);
		currentPlayable.play();

		state = PlayerState.PLAYING;
		notifyCurrentPlayableChanged();
	}

	private void playNextFromQueue() {
		Playable nextPlayable = queue.dequeue();
		notifyQueueChanged();

		if (nextPlayable == null) {
			currentPlayable = null;
			state = PlayerState.STOPPED;
			notifyCurrentPlayableChanged();
			return;
		}

		startPlayable(nextPlayable);
	}

	private void stopCurrentPlayable() {
		if (currentPlayable != null) {
			currentPlayable.getEvents().unsubscribe(EventType.PLAYABLE_COMPLETED, this);
			currentPlayable.stop();
			currentPlayable = null;
		}
	}

	private void finishCurrentPlayable() {
		if (currentPlayable != null) {
			currentPlayable.getEvents().unsubscribe(EventType.PLAYABLE_COMPLETED, this);
			currentPlayable = null;
		}
	}

	private void notifyCurrentPlayableChanged() {
		events.notifyListeners(EventType.CURRENT_PLAYABLE_CHANGED);
	}

	private void notifyQueueChanged() {
		events.notifyListeners(EventType.QUEUE_CHANGED);
	}
}
