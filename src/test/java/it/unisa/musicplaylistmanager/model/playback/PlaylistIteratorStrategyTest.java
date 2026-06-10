package it.unisa.musicplaylistmanager.model.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import org.junit.jupiter.api.Test;

class PlaylistIteratorStrategyTest {

	@Test
	void sequentialIteratorShouldStopAfterLastSong() {
		Playlist playlist = createPlaylist();
		PlaylistIterator iterator = new ConfigurablePlaylistIterator(playlist, new SequentialIteratorStrategy());

		// In sequenziale i brani scorrono in ordine e poi la playlist termina.
		assertEquals("Prima", iterator.next().getTitle());
		assertEquals("Seconda", iterator.next().getTitle());
		assertNull(iterator.next());
	}

	@Test
	void loopIteratorShouldRestartFromFirstSong() {
		Playlist playlist = createPlaylist();
		PlaylistIterator iterator = new ConfigurablePlaylistIterator(playlist, new LoopIteratorStrategy());

		// In loop playlist, dopo l'ultimo brano si torna al primo.
		assertEquals("Prima", iterator.next().getTitle());
		assertEquals("Seconda", iterator.next().getTitle());
		assertEquals("Prima", iterator.next().getTitle());
	}

	@Test
	void iteratorShouldChangeModeAtRuntime() {
		Playlist playlist = createPlaylist();
		PlaylistIterator iterator = new ConfigurablePlaylistIterator(playlist, new SequentialIteratorStrategy());

		assertEquals("Prima", iterator.next().getTitle());
		assertEquals("Seconda", iterator.next().getTitle());

		iterator.setStrategy(new LoopIteratorStrategy());

		// Il cambio modalita' deve valere subito, senza ricreare la playlist.
		assertEquals("Prima", iterator.next().getTitle());
	}

	private Playlist createPlaylist() {
		Playlist playlist = new Playlist("Playlist test");
		playlist.addSong(new Song("Prima", "Autore", Genre.POP, 2024, 180, "fake/path/first.wav"));
		playlist.addSong(new Song("Seconda", "Autore", Genre.ROCK, 2024, 200, "fake/path/second.wav"));
		return playlist;
	}
}
