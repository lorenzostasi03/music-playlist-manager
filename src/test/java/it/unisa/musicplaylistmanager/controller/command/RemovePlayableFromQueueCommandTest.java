package it.unisa.musicplaylistmanager.controller.command;

import static org.junit.jupiter.api.Assertions.*;

import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.playback.events.EventType;
import it.unisa.musicplaylistmanager.model.playback.mode.PlaybackMode;
import it.unisa.musicplaylistmanager.model.playback.playable.Playable;
import it.unisa.musicplaylistmanager.model.playback.player.Player;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test della classe {@link RemovePlayableFromQueueCommand}.
 */
class RemovePlayableFromQueueCommandTest {

	private Player player;
	private Playable firstPlayable;
	private Playable playableToRemove;
	private Playable lastPlayable;
	private RemovePlayableFromQueueCommand command;

	@BeforeEach
	void setUp() {
		player = new Player();

		firstPlayable = new TestPlayable("Primo");
		playableToRemove = new TestPlayable("Da rimuovere");
		lastPlayable = new TestPlayable("Ultimo");

		command = new RemovePlayableFromQueueCommand(
				player,
				playableToRemove);
	}

	@Test
	void costruttoreConPlayerNullLanciaEccezione() {
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> new RemovePlayableFromQueueCommand(
						null,
						playableToRemove));

		assertEquals(
				"Player non può essere null!",
				exception.getMessage());
	}

	@Test
	void executeRimuovePlayableDallaCoda() {
		player.enqueue(firstPlayable);
		player.enqueue(playableToRemove);
		player.enqueue(lastPlayable);

		command.execute();

		assertEquals(
				List.of(firstPlayable, lastPlayable),
				player.getQueueSnapshot());
	}

	@Test
	void executeNonRimuoveGliAltriPlayable() {
		player.enqueue(firstPlayable);
		player.enqueue(playableToRemove);
		player.enqueue(lastPlayable);

		command.execute();

		assertAll(
				() -> assertTrue(
						player.getQueueSnapshot().contains(firstPlayable)),
				() -> assertTrue(
						player.getQueueSnapshot().contains(lastPlayable)),
				() -> assertFalse(
						player.getQueueSnapshot().contains(playableToRemove)));
	}

	@Test
	void executeRimuoveTutteLeOccorrenzeDelPlayable() {
		player.enqueue(playableToRemove);
		player.enqueue(firstPlayable);
		player.enqueue(playableToRemove);

		command.execute();

		assertEquals(
				List.of(firstPlayable),
				player.getQueueSnapshot());
	}

	@Test
	void executeConPlayableNonPresenteNonModificaLaCoda() {
		player.enqueue(firstPlayable);
		player.enqueue(lastPlayable);

		assertDoesNotThrow(command::execute);

		assertEquals(
				List.of(firstPlayable, lastPlayable),
				player.getQueueSnapshot());
	}

	@Test
	void undoReinseriscePlayableInFondoAllaCoda() {
		player.enqueue(firstPlayable);
		player.enqueue(playableToRemove);
		player.enqueue(lastPlayable);

		command.execute();
		command.undo();

		assertEquals(
				List.of(firstPlayable, lastPlayable, playableToRemove),
				player.getQueueSnapshot());
	}

	@Test
	void executeSeguitoDaUndoRipristinaLaPresenzaDelPlayable() {
		player.enqueue(playableToRemove);

		command.execute();
		assertFalse(player.getQueueSnapshot().contains(playableToRemove));

		command.undo();
		assertTrue(player.getQueueSnapshot().contains(playableToRemove));
	}

	/**
	 * Implementazione minimale di {@link Playable} utilizzata esclusivamente
	 * nei test del comando.
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