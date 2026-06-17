package it.unisa.musicplaylistmanager.controller.command;

import static org.junit.jupiter.api.Assertions.*;

import it.unisa.musicplaylistmanager.exceptions.DuplicatedSongException;
import it.unisa.musicplaylistmanager.fake.FakePlaylistDAO;
import it.unisa.musicplaylistmanager.fake.FakeSongDAO;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.library.MusicLibrary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test della classe {@link AddSongToCatalogCommand}.
 */
class AddSongToCatalogCommandTest {

	private MusicLibrary musicLibrary;
	private Song song;
	private AddSongToCatalogCommand command;

	@BeforeEach
	void setUp() {
		musicLibrary = new MusicLibrary(
				new FakeSongDAO(),
				new FakePlaylistDAO());

		song = new Song(
				"Yesterday",
				"Beatles",
				Genre.POP,
				1965,
				125,
				"/yesterday.mp3");

		command = new AddSongToCatalogCommand(musicLibrary, song);
	}

	@Test
	void costruttoreConMusicLibraryNullLanciaEccezione() {
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> new AddSongToCatalogCommand(null, song));

		assertEquals(
				"MusicLibrary non può essere null!",
				exception.getMessage());
	}

	@Test
	void executeAggiungeBranoAlCatalogo() {
		command.execute();

		assertAll(
				() -> assertTrue(musicLibrary.catalogContains(song)),
				() -> assertEquals(1, musicLibrary.getAllSongs().size()));
	}

	@Test
	void undoRimuoveBranoDalCatalogo() {
		command.execute();

		command.undo();

		assertAll(
				() -> assertFalse(musicLibrary.catalogContains(song)),
				() -> assertTrue(musicLibrary.getAllSongs().isEmpty()));
	}

	@Test
	void executeSeguitoDaUndoRipristinaLoStatoIniziale() {
		int initialSize = musicLibrary.getAllSongs().size();

		command.execute();
		command.undo();

		assertEquals(initialSize, musicLibrary.getAllSongs().size());
	}

	@Test
void executeDueVolteLanciaEccezionePerBranoDuplicato() {
	command.execute();

	assertThrows(
			DuplicatedSongException.class,
			command::execute);
}
}