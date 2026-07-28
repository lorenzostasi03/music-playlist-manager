package it.unisa.musicplaylistmanager.controller.command;

import static org.junit.jupiter.api.Assertions.*;

import it.unisa.musicplaylistmanager.fake.FakePlaylistDAO;
import it.unisa.musicplaylistmanager.fake.FakeSongDAO;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.library.MusicLibrary;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test della classe {@link AddSongsToPlaylistCommand}.
 */
class AddSongsToPlaylistCommandTest {

	private MusicLibrary musicLibrary;
	private Playlist playlist;

	private Song firstSong;
	private Song secondSong;

	private AddSongsToPlaylistCommand command;

	@BeforeEach
	void setUp() {
		musicLibrary = new MusicLibrary(new FakeSongDAO(), new FakePlaylistDAO());

		firstSong = new Song("Yesterday", "Beatles", Genre.POP, 1965, 125, "/yesterday.mp3");

		secondSong = new Song("Come Together", "Beatles", Genre.ROCK, 1969, 259, "/come-together.mp3");

		playlist = new Playlist("Beatles");

		musicLibrary.addSongToCatalog(firstSong);
		musicLibrary.addSongToCatalog(secondSong);
		musicLibrary.addPlaylist(playlist);

		command = new AddSongsToPlaylistCommand(musicLibrary, playlist, List.of(firstSong, secondSong));
	}

	@Test
	void costruttoreConMusicLibraryNullLanciaEccezione() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new AddSongsToPlaylistCommand(null, playlist, List.of(firstSong, secondSong)));

		assertEquals("MusicLibrary non può essere null!", exception.getMessage());
	}

	@Test
	void executeAggiungeTuttiIBraniAllaPlaylist() {
		command.execute();

		assertAll(() -> assertEquals(2, playlist.size()), () -> assertTrue(playlist.contains(firstSong)),
				() -> assertTrue(playlist.contains(secondSong)));
	}

	@Test
	void executeMantieneOrdineDeiBrani() {
		command.execute();

		assertAll(() -> assertEquals(firstSong, playlist.getSongAt(0)),
				() -> assertEquals(secondSong, playlist.getSongAt(1)));
	}

	@Test
	void undoRimuoveTuttiIBraniAggiunti() {
		command.execute();

		command.undo();

		assertAll(() -> assertTrue(playlist.isEmpty()), () -> assertFalse(playlist.contains(firstSong)),
				() -> assertFalse(playlist.contains(secondSong)));
	}

	@Test
	void executeSeguitoDaUndoRipristinaLoStatoIniziale() {
		int initialSize = playlist.size();

		command.execute();
		command.undo();

		assertEquals(initialSize, playlist.size());
	}

	@Test
	void executeConListaVuotaNonModificaLaPlaylist() {
		AddSongsToPlaylistCommand emptyCommand = new AddSongsToPlaylistCommand(musicLibrary, playlist, List.of());

		assertDoesNotThrow(emptyCommand::execute);
		assertTrue(playlist.isEmpty());
	}
}
