package it.unisa.musicplaylistmanager.model.playback;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Song;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class SongPlayableTest {

	/**
	 * Controlla che non si possa creare un SongPlayable senza una traccia.
	 */
	@Test
	void constructorShouldRejectNullSong() {
		assertThrows(IllegalArgumentException.class, () -> new SongPlayable(null));
	}

	/**
	 * Controlla che SongPlayable restituisca la stessa traccia passata al
	 * costruttore.
	 */
	@Test
	void getCurrentSongShouldReturnWrappedSong() {
		Song song = createTestSong();
		SongPlayable playable = new SongPlayable(song);

		assertSame(song, playable.getCurrentSong());
	}

	/**
	 * Verifica il comportamento normale: se l'audio finisce, il playable notifica
	 * il completamento.
	 */
	@Test
	void audioCompletedShouldNotifyPlayableCompleted() {
		Song song = createTestSong();
		SongPlayable playable = new SongPlayable(song);

		AtomicBoolean completed = new AtomicBoolean(false);

		playable.getEvents().subscribe(EventType.PLAYABLE_COMPLETED, eventType -> completed.set(true));

		playable.update(EventType.AUDIO_COMPLETED);

		assertTrue(completed.get());
	}

	/**
	 * Verifica che la modalita' iniziale sia sequenziale, cioe' quella di default
	 * richiesta dalla user story.
	 */
	@Test
	void defaultModeShouldBeSequential() {
		SongPlayable playable = new SongPlayable(createTestSong());

		assertEquals(PlaybackMode.SEQUENTIAL, playable.getPlaybackMode());
	}

	/**
	 * Verifica il loop traccia: quando arriva l'evento di fine audio, la traccia
	 * non deve essere segnata come completata.
	 */
	@Test
	void loopTrackShouldNotNotifyPlayableCompleted() {
		SongPlayable playable = new SongPlayable(createTestSong());
		AtomicBoolean completed = new AtomicBoolean(false);

		playable.setPlaybackMode(PlaybackMode.LOOP_TRACK);
		playable.getEvents().subscribe(EventType.PLAYABLE_COMPLETED, eventType -> completed.set(true));

		playable.update(EventType.AUDIO_COMPLETED);

		assertFalse(completed.get());
	}

	/**
	 * Verifica il cambio modalita' a runtime: dopo essere tornati a sequenziale, la
	 * fine della traccia deve chiudere la riproduzione.
	 */
	@Test
	void changingModeToSequentialShouldCompleteTrackNormally() {
		SongPlayable playable = new SongPlayable(createTestSong());
		AtomicBoolean completed = new AtomicBoolean(false);

		playable.setPlaybackMode(PlaybackMode.LOOP_TRACK);
		playable.setPlaybackMode(PlaybackMode.SEQUENTIAL);
		playable.getEvents().subscribe(EventType.PLAYABLE_COMPLETED, eventType -> completed.set(true));

		playable.update(EventType.AUDIO_COMPLETED);

		assertTrue(completed.get());
	}

	private Song createTestSong() {
		return new Song("Test Song", "Test Author", Genre.POP, 2024, 180, "fake/path/test.wav");
	}
}
