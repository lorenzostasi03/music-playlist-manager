package it.unisa.musicplaylistmanager.model.playback;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Gestisce la registrazione e la notifica dei listener associati agli eventi di
 * riproduzione.
 */
public class EventManager {

	private final Map<EventType, List<EventListener>> listeners;

	/**
	 * Crea un nuovo gestore di eventi inizializzando la lista dei listener per
	 * ciascun tipo di evento disponibile.
	 */
	public EventManager() {
		this.listeners = new EnumMap<>(EventType.class);

		for (EventType eventType : EventType.values()) {
			listeners.put(eventType, new ArrayList<>());
		}
	}

	/**
	 * Registra un listener per il tipo di evento specificato.
	 *
	 * @param eventType
	 *            tipo di evento da osservare
	 * @param listener
	 *            listener da registrare
	 * @throws IllegalArgumentException
	 *             se il tipo di evento o il listener sono {@code null}
	 */
	public void subscribe(EventType eventType, EventListener listener) {
		if (eventType == null) {
			throw new IllegalArgumentException("Event type cannot be null.");
		}

		if (listener == null) {
			throw new IllegalArgumentException("Listener cannot be null.");
		}

		listeners.get(eventType).add(listener);
	}

	/**
	 * Rimuove un listener dal tipo di evento specificato.
	 *
	 * @param eventType
	 *            tipo di evento osservato
	 * @param listener
	 *            listener da rimuovere
	 * @throws IllegalArgumentException
	 *             se il tipo di evento o il listener sono {@code null}
	 */
	public void unsubscribe(EventType eventType, EventListener listener) {
		if (eventType == null) {
			throw new IllegalArgumentException("Event type cannot be null.");
		}

		if (listener == null) {
			throw new IllegalArgumentException("Listener cannot be null.");
		}

		listeners.get(eventType).remove(listener);
	}

	/**
	 * Notifica a tutti i listener registrati che si è verificato il tipo di evento
	 * specificato.
	 *
	 * @param eventType
	 *            tipo di evento da notificare
	 * @throws IllegalArgumentException
	 *             se il tipo di evento è {@code null}
	 */
	public void notifyListeners(EventType eventType) {
		if (eventType == null) {
			throw new IllegalArgumentException("Event type cannot be null.");
		}

		List<EventListener> eventListeners = new ArrayList<>(listeners.get(eventType));

		for (EventListener listener : eventListeners) {
			listener.update(eventType);
		}
	}
}
