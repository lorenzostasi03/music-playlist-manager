package it.unisa.musicplaylistmanager.controller.command;

import static org.junit.jupiter.api.Assertions.*;

import it.unisa.musicplaylistmanager.fake.FakePlaylistDAO;
import it.unisa.musicplaylistmanager.fake.FakeSongDAO;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.library.MusicLibrary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test della classe {@link RemoveSongFromPlaylistCommand}.
 */
class RemoveSongFromPlaylistCommandTest {

	private MusicLibrary musicLibrary;
	private FakeSongDAO songDAO;
	private FakePlaylistDAO playlistDAO;

	private Playlist playlist;
	private Song song;
	private Song otherSong;

	private RemoveSongFromPlaylistCommand command;

	@BeforeEach
	void setUp() {
		songDAO = new FakeSongDAO();
		playlistDAO = new FakePlaylistDAO();

		musicLibrary = new MusicLibrary(songDAO, playlistDAO);

		song = new Song("Yesterday", "Beatles", Genre.POP, 1965, 125, "/yesterday.mp3");

		otherSong = new Song("Whole lotta love", "Led Zeppelin", Genre.ROCK, 1969, 280, "/whole-lotta-love.mp3");

		playlist = new Playlist("Mia playlist");

		musicLibrary.addSongToCatalog(song);
		musicLibrary.addSongToCatalog(otherSong);
		musicLibrary.addPlaylist(playlist);
		musicLibrary.addSongToPlaylist(song, playlist);
		musicLibrary.addSongToPlaylist(otherSong, playlist);

		command = new RemoveSongFromPlaylistCommand(musicLibrary, playlist, song);
	}

	@Test
	void costruttoreConMusicLibraryNullLanciaEccezione() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new RemoveSongFromPlaylistCommand(null, playlist, song));

		assertEquals("MusicLibrary non può essere null!", exception.getMessage());
	}

	@Test
	void executeRimuoveBranoDallaPlaylist() {
		command.execute();

		assertAll(() -> assertFalse(playlist.contains(song)), () -> assertEquals(1, playlist.size()));
	}

	@Test
	void executeNonRimuoveGliAltriBraniDallaPlaylist() {
		command.execute();

		assertAll(() -> assertTrue(playlist.contains(otherSong)), () -> assertEquals(otherSong, playlist.getSongAt(0)));
	}

	@Test
	void executeNonRimuoveBranoDalCatalogo() {
		command.execute();

		assertAll(() -> assertTrue(musicLibrary.catalogContains(song)),
				() -> assertEquals(2, musicLibrary.getAllSongs().size()));
	}

	@Test
	void undoReinserisceBranoNellaPlaylist() {
		command.execute();

		command.undo();

		assertAll(() -> assertTrue(playlist.contains(song)), () -> assertEquals(2, playlist.size()));
	}

	@Test
	void executeSeguitoDaUndoRipristinaNumeroInizialeDiBrani() {
		int initialSize = playlist.size();

		command.execute();
		command.undo();

		assertEquals(initialSize, playlist.size());
	}

	@Test
	void executeRimuoveAssociazioneDalPlaylistDAO() {
		command.execute();

		assertFalse(playlistDAO.getSongIds(playlist.getId()).contains(song.getId()));
	}

	@Test
	void undoRipristinaAssociazioneNelPlaylistDAO() {
		command.execute();
		command.undo();

		assertTrue(playlistDAO.getSongIds(playlist.getId()).contains(song.getId()));
	}
}
