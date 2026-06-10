package it.unisa.musicplaylistmanager.model.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test sulle modalita' di riproduzione della playlist.
 *
 * Qui non facciamo partire audio veri: ci interessa controllare che gli indici e
 * gli eventi si comportino come richiesto dalle user stories.
 */
class PlaylistPlayableTest {

	private Playlist playlist;
	private Song firstSong;
	private Song secondSong;
	private Song thirdSong;

	@BeforeEach
	void setUp() {
		playlist = new Playlist("Playlist test");
		firstSong = new Song("Prima", "Autore", Genre.POP, 2024, 3, "fake/first.wav");
		secondSong = new Song("Seconda", "Autore", Genre.ROCK, 2024, 3, "fake/second.wav");
		thirdSong = new Song("Terza", "Autore", Genre.HIP_HOP, 2024, 3, "fake/third.wav");

		playlist.addSong(firstSong);
		playlist.addSong(secondSong);
		playlist.addSong(thirdSong);
	}

	/**
	 * Controlla che non si possa creare un PlaylistPlayable senza playlist.
	 */
	@Test
	void constructorShouldRejectNullPlaylist() {
		assertThrows(IllegalArgumentException.class, () -> new PlaylistPlayable(null));
	}

	/**
	 * Verifica che la playlist parta dalla prima traccia e in modalita'
	 * sequenziale.
	 */
	@Test
	void defaultModeShouldBeSequentialAndStartFromFirstSong() {
		TestablePlaylistPlayable playable = new TestablePlaylistPlayable(playlist);

		assertEquals(PlaybackMode.SEQUENTIAL, playable.getPlaybackMode());
		assertSame(firstSong, playable.getCurrentSong());
	}

	/**
	 * Verifica US22: in sequenziale la fine di una traccia porta a quella
	 * successiva.
	 */
	@Test
	void sequentialModeShouldMoveToNextSongOnAudioCompleted() {
		TestablePlaylistPlayable playable = new TestablePlaylistPlayable(playlist);

		playable.update(EventType.AUDIO_COMPLETED);

		assertSame(secondSong, playable.getCurrentSong());
		assertEquals(1, playable.playRequests);
	}

	/**
	 * Verifica US22: arrivati alla fine della playlist, la riproduzione termina.
	 */
	@Test
	void sequentialModeShouldCompleteAtEndOfPlaylist() {
		TestablePlaylistPlayable playable = new TestablePlaylistPlayable(playlist);
		AtomicBoolean completed = new AtomicBoolean(false);

		playable.skipNext();
		playable.skipNext();
		playable.getEvents().subscribe(EventType.PLAYABLE_COMPLETED, eventType -> completed.set(true));

		playable.update(EventType.AUDIO_COMPLETED);

		assertTrue(completed.get());
		assertSame(thirdSong, playable.getCurrentSong());
	}

	/**
	 * Verifica US24: con loop traccia, la fine dell'audio non cambia traccia e non
	 * chiude il playable.
	 */
	@Test
	void loopTrackShouldKeepCurrentSongOnAudioCompleted() {
		TestablePlaylistPlayable playable = new TestablePlaylistPlayable(playlist);
		AtomicBoolean completed = new AtomicBoolean(false);

		playable.setPlaybackMode(PlaybackMode.LOOP_TRACK);
		playable.getEvents().subscribe(EventType.PLAYABLE_COMPLETED, eventType -> completed.set(true));

		playable.update(EventType.AUDIO_COMPLETED);

		assertFalse(completed.get());
		assertSame(firstSong, playable.getCurrentSong());
	}

	/**
	 * Verifica US24: anche se c'e' il loop traccia, lo skip manuale deve andare
	 * avanti.
	 */
	@Test
	void manualSkipShouldWorkDuringLoopTrack() {
		TestablePlaylistPlayable playable = new TestablePlaylistPlayable(playlist);

		playable.setPlaybackMode(PlaybackMode.LOOP_TRACK);
		playable.skipNext();

		assertSame(secondSong, playable.getCurrentSong());
	}

	/**
	 * Verifica US25: in loop playlist, dopo l'ultima traccia si torna alla prima.
	 */
	@Test
	void loopPlaylistShouldRestartFromFirstSongAfterLastSong() {
		TestablePlaylistPlayable playable = new TestablePlaylistPlayable(playlist);

		playable.setPlaybackMode(PlaybackMode.LOOP_PLAYLIST);
		playable.skipNext();
		playable.skipNext();
		playable.update(EventType.AUDIO_COMPLETED);

		assertSame(firstSong, playable.getCurrentSong());
	}

	/**
	 * Verifica US25: lo skip sull'ultima traccia, in loop playlist, torna subito
	 * alla prima.
	 */
	@Test
	void manualSkipShouldWrapToFirstSongDuringLoopPlaylist() {
		TestablePlaylistPlayable playable = new TestablePlaylistPlayable(playlist);

		playable.setPlaybackMode(PlaybackMode.LOOP_PLAYLIST);
		playable.skipNext();
		playable.skipNext();
		playable.skipNext();

		assertSame(firstSong, playable.getCurrentSong());
	}

	/**
	 * Verifica il cambio modalita' a runtime: se torno a sequenziale, l'ultima
	 * traccia non deve piu' ricominciare dalla prima.
	 */
	@Test
	void changingModeToSequentialShouldStopLoopPlaylist() {
		TestablePlaylistPlayable playable = new TestablePlaylistPlayable(playlist);
		AtomicBoolean completed = new AtomicBoolean(false);

		playable.setPlaybackMode(PlaybackMode.LOOP_PLAYLIST);
		playable.setPlaybackMode(PlaybackMode.SEQUENTIAL);
		playable.skipNext();
		playable.skipNext();
		playable.getEvents().subscribe(EventType.PLAYABLE_COMPLETED, eventType -> completed.set(true));

		playable.update(EventType.AUDIO_COMPLETED);

		assertTrue(completed.get());
		assertSame(thirdSong, playable.getCurrentSong());
	}

	/**
	 * Versione usata solo nei test: conta quante volte verrebbe avviata una
	 * traccia, senza provare ad aprire file audio reali.
	 */
	private static class TestablePlaylistPlayable extends PlaylistPlayable {

		private int playRequests;

		TestablePlaylistPlayable(Playlist playlist) {
			super(playlist);
			this.playRequests = 0;
		}

		@Override
		protected void playCurrentSong() {
			playRequests++;
		}
	}
}
