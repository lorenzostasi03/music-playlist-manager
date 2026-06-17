package it.unisa.musicplaylistmanager.model.playback.player;

import it.unisa.musicplaylistmanager.model.playback.playable.Playable;

import java.util.*;

/**
 * Rappresenta la coda dei playable in attesa di essere riprodotti.
 */
public class PlaybackQueue {

	private final Deque<Playable> queue;

	public PlaybackQueue() {
		this.queue = new ArrayDeque<>();
	}

	public void enqueue(Playable playable) {
		if (playable == null) {
			throw new IllegalArgumentException("Playable cannot be null.");
		}

		queue.add(playable);
	}

	public Playable dequeue() { return queue.poll(); }

    public void remove(Playable playable){
        if (queue.isEmpty()) return;

        queue.removeIf(p -> p.equals(playable));
    }

    public void removeLast() {
        if (queue.isEmpty()) return;

       queue.removeLast();
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Queue:\n");
        for (Playable playable : queue) {
            sb.append(playable).append("\n");
        }

        return sb.toString();
    }
}
