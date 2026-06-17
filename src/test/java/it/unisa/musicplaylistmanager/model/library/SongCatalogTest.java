package it.unisa.musicplaylistmanager.model.library;

import it.unisa.musicplaylistmanager.exceptions.DuplicatedSongException;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.entity.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di unità per la classe SongCatalog.
 */
class SongCatalogTest {

	private SongCatalog catalog;

	private Song song1;
	private Song song2;

	@BeforeEach
	void setUp() {
		catalog = new SongCatalog();
		song1 = new Song("Bohemian Rhapsody", "Queen", Genre.ROCK, 1975, 354, "/1.mp3");
		song2 = new Song("Stairway to Heaven", "Led Zeppelin", Genre.ROCK, 1971, 482, "/2.mp3");
	}

	/**
	 * Verifica che il catalogo appena creato sia vuoto.
	 */
	@Test
	void testCatalogoNuovoVuoto() {
		assertTrue(catalog.isEmpty());
		assertEquals(0, catalog.size());
	}

	/**
	 * Verifica che l'aggiunta di una traccia valida incrementi la dimensione e che
	 * essa sia presente nel catalogo.
	 */
	@Test
	void testAggiuntaTraccia() {
		catalog.addSong(song1);
		assertEquals(1, catalog.size());
		assertTrue(catalog.contains(song1));
	}

	/**
	 * Verifica che aggiungere la stessa traccia due volte lanci un'eccezione.
	 */
	@Test
	void testAggiuntaTracciaDuplicataLanciaEccezione() {
		catalog.addSong(song1);
		assertThrows(DuplicatedSongException.class, () -> catalog.addSong(song1));
	}

	/**
	 * Verifica che una traccia presente nel catalogo possa essere rimossa con
	 * successo.
	 */
	@Test
	void testRimozioneTraccia() {
		catalog.addSong(song1);
		catalog.removeSong(song1);
		assertFalse(catalog.contains(song1));
		assertEquals(0, catalog.size());
	}

	/**
	 * Verifica che rimuovere una traccia non presente nel catalogo lanci
	 * un'eccezione.
	 */
	@Test
	void testRimozioneTracciaAssenteLanciaEccezione() {
		assertThrows(IllegalArgumentException.class, () -> catalog.removeSong(song1));
	}

	/**
	 * Verifica che vengano restituite tutte le tracce presenti nel catalogo.
	 */
	@Test
	void testGetAllSongs() {
		catalog.addSong(song1);
		catalog.addSong(song2);
		List<Song> all = catalog.getAllSongs();
		assertEquals(2, all.size());
		assertTrue(all.containsAll(List.of(song1, song2)));
	}

	/**
	 * Verifica che la lista di tutte le tracce presenti nel catalogo sia
	 * immutabile.
	 */
	@Test
	void testGetAllSongsImmutabile() {
		assertThrows(UnsupportedOperationException.class, () -> catalog.getAllSongs().add(song1));
	}

	/**
	 * Verifica che se non sono presenti tracce nel catalogo restituista false senza
	 * lanciare eccezioni.
	 */
	@Test
	void testContainsNull() {
		assertFalse(catalog.contains(null));
	}

	/**
	 * Verifica il filtro combinato per titolo, genere, autore, anno e tag.
	 */
	@Test
	void testFiltroCombinatoPerMetadatiETag() {
		song1.addTag(Tag.FAVOURITE);
		catalog.addSong(song1);
		catalog.addSong(song2);

		List<Song> result = catalog.filterSongs("bohemian", Genre.ROCK, "Queen", 1975, Tag.FAVOURITE);

		assertEquals(1, result.size());
		assertTrue(result.contains(song1));
	}

	/**
	 * Verifica che senza filtri vengano restituite tutte le tracce.
	 */
	@Test
	void testFiltroSenzaCriteriRestituisceTutto() {
		catalog.addSong(song1);
		catalog.addSong(song2);

		List<Song> result = catalog.filterSongs("", null, null, null, null);

		assertEquals(2, result.size());
		assertTrue(result.containsAll(List.of(song1, song2)));
	}

	/**
	 * Verifica che un filtro senza corrispondenze restituisca una lista vuota.
	 */
	@Test
	void testFiltroNessunRisultato() {
		catalog.addSong(song1);
		List<Song> result = catalog.filterSongs("", Genre.JAZZ, null, null, null);
		assertTrue(result.isEmpty());
	}
}
