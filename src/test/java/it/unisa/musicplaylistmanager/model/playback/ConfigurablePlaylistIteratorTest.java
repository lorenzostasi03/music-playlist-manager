package it.unisa.musicplaylistmanager.model.playback;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test dell'iteratore configurabile mostrato nel diagramma delle classi.
 *
 * L'idea e' controllare che lo stesso iteratore cambi comportamento quando gli
 * viene assegnata una strategia diversa.
 */
class ConfigurablePlaylistIteratorTest {

	private Playlist playlist;
	private Song firstSong;
	private Song secondSong;
	private Song thirdSong;

	@BeforeEach
	void setUp() {
		playlist = new Playlist("Iterator test");
		firstSong = new Song("Prima", "Autore", Genre.POP, 2024, 3, "fake/first.wav");
		secondSong = new Song("Seconda", "Autore", Genre.ROCK, 2024, 3, "fake/second.wav");
		thirdSong = new Song("Terza", "Autore", Genre.HIP_HOP, 2024, 3, "fake/third.wav");

		playlist.addSong(firstSong);
		playlist.addSong(secondSong);
		playlist.addSong(thirdSong);
	}

	/**
	 * Verifica che la playlist crei davvero un iteratore configurabile.
	 */
	@Test
	void playlistShouldCreateConfigurableIterator() {
		PlaylistIterator iterator = playlist.createIterator();

		assertTrue(iterator instanceof ConfigurablePlaylistIterator);
	}

	/**
	 * Verifica la strategia sequenziale: si va avanti in ordine e alla fine non
	 * c'e' piu' una traccia successiva.
	 */
	@Test
	void sequentialStrategyShouldMoveForwardUntilLastSong() {
		ConfigurablePlaylistIterator iterator = new ConfigurablePlaylistIterator(playlist,
				new SequentialIteratorStrategy());

		assertSame(firstSong, iterator.getCurrentSong());
		assertSame(secondSong, iterator.next());
		assertSame(thirdSong, iterator.next());
		assertFalse(iterator.hasNext());
	}

	/**
	 * Verifica la strategia loop: dopo l'ultima traccia si torna alla prima.
	 */
	@Test
	void loopStrategyShouldReturnToFirstSongAfterLastSong() {
		ConfigurablePlaylistIterator iterator = new ConfigurablePlaylistIterator(playlist, new LoopIteratorStrategy());

		iterator.next();
		iterator.next();

		assertSame(firstSong, iterator.next());
	}

	/**
	 * Verifica il cambio strategia a runtime: prima sequenziale, poi loop.
	 */
	@Test
	void iteratorShouldChangeStrategyAtRuntime() {
		ConfigurablePlaylistIterator iterator = new ConfigurablePlaylistIterator(playlist,
				new SequentialIteratorStrategy());

		iterator.next();
		iterator.next();
		iterator.setStrategy(new LoopIteratorStrategy());

		assertSame(firstSong, iterator.next());
	}

	/**
	 * Controlla che l'iteratore non accetti una strategia null.
	 */
	@Test
	void setStrategyShouldRejectNullStrategy() {
		ConfigurablePlaylistIterator iterator = new ConfigurablePlaylistIterator(playlist);

		assertThrows(IllegalArgumentException.class, () -> iterator.setStrategy(null));
	}
}
