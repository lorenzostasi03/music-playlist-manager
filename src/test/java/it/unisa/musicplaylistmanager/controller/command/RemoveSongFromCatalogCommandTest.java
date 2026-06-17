package it.unisa.musicplaylistmanager.controller.command;

import static org.junit.jupiter.api.Assertions.*;

import it.unisa.musicplaylistmanager.fake.FakePlaylistDAO;
import it.unisa.musicplaylistmanager.fake.FakeSongDAO;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.library.MusicLibrary;
import it.unisa.musicplaylistmanager.model.playback.playable.SongPlayable;
import it.unisa.musicplaylistmanager.model.playback.player.Player;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test della classe {@link RemoveSongFromCatalogCommand}.
 */
class RemoveSongFromCatalogCommandTest {

	private MusicLibrary musicLibrary;
	private Player player;
	private Song song;
	private Playlist playlist;
	private RemoveSongFromCatalogCommand command;

	@BeforeEach
	void setUp() {
		musicLibrary = new MusicLibrary(
				new FakeSongDAO(),
				new FakePlaylistDAO());

		player = new Player();

		song = new Song(
				"Come Together",
				"Beatles",
				Genre.ROCK,
				1969,
				259,
				"/come-together.mp3");

		playlist = new Playlist("Beatles");

		musicLibrary.addSongToCatalog(song);
		musicLibrary.addPlaylist(playlist);
		musicLibrary.addSongToPlaylist(song, playlist);

		command = new RemoveSongFromCatalogCommand(
				musicLibrary,
				player,
				song);
	}

	@Test
	void costruttoreConMusicLibraryNullLanciaEccezione() {
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> new RemoveSongFromCatalogCommand(
						null,
						player,
						song));

		assertEquals(
				"MusicLibrary non può essere null!",
				exception.getMessage());
	}

	@Test
	void costruttoreConPlayerNullLanciaEccezione() {
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> new RemoveSongFromCatalogCommand(
						musicLibrary,
						null,
						song));

		assertEquals(
				"Player non può essere null!",
				exception.getMessage());
	}

	@Test
	void executeRimuoveBranoDalCatalogo() {
		command.execute();

		assertAll(
				() -> assertFalse(musicLibrary.catalogContains(song)),
				() -> assertTrue(musicLibrary.getAllSongs().isEmpty()));
	}

	@Test
	void executeRimuoveBranoAncheDallePlaylist() {
		assertTrue(playlist.contains(song));

		command.execute();

		assertAll(
				() -> assertFalse(playlist.contains(song)),
				() -> assertTrue(playlist.isEmpty()));
	}

	@Test
	void executeRimuoveBranoDallaCoda() {
		SongPlayable queuedPlayable = new SongPlayable(song);
		player.enqueue(queuedPlayable);

		command.execute();

		assertAll(
				() -> assertFalse(
						player.getQueueSnapshot().contains(queuedPlayable)),
				() -> assertTrue(
						player.getQueueSnapshot().isEmpty()));
	}

	@Test
	void executeRimuoveTutteLeOccorrenzeDelBranoDallaCoda() {
		SongPlayable firstOccurrence = new SongPlayable(song);
		SongPlayable secondOccurrence = new SongPlayable(song);

		player.enqueue(firstOccurrence);
		player.enqueue(secondOccurrence);

		command.execute();

		assertTrue(player.getQueueSnapshot().isEmpty());
	}

	@Test
	void executeNonRimuoveAltriBraniDallaCoda() {
		Song otherSong = new Song(
				"Yesterday",
				"Beatles",
				Genre.POP,
				1965,
				125,
				"/yesterday.mp3");

		SongPlayable targetPlayable = new SongPlayable(song);
		SongPlayable otherPlayable = new SongPlayable(otherSong);

		player.enqueue(targetPlayable);
		player.enqueue(otherPlayable);

		command.execute();

		assertEquals(
				List.of(otherPlayable),
				player.getQueueSnapshot());
	}

	@Test
	void executeConBranoNonPresenteInCodaNonModificaGliAltriElementi() {
		Song otherSong = new Song(
				"Yesterday",
				"Beatles",
				Genre.POP,
				1965,
				125,
				"/yesterday.mp3");

		SongPlayable otherPlayable = new SongPlayable(otherSong);
		player.enqueue(otherPlayable);

		command.execute();

		assertEquals(
				List.of(otherPlayable),
				player.getQueueSnapshot());
	}

	@Test
	void undoReinserisceBranoNelCatalogo() {
		command.execute();

		command.undo();

		assertAll(
				() -> assertTrue(musicLibrary.catalogContains(song)),
				() -> assertEquals(1, musicLibrary.getAllSongs().size()));
	}

	@Test
	void executeSeguitoDaUndoRipristinaNumeroInizialeDiBrani() {
		int initialSize = musicLibrary.getAllSongs().size();

		command.execute();
		command.undo();

		assertEquals(initialSize, musicLibrary.getAllSongs().size());
	}
}