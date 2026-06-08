package it.unisa.musicplaylistmanager.model.library;

import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di unità per la classe PlaylistCatalog.
 */
class PlaylistCatalogTest {

	private PlaylistCatalog catalog;

	private Playlist p1;

	private Playlist p2;

	@BeforeEach
	void setUp() {
		catalog = new PlaylistCatalog();
		p1 = new Playlist("Rock Classics");
		p2 = new Playlist("Pop Hits");
	}

	/**
	 * Verifica che il catalogo delle playlist appena creato sia vuoto.
	 */
	@Test
	void testCollezioneNuovaVuota() {
		assertTrue(catalog.isEmpty());
		assertEquals(0, catalog.size());
	}

	/**
	 * Verifica che una playlist valida possa essere aggiunta.
	 */
	@Test
	void testAggiuntaPlaylistValida() {
		catalog.addPlaylist(p1);
		assertEquals(1, catalog.size());
		assertTrue(catalog.existsByName("Rock Classics"));
	}

	/**
	 * Il sistema non deve permettere di avere due playlist chiamate allo stesso
	 * modo.
	 */
	@Test
	void testAggiuntaPlaylistDuplicataLanciaEccezione() {
		catalog.addPlaylist(p1);
		Playlist duplicata = new Playlist("Rock Classics");
		assertThrows(IllegalArgumentException.class, () -> catalog.addPlaylist(duplicata));
	}

	/**
	 * Verifica che il controllo del nome sia case-insensitive.
	 */
	@Test
	void testUnicitaNomeCaseInsensitive() {
		catalog.addPlaylist(p1);
		Playlist duplicata = new Playlist("ROCK CLASSICS");
		assertThrows(IllegalArgumentException.class, () -> catalog.addPlaylist(duplicata));
	}

	/**
	 * Verifica che aggiungere una playlist null lanci un'eccezione.
	 */
	@Test
	void testAggiuntaPlaylistNullLanciaEccezione() {
		assertThrows(IllegalArgumentException.class, () -> catalog.addPlaylist(null));
	}

	/**
	 * Verifica che una playlist presente nel catalogo possa essere rimossa
	 * correttamente.
	 */
	@Test
	void testRimozionePlaylistPresente() {
		catalog.addPlaylist(p1);
		catalog.removePlaylist(p1);
		assertFalse(catalog.existsByName("Rock Classics"));
		assertTrue(catalog.isEmpty());
	}

	/**
	 * Verifica che rimuovere una playlist non presente nel catalogo lanci
	 * un'eccezione.
	 */
	@Test
	void testRimozionePlaylistAssenteLanciaEccezione() {
		assertThrows(IllegalArgumentException.class, () -> catalog.removePlaylist(p1));
	}

	/**
	 * Verifica che chiamare una nuova playlist con un nome già usato da un'altra
	 * playlist restituisca true.
	 */
	@Test
	void testIsNameTakenByOtherNomeEsistente() {
		catalog.addPlaylist(p1);
		catalog.addPlaylist(p2);
		assertTrue(catalog.isNameTakenByOther("Rock Classics", "Pop Hits"));
	}

	/**
	 * Assicura che il controllo sul nome permetta il salvataggio se l'utente lascia
	 * il nome inalterato.
	 */
	@Test
	void testIsNameTakenByOtherStessoNome() {
		catalog.addPlaylist(p1);
		assertFalse(catalog.isNameTakenByOther("Rock Classics", "Rock Classics"));
	}

	/**
	 * Controlla che la ricerca incrociata funzioni correttamente: se cerco una
	 * canzone specifica, il catalogo deve restituirmi solo le playlist in cui quel
	 * brano è stato effettivamente aggiunto.
	 */
	@Test
	void testGetPlaylistsContaining() {
		Song song = new Song("Test", "Artist", Genre.POP, 2020, 100, "/t.mp3");
		p1.addSong(song);
		catalog.addPlaylist(p1);
		catalog.addPlaylist(p2);

		List<Playlist> result = catalog.getPlaylistsContaining(song);
		assertEquals(1, result.size());
		assertTrue(result.contains(p1));
	}

	/**
	 * Verifica che passando una traccia nulla, il sistema non vada in crash ma
	 * gestisca il caso restituendo una lista vuota.
	 */
	@Test
	void testGetPlaylistsContainingNull() {
		assertTrue(catalog.getPlaylistsContaining(null).isEmpty());
	}

	/**
	 * Garantisce che la lista delle playlist restituita dal catalogo sia in sola
	 * lettura, in modo da prevenire modifiche accidentali o indesiderate
	 * dall'esterno della classe.
	 */
	@Test
	void testGetAllPlaylistsImmutabile() {
		assertThrows(UnsupportedOperationException.class, () -> catalog.getAllPlaylists().add(p1));
	}
}
