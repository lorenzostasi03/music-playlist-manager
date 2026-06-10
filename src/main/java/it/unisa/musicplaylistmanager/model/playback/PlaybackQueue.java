package it.unisa.musicplaylistmanager.model.playback;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Rappresenta la coda degli oggetti riproducibili in attesa.
 */
public class PlaybackQueue {

	private final Queue<Playable> queue;

	public PlaybackQueue() {
		this.queue = new ArrayDeque<>();
	}

	public void enqueue(Playable playable) {
		if (playable == null) {
			throw new IllegalArgumentException("Playable cannot be null.");
		}

		queue.add(playable);
	}

	public Playable dequeue() {
		return queue.poll();
	}

	public void clear() {
		queue.clear();
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public int size() {
		return queue.size();
	}

	public List<Playable> getSnapshot() {
		return new ArrayList<>(queue);
	}
}