package it.unisa.musicplaylistmanager.model.playback;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class EventManagerTest {

	@Test
	void subscribeAndNotifyListenersShouldCallListener() {
		EventManager eventManager = new EventManager();
		AtomicBoolean notified = new AtomicBoolean(false);

		eventManager.subscribe(EventType.AUDIO_COMPLETED, eventType -> notified.set(true));
		eventManager.notifyListeners(EventType.AUDIO_COMPLETED);

		assertTrue(notified.get());
	}

	@Test
	void unsubscribeShouldRemoveListener() {
		EventManager eventManager = new EventManager();
		AtomicBoolean notified = new AtomicBoolean(false);

		EventListener listener = eventType -> notified.set(true);

		eventManager.subscribe(EventType.AUDIO_COMPLETED, listener);
		eventManager.unsubscribe(EventType.AUDIO_COMPLETED, listener);
		eventManager.notifyListeners(EventType.AUDIO_COMPLETED);

		assertFalse(notified.get());
	}

	@Test
	void subscribeShouldRejectNullEventType() {
		EventManager eventManager = new EventManager();

		assertThrows(IllegalArgumentException.class, () -> eventManager.subscribe(null, eventType -> {
		}));
	}

	@Test
	void subscribeShouldRejectNullListener() {
		EventManager eventManager = new EventManager();

		assertThrows(IllegalArgumentException.class, () -> eventManager.subscribe(EventType.AUDIO_COMPLETED, null));
	}

	@Test
	void notifyListenersShouldRejectNullEventType() {
		EventManager eventManager = new EventManager();

		assertThrows(IllegalArgumentException.class, () -> eventManager.notifyListeners(null));
	}
}
