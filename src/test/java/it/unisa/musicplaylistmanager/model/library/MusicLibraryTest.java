package it.unisa.musicplaylistmanager.model.library;

import static org.junit.jupiter.api.Assertions.*;

import it.unisa.musicplaylistmanager.fake.FakePlaylistDAO;
import it.unisa.musicplaylistmanager.fake.FakeSongDAO;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test della classe MusicLibrary.
 */
class MusicLibraryTest {

	private MusicLibrary library;
	private FakeSongDAO songDAO;
	private FakePlaylistDAO playlistDAO;

	private Song song;
	private Playlist playlist;

	@BeforeEach
	void setUp() {
		songDAO = new FakeSongDAO();
		playlistDAO = new FakePlaylistDAO();

		library = new MusicLibrary(songDAO, playlistDAO);

		song = new Song("Yesterday", "Beatles", Genre.POP, 1965, 125, "/y.mp3");
		playlist = new Playlist("60s Classics");
	}

	@Test
	void aggiuntaBranoAlCatalogo() {
		library.addSongToCatalog(song);

		assertTrue(library.catalogContains(song));
		assertEquals(1, library.getAllSongs().size());
	}

	@Test
	void rimozioneBranoPropagataAllePlaylist() {
		library.addSongToCatalog(song);
		library.addPlaylist(playlist);
		library.addSongToPlaylist(song, playlist);

		library.removeSongFromCatalog(song);

		assertFalse(library.catalogContains(song));
		assertFalse(playlist.contains(song));
	}

	@Test
	void rimozioneBranoNonPresenteNonGeneraEccezione() {
		assertDoesNotThrow(() -> library.removeSongFromCatalog(song));

		assertFalse(library.catalogContains(song));
	}

	@Test
	void creazionePlaylist() {
		library.addPlaylist(playlist);

		assertTrue(library.getAllPlaylists().contains(playlist));
	}

	@Test
	void eliminazionePlaylist() {
		library.addPlaylist(playlist);
		library.removePlaylist(playlist);

		assertFalse(library.getAllPlaylists().contains(playlist));
	}

	@Test
	void rinominaPlaylistConNomeValido() {
		library.addPlaylist(playlist);

		library.renamePlaylist(playlist, "Best of 60s");

		assertEquals("Best of 60s", playlist.getName());
	}

	@Test
	void rinominaPlaylistConNomeDuplicatoLanciaEccezione() {
		library.addPlaylist(playlist);
		library.addPlaylist(new Playlist("Best of 60s"));

		assertThrows(IllegalArgumentException.class, () -> library.renamePlaylist(playlist, "Best of 60s"));
	}

	@Test
	void rinominaPlaylistNullLanciaEccezione() {
		assertThrows(IllegalArgumentException.class, () -> library.renamePlaylist(null, "NuovoNome"));
	}

	@Test
	void aggiuntaBranoAPlaylist() {
		library.addSongToCatalog(song);
		library.addPlaylist(playlist);

		library.addSongToPlaylist(song, playlist);

		assertTrue(playlist.contains(song));
	}

	@Test
	void aggiuntaBranoNonPresenteNelCatalogoLanciaEccezione() {
		library.addPlaylist(playlist);

		assertThrows(IllegalArgumentException.class, () -> library.addSongToPlaylist(song, playlist));
	}

	@Test
	void rimozioneBranoDaPlaylistNonInfluisceSulCatalogo() {
		library.addSongToCatalog(song);
		library.addPlaylist(playlist);
		library.addSongToPlaylist(song, playlist);

		library.removeSongFromPlaylist(song, playlist);

		assertFalse(playlist.contains(song));
		assertTrue(library.catalogContains(song));
	}

	@Test
	void spostamentoBranoInPlaylistConPosizioneValida() {
		Song secondSong = new Song("Hey Jude", "Beatles", Genre.POP, 1968, 431, "/hey-jude.mp3");

		library.addSongToCatalog(song);
		library.addSongToCatalog(secondSong);
		library.addPlaylist(playlist);
		library.addSongToPlaylist(song, playlist);
		library.addSongToPlaylist(secondSong, playlist);

		library.moveSongInPlaylist(secondSong, playlist, 0);

		assertEquals(secondSong, playlist.getSongAt(0));
		assertEquals(song, playlist.getSongAt(1));
	}

	@Test
	void spostamentoBranoInUltimaPosizione() {
		Song secondSong = new Song("Hey Jude", "Beatles", Genre.POP, 1968, 431, "/hey-jude.mp3");
		Song thirdSong = new Song("Come Together", "Beatles", Genre.ROCK, 1969, 259, "/come-together.mp3");

		library.addSongToCatalog(song);
		library.addSongToCatalog(secondSong);
		library.addSongToCatalog(thirdSong);
		library.addPlaylist(playlist);
		library.addSongToPlaylist(song, playlist);
		library.addSongToPlaylist(secondSong, playlist);
		library.addSongToPlaylist(thirdSong, playlist);

		library.moveSongInPlaylist(song, playlist, 2);

		assertEquals(secondSong, playlist.getSongAt(0));
		assertEquals(thirdSong, playlist.getSongAt(1));
		assertEquals(song, playlist.getSongAt(2));
	}

	@Test
	void spostamentoBranoInPlaylistConPosizioneNonValidaLanciaEccezione() {
		library.addSongToCatalog(song);
		library.addPlaylist(playlist);
		library.addSongToPlaylist(song, playlist);

		assertThrows(IllegalArgumentException.class, () -> library.moveSongInPlaylist(song, playlist, 5));
	}

	@Test
	void creazioneAutomaticaPlaylistCombinaCriteriInOr() {
		Song rockSong = new Song("Come Together", "Beatles", Genre.ROCK, 1969, 259, "/come-together.mp3");

		Song songFrom1968 = new Song("Hey Jude", "Beatles", Genre.POP, 1968, 431, "/hey-jude.mp3");

		library.addSongToCatalog(song);
		library.addSongToCatalog(rockSong);
		library.addSongToCatalog(songFrom1968);

		Playlist generatedPlaylist = library.createAutomaticPlaylist("Rock e 1968", Set.of(Genre.ROCK), Set.of(1968),
				Set.of());

		assertAll(() -> assertEquals("Rock e 1968", generatedPlaylist.getName()),
				() -> assertEquals(2, generatedPlaylist.size()), () -> assertTrue(generatedPlaylist.contains(rockSong)),
				() -> assertTrue(generatedPlaylist.contains(songFrom1968)),
				() -> assertFalse(generatedPlaylist.contains(song)),
				() -> assertTrue(library.getAllPlaylists().contains(generatedPlaylist)));
	}

	@Test
	void creazioneAutomaticaNonDuplicaBranoCheSoddisfaPiuCriteri() {
		library.addSongToCatalog(song);

		Playlist generatedPlaylist = library.createAutomaticPlaylist("Pop del 1965", Set.of(Genre.POP), Set.of(1965),
				Set.of());

		assertAll(() -> assertEquals(1, generatedPlaylist.size()),
				() -> assertEquals(song, generatedPlaylist.getSongAt(0)));
	}

	@Test
	void creazioneAutomaticaSenzaCriteriLanciaEccezione() {
		library.addSongToCatalog(song);

		assertThrows(IllegalArgumentException.class,
				() -> library.createAutomaticPlaylist("Playlist automatica", Set.of(), Set.of(), Set.of()));
	}
}
