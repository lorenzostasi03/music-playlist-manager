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
 * Il player mantiene il riferimento al playable attualmente in
 * esecuzione e coordina le operazioni di avvio, pausa, ripresa, interruzione e
 * avanzamento della coda di riproduzione.
 * </p>
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
	 * </p>
	 *
	 * @param playable
	 *            oggetto da riprodurre
	 * @throws IllegalArgumentException
	 *             se il playable è {@code null}
	 */
	public void play(Playable playable) {
		if (playable == null) {
			throw new IllegalArgumentException("Playable cannot be null.");
		}

		stopCurrentPlayable();
		startPlayable(playable);
	}

	/**
	 * Aggiunge un playable alla coda.
	 *
	 * @param playable
	 *            oggetto da aggiungere alla coda
	 * @throws IllegalArgumentException
	 *             se il playable è {@code null}
	 */
	public void enqueue(Playable playable) {
		queue.enqueue(playable);
		notifyQueueChanged();
	}

	/**
	 * Rimuove l'ultimo playable aggiunto alla coda.
	 */
	public void removeLast() {
		queue.removeLast();
		notifyQueueChanged();
	}

	/**
	 * Rimuove tutte le occorrenze del playable specificato dalla coda.
	 *
	 * @param playable
	 *            playable da rimuovere
	 * @throws IllegalArgumentException
	 *             se il playable è {@code null}
	 */
	public void removeFromQueue(Playable playable) {
		if (playable == null) {
			throw new IllegalArgumentException("Playable non può essere null.");
		}

		queue.remove(playable);
		notifyQueueChanged();
	}

	/**
	 * Svuota la coda di riproduzione.
	 */
	public void clearQueue() {
		if (queue.isEmpty()) {
			return;
		}

		queue.clear();
		notifyQueueChanged();
	}

	/**
	 * Restituisce una copia della coda corrente.
	 *
	 * @return lista degli elementi presenti in coda
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

	/**
	 * Avanza al brano successivo del playable corrente.
	 *
	 * <p>
	 * Se il playable non può gestire internamente l'avanzamento, viene interrotto e
	 * viene avviato il primo elemento presente nella coda.
	 * </p>
	 */
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

	/**
	 * Interrompe il playable corrente e avvia il successivo presente nella coda.
	 */
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
	 * @return playable corrente, oppure {@code null} se non è presente alcuna
	 *         riproduzione
	 */
	public Playable getCurrentPlayable() {
		return currentPlayable;
	}

	/**
	 * Restituisce il gestore degli eventi del player.
	 *
	 * @return gestore degli eventi
	 */
	public EventManager getEvents() {
		return events;
	}

	/**
	 * Gestisce gli eventi ricevuti dal playable corrente.
	 *
	 * <p>
	 * Quando il playable termina, il player passa automaticamente al primo elemento
	 * presente nella coda.
	 * </p>
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

	/**
	 * Imposta e avvia il playable specificato, registrando il player al relativo
	 * evento di completamento.
	 *
	 * @param playable
	 *            playable da avviare
	 */
	private void startPlayable(Playable playable) {
		currentPlayable = playable;
		currentPlayable.getEvents().subscribe(EventType.PLAYABLE_COMPLETED, this);
		currentPlayable.play();

		state = PlayerState.PLAYING;
		notifyCurrentPlayableChanged();
	}

	/**
	 * Estrae e avvia il primo playable della coda. Se la coda è vuota, porta il
	 * player nello stato {@link PlayerState#STOPPED}.
	 */
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

	/**
	 * Interrompe il playable corrente, rimuove la sottoscrizione ai suoi eventi e
	 * ne elimina il riferimento.
	 */
	private void stopCurrentPlayable() {
		if (currentPlayable != null) {
			currentPlayable.getEvents().unsubscribe(EventType.PLAYABLE_COMPLETED, this);
			currentPlayable.stop();
			currentPlayable = null;
		}
	}

	/**
	 * Rimuove il riferimento al playable terminato.
	 */
	private void finishCurrentPlayable() {
		if (currentPlayable != null) {
			currentPlayable.getEvents().unsubscribe(EventType.PLAYABLE_COMPLETED, this);
			currentPlayable = null;
		}
	}

	/**
	 * Notifica ai listener il cambiamento del playable corrente.
	 */
	private void notifyCurrentPlayableChanged() {
		events.notifyListeners(EventType.CURRENT_PLAYABLE_CHANGED);
	}

	/**
	 * Notifica ai listener una modifica della coda di riproduzione.
	 */
	private void notifyQueueChanged() {
		events.notifyListeners(EventType.QUEUE_CHANGED);
	}
}
