package it.unisa.musicplaylistmanager.model.playback.player;

import static org.junit.jupiter.api.Assertions.*;

import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.playback.events.EventType;
import it.unisa.musicplaylistmanager.model.playback.mode.PlaybackMode;
import it.unisa.musicplaylistmanager.model.playback.playable.Playable;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test della classe {@link PlaybackQueue}.
 */
class PlaybackQueueTest {

	private PlaybackQueue queue;
	private Playable firstPlayable;
	private Playable secondPlayable;
	private Playable thirdPlayable;

	@BeforeEach
	void setUp() {
		queue = new PlaybackQueue();

		firstPlayable = new TestPlayable("Prima playlist");
		secondPlayable = new TestPlayable("Seconda playlist");
		thirdPlayable = new TestPlayable("Terza playlist");
	}

	@Test
	void nuovaCodaEVuota() {
		assertAll(() -> assertTrue(queue.isEmpty()), () -> assertEquals(0, queue.size()));
	}

	@Test
	void enqueueAggiungePlayableAllaCoda() {
		queue.enqueue(firstPlayable);

		assertAll(() -> assertFalse(queue.isEmpty()), () -> assertEquals(1, queue.size()),
				() -> assertEquals(firstPlayable, queue.getSnapshot().get(0)));
	}

	@Test
	void enqueueConPlayableNullLanciaEccezione() {
		assertThrows(IllegalArgumentException.class, () -> queue.enqueue(null));
	}

	@Test
	void dequeueRestituiscePlayableInOrdineFifo() {
		queue.enqueue(firstPlayable);
		queue.enqueue(secondPlayable);
		queue.enqueue(thirdPlayable);

		assertAll(() -> assertEquals(firstPlayable, queue.dequeue()),
				() -> assertEquals(secondPlayable, queue.dequeue()), () -> assertEquals(thirdPlayable, queue.dequeue()),
				() -> assertTrue(queue.isEmpty()));
	}

	@Test
	void dequeueSuCodaVuotaRestituisceNull() {
		assertNull(queue.dequeue());
	}

	@Test
	void removeEliminaIlPlayableSpecificato() {
		queue.enqueue(firstPlayable);
		queue.enqueue(secondPlayable);
		queue.enqueue(thirdPlayable);

		queue.remove(secondPlayable);

		assertEquals(List.of(firstPlayable, thirdPlayable), queue.getSnapshot());
	}

	@Test
	void removeEliminaTutteLeOccorrenzeUguali() {
		queue.enqueue(firstPlayable);
		queue.enqueue(secondPlayable);
		queue.enqueue(firstPlayable);

		queue.remove(firstPlayable);

		assertEquals(List.of(secondPlayable), queue.getSnapshot());
	}

	@Test
	void removeDiPlayableNonPresenteNonModificaLaCoda() {
		queue.enqueue(firstPlayable);
		queue.enqueue(secondPlayable);

		queue.remove(thirdPlayable);

		assertEquals(List.of(firstPlayable, secondPlayable), queue.getSnapshot());
	}

	@Test
	void removeSuCodaVuotaNonGeneraEccezione() {
		assertDoesNotThrow(() -> queue.remove(firstPlayable));

		assertTrue(queue.isEmpty());
	}

	@Test
	void removeLastEliminaUltimoPlayable() {
		queue.enqueue(firstPlayable);
		queue.enqueue(secondPlayable);
		queue.enqueue(thirdPlayable);

		queue.removeLast();

		assertEquals(List.of(firstPlayable, secondPlayable), queue.getSnapshot());
	}

	@Test
	void removeLastSuCodaVuotaNonGeneraEccezione() {
		assertDoesNotThrow(queue::removeLast);

		assertTrue(queue.isEmpty());
	}

	@Test
	void clearRimuoveTuttiIPlayable() {
		queue.enqueue(firstPlayable);
		queue.enqueue(secondPlayable);
		queue.enqueue(thirdPlayable);

		queue.clear();

		assertAll(() -> assertTrue(queue.isEmpty()), () -> assertEquals(0, queue.size()),
				() -> assertTrue(queue.getSnapshot().isEmpty()));
	}

	@Test
	void getSnapshotMantieneOrdineDellaCoda() {
		queue.enqueue(firstPlayable);
		queue.enqueue(secondPlayable);
		queue.enqueue(thirdPlayable);

		List<Playable> snapshot = queue.getSnapshot();

		assertEquals(List.of(firstPlayable, secondPlayable, thirdPlayable), snapshot);
	}

	@Test
	void getSnapshotRestituisceCopiaIndipendente() {
		queue.enqueue(firstPlayable);
		queue.enqueue(secondPlayable);

		List<Playable> snapshot = queue.getSnapshot();
		snapshot.clear();

		assertAll(() -> assertEquals(2, queue.size()),
				() -> assertEquals(List.of(firstPlayable, secondPlayable), queue.getSnapshot()));
	}

	@Test
	void toStringContieneIPlayableNellOrdineDellaCoda() {
		queue.enqueue(firstPlayable);
		queue.enqueue(secondPlayable);

		String result = queue.toString();

		assertEquals("Queue:\nPrima playlist\nSeconda playlist\n", result);
	}

	/**
	 * Implementazione minimale di {@link Playable} utilizzata esclusivamente nei
	 * test della coda.
	 */
	private static class TestPlayable extends Playable {

		private final String title;
		private PlaybackMode playbackMode;

		TestPlayable(String title) {
			this.title = title;
			this.playbackMode = PlaybackMode.SEQUENTIAL;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public void play() {
		}

		@Override
		public void pause() {
		}

		@Override
		public void resume() {
		}

		@Override
		public void stop() {
		}

		@Override
		public Song getCurrentSong() {
			return null;
		}

		@Override
		protected void updatePlayCount() {
		}

		@Override
		public void update(EventType eventType) {
		}

		@Override
		public boolean skipToNextSong() {
			return false;
		}

		@Override
		public void setPlaybackMode(PlaybackMode mode) {
			this.playbackMode = mode;
		}

		@Override
		public PlaybackMode getPlaybackMode() {
			return playbackMode;
		}

		@Override
		public String toString() {
			return title;
		}
	}
}
