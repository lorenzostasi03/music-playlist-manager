package it.unisa.musicplaylistmanager.model.playback;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Song;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class SongPlayableTest {

	@Test
	void constructorShouldRejectNullSong() {
		assertThrows(IllegalArgumentException.class, () -> new SongPlayable(null));
	}

	@Test
	void getCurrentSongShouldReturnWrappedSong() {
		Song song = createTestSong();
		SongPlayable playable = new SongPlayable(song);

		assertSame(song, playable.getCurrentSong());
	}

	@Test
	void audioCompletedShouldNotifyPlayableCompleted() {
		Song song = createTestSong();
		SongPlayable playable = new SongPlayable(song);

		AtomicBoolean completed = new AtomicBoolean(false);

		playable.getEvents().subscribe(EventType.PLAYABLE_COMPLETED, eventType -> completed.set(true));

		playable.update(EventType.AUDIO_COMPLETED);

		assertTrue(completed.get());
	}

	@Test
	void newSongPlayableShouldUseSequentialMode() {
		SongPlayable playable = new SongPlayable(createTestSong());

		// All'inizio una singola traccia usa la modalita' normale.
		assertSame(PlaybackMode.SEQUENTIAL, playable.getPlaybackMode());
	}

	@Test
	void loopTrackShouldNotNotifyPlayableCompleted() {
		SongPlayable playable = new SongPlayable(createTestSong());
		AtomicBoolean completed = new AtomicBoolean(false);

		playable.getEvents().subscribe(EventType.PLAYABLE_COMPLETED, eventType -> completed.set(true));
		playable.setPlaybackMode(PlaybackMode.LOOP_TRACK);
		playable.update(EventType.AUDIO_COMPLETED);

		// In loop traccia il brano deve ripartire, quindi non deve risultare finito.
		assertFalse(completed.get());
	}

	@Test
	void changingBackToSequentialShouldNotifyPlayableCompleted() {
		SongPlayable playable = new SongPlayable(createTestSong());
		AtomicBoolean completed = new AtomicBoolean(false);

		playable.getEvents().subscribe(EventType.PLAYABLE_COMPLETED, eventType -> completed.set(true));
		playable.setPlaybackMode(PlaybackMode.LOOP_TRACK);
		playable.setPlaybackMode(PlaybackMode.SEQUENTIAL);
		playable.update(EventType.AUDIO_COMPLETED);

		// Se cambio modalita' a runtime, il comportamento deve aggiornarsi subito.
		assertTrue(completed.get());
	}

	private Song createTestSong() {
		return new Song("Test Song", "Test Author", Genre.POP, 2024, 180, "fake/path/test.wav");
	}
}
