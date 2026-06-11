package it.unisa.musicplaylistmanager.model.playback;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Song;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unisa.musicplaylistmanager.model.playback.events.EventType;
import it.unisa.musicplaylistmanager.model.playback.playable.SongPlayable;
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

	private Song createTestSong() {
		return new Song("Test Song", "Test Author", Genre.POP, 2024, 180, "fake/path/test.wav");
	}
}
