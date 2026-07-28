package it.unisa.musicplaylistmanager.model.playback.player;

import it.unisa.musicplaylistmanager.model.playback.playable.Playable;
import java.util.*;

/**
 * Rappresenta la coda dei playable in attesa di essere riprodotti.
 */
public class PlaybackQueue {

	private final Deque<Playable> queue;

	/**
	 * Crea una nuova coda di riproduzione vuota.
	 */
	public PlaybackQueue() {
		this.queue = new ArrayDeque<>();
	}

	/**
	 * Aggiunge un playable in fondo alla coda.
	 *
	 * @param playable
	 *            elemento da aggiungere
	 * @throws IllegalArgumentException
	 *             se il playable è {@code null}
	 */
	public void enqueue(Playable playable) {
		if (playable == null) {
			throw new IllegalArgumentException("Playable cannot be null.");
		}

		queue.add(playable);
	}

	/**
	 * Rimuove e restituisce il primo playable della coda.
	 *
	 * @return primo elemento della coda, oppure {@code null} se la coda è vuota
	 */
	public Playable dequeue() {
		return queue.poll();
	}

	/**
	 * Rimuove dalla coda tutte le occorrenze uguali al playable specificato.
	 *
	 * @param playable
	 *            elemento da rimuovere
	 */
	public void remove(Playable playable) {
		if (queue.isEmpty())
			return;

		queue.removeIf(p -> p.equals(playable));
	}

	/**
	 * Rimuove l'ultimo playable presente nella coda.
	 */
	public void removeLast() {
		if (queue.isEmpty())
			return;

		queue.removeLast();
	}

	/**
	 * Rimuove tutti gli elementi dalla coda.
	 */
	public void clear() {
		queue.clear();
	}

	/**
	 * Verifica se la coda è vuota.
	 *
	 * @return {@code true} se la coda non contiene elementi
	 */
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	/**
	 * Restituisce il numero di elementi presenti nella coda.
	 *
	 * @return dimensione della coda
	 */
	public int size() {
		return queue.size();
	}

	/**
	 * Restituisce una copia degli elementi presenti nella coda, mantenendone
	 * l'ordine.
	 *
	 * @return copia della coda corrente
	 */
	public List<Playable> getSnapshot() {
		return new ArrayList<>(queue);
	}

	/**
	 * Restituisce la rappresentazione testuale della coda.
	 *
	 * @return contenuto della coda
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Queue:\n");

		for (Playable playable : queue) {
			sb.append(playable).append("\n");
		}

		return sb.toString();
	}
}
