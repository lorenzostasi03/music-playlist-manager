package it.unisa.musicplaylistmanager.controller.command;

import static org.junit.jupiter.api.Assertions.*;

import it.unisa.musicplaylistmanager.fake.FakePlaylistDAO;
import it.unisa.musicplaylistmanager.fake.FakeSongDAO;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.library.MusicLibrary;
import it.unisa.musicplaylistmanager.model.playback.playable.PlaylistPlayable;
import it.unisa.musicplaylistmanager.model.playback.player.Player;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test della classe {@link RemovePlaylistCommand}.
 */
class RemovePlaylistCommandTest {

	private MusicLibrary musicLibrary;
	private Player player;
	private Playlist playlist;
	private Song song;
	private RemovePlaylistCommand command;

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

		command = new RemovePlaylistCommand(
				musicLibrary,
				player,
				playlist);
	}

	@Test
	void costruttoreConMusicLibraryNullLanciaEccezione() {
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> new RemovePlaylistCommand(
						null,
						player,
						playlist));

		assertEquals(
				"MusicLibrary non può essere null!",
				exception.getMessage());
	}

	@Test
	void costruttoreConPlayerNullLanciaEccezione() {
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> new RemovePlaylistCommand(
						musicLibrary,
						null,
						playlist));

		assertEquals(
				"Player non può essere null!",
				exception.getMessage());
	}

	@Test
	void executeRimuovePlaylistDallaLibreria() {
		command.execute();

		assertAll(
				() -> assertFalse(
						musicLibrary.getAllPlaylists().contains(playlist)),
				() -> assertTrue(
						musicLibrary.getAllPlaylists().isEmpty()));
	}

	@Test
	void executeRimuovePlaylistDallaCoda() {
		PlaylistPlayable queuedPlayable =
				new PlaylistPlayable(playlist);

		player.enqueue(queuedPlayable);

		command.execute();

		assertAll(
				() -> assertFalse(
						player.getQueueSnapshot().contains(queuedPlayable)),
				() -> assertTrue(
						player.getQueueSnapshot().isEmpty()));
	}

	@Test
	void executeRimuoveTutteLeOccorrenzeDellaPlaylistDallaCoda() {
		PlaylistPlayable firstOccurrence =
				new PlaylistPlayable(playlist);

		PlaylistPlayable secondOccurrence =
				new PlaylistPlayable(playlist);

		player.enqueue(firstOccurrence);
		player.enqueue(secondOccurrence);

		command.execute();

		assertTrue(player.getQueueSnapshot().isEmpty());
	}

	@Test
	void executeNonRimuoveAltrePlaylistDallaCoda() {
		Playlist otherPlaylist = new Playlist("Workout");
		otherPlaylist.addSong(song);

		PlaylistPlayable targetPlayable =
				new PlaylistPlayable(playlist);

		PlaylistPlayable otherPlayable =
				new PlaylistPlayable(otherPlaylist);

		player.enqueue(targetPlayable);
		player.enqueue(otherPlayable);

		command.execute();

		assertEquals(
				List.of(otherPlayable),
				player.getQueueSnapshot());
	}

	@Test
	void executeConPlaylistNonPresenteInCodaNonModificaGliAltriElementi() {
		Playlist otherPlaylist = new Playlist("Workout");
		otherPlaylist.addSong(song);

		PlaylistPlayable otherPlayable =
				new PlaylistPlayable(otherPlaylist);

		player.enqueue(otherPlayable);

		command.execute();

		assertEquals(
				List.of(otherPlayable),
				player.getQueueSnapshot());
	}

	@Test
	void undoReinseriscePlaylistNellaLibreria() {
		command.execute();

		command.undo();

		assertAll(
				() -> assertTrue(
						musicLibrary.getAllPlaylists().contains(playlist)),
				() -> assertEquals(
						1,
						musicLibrary.getAllPlaylists().size()));
	}

	@Test
	void executeSeguitoDaUndoRipristinaNumeroInizialeDiPlaylist() {
		int initialSize = musicLibrary.getAllPlaylists().size();

		command.execute();
		command.undo();

		assertEquals(
				initialSize,
				musicLibrary.getAllPlaylists().size());
	}
}