package it.unisa.musicplaylistmanager.controller.command;

import static org.junit.jupiter.api.Assertions.*;

import it.unisa.musicplaylistmanager.fake.FakePlaylistDAO;
import it.unisa.musicplaylistmanager.fake.FakeSongDAO;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.library.MusicLibrary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test della classe {@link AddPlaylistCommand}.
 */
class AddPlaylistCommandTest {

	private MusicLibrary musicLibrary;
	private Playlist playlist;
	private AddPlaylistCommand command;

	@BeforeEach
	void setUp() {
		musicLibrary = new MusicLibrary(
				new FakeSongDAO(),
				new FakePlaylistDAO());

		playlist = new Playlist("Playlist di test");
		command = new AddPlaylistCommand(musicLibrary, playlist);
	}

	@Test
	void costruttoreConMusicLibraryNullLanciaEccezione() {
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> new AddPlaylistCommand(null, playlist));

		assertEquals(
				"MusicLibrary non può essere null!",
				exception.getMessage());
	}

	@Test
	void executeAggiungePlaylistAllaLibreria() {
		command.execute();

		assertAll(
				() -> assertTrue(
						musicLibrary.getAllPlaylists().contains(playlist)),
				() -> assertEquals(
						1,
						musicLibrary.getAllPlaylists().size()));
	}

	@Test
	void undoRimuovePlaylistDallaLibreria() {
		command.execute();

		command.undo();

		assertAll(
				() -> assertFalse(
						musicLibrary.getAllPlaylists().contains(playlist)),
				() -> assertTrue(
						musicLibrary.getAllPlaylists().isEmpty()));
	}

	@Test
	void executeSeguitoDaUndoRipristinaLoStatoIniziale() {
		int initialSize = musicLibrary.getAllPlaylists().size();

		command.execute();
		command.undo();

		assertEquals(
				initialSize,
				musicLibrary.getAllPlaylists().size());
	}
}