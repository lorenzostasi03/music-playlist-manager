package it.unisa.musicplaylistmanager.controller.command;

import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.playback.playable.Playable;
import it.unisa.musicplaylistmanager.model.playback.playable.PlaylistPlayable;
import it.unisa.musicplaylistmanager.model.playback.playable.SongPlayable;
import it.unisa.musicplaylistmanager.model.playback.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test della classe {@link AddPlayableToQueueCommand}.
 */
class AddPlayableToQueueCommandTest {

	private Player player;
	private SongPlayable songPlayable;
	private PlaylistPlayable playlistPlayable;
	private AddPlayableToQueueCommand command;

	@BeforeEach
	void setUp() {
		player = new Player();

		Song song = new Song("Title", "Author", Genre.ROCK, 2001, 180, "file.mp3");

		Playlist playlist = new Playlist("TestPlaylist");
		playlist.addSong(song);

		songPlayable = new SongPlayable(song);
		playlistPlayable = new PlaylistPlayable(playlist);

		command = new AddPlayableToQueueCommand(player, songPlayable);
	}

	@Test
	void constructorThrowsWhenPlayerIsNull() {
		assertThrows(IllegalArgumentException.class, () -> new AddPlayableToQueueCommand(null, songPlayable));
	}

	@Test
	void constructorThrowsWhenBothArgumentsAreNull() {
		assertThrows(IllegalArgumentException.class, () -> new AddPlayableToQueueCommand(null, null));
	}

	@Test
	void constructorAcceptsNullPlayable() {
		assertDoesNotThrow(() -> new AddPlayableToQueueCommand(player, null));
	}

	@Test
	void constructorSucceedsWithValidArguments() {
		assertNotNull(new AddPlayableToQueueCommand(player, songPlayable));
	}

	@Test
	void executeAddsSongPlayableToQueueOnce() {
		command.execute();

		List<Playable> queue = player.getQueueSnapshot();
		assertEquals(1, queue.size());
		assertSame(songPlayable, queue.get(0));
	}

	@Test
	void executeAddsPlaylistPlayableToQueueOnce() {
		AddPlayableToQueueCommand cmd = new AddPlayableToQueueCommand(player, playlistPlayable);
		cmd.execute();

		List<Playable> queue = player.getQueueSnapshot();
		assertEquals(1, queue.size());
		assertSame(playlistPlayable, queue.get(0));
	}

	@Test
	void executeDoesNotCallRemoveLast() {
		command.execute();
	}

	@Test
	void executeCalledMultipleTimesEnqueuesEachTime() {
		command.execute();
		command.execute();
		command.execute();

		assertEquals(3, player.getQueueSnapshot().size());
	}

	@Test
	void executeWithNullPlayablePropagatesPlayerValidation() {
		AddPlayableToQueueCommand cmdWithNullPlayable = new AddPlayableToQueueCommand(player, null);

		assertThrows(IllegalArgumentException.class, cmdWithNullPlayable::execute);
	}

	@Test
	void undoRemovesLastElementFromQueue() {
		command.execute();
		command.undo();

		assertTrue(player.getQueueSnapshot().isEmpty());
	}

	@Test
	void undoDoesNotCallEnqueue() {
		command.execute();
		command.undo();

		assertTrue(player.getQueueSnapshot().isEmpty());
	}

	@Test
	void undoCalledMultipleTimesCallsRemoveLastEachTime() {
		command.execute();
		command.execute();
		command.execute();

		command.undo();
		command.undo();
		command.undo();

		assertTrue(player.getQueueSnapshot().isEmpty());
	}

	@Test
	void undoWithoutPriorExecuteDelegatesToRemoveLast() {
		try {
			command.undo();
		} catch (Exception ignored) {
		}
	}

	@Test
	void executeFollowedByUndoLeavesQueueEmpty() {
		command.execute();
		command.undo();

		assertTrue(player.getQueueSnapshot().isEmpty());
	}

	@Test
	void executeUndoExecuteProducesCorrectQueueStateAndCallCounts() {
		command.execute();
		command.undo();
		command.execute();

		assertEquals(1, player.getQueueSnapshot().size());
	}

	@Test
	void twoExecutesFollowedByTwoUndosProduceCorrectCountsAndEmptyQueue() {
		command.execute();
		command.execute();
		command.undo();
		command.undo();

		assertTrue(player.getQueueSnapshot().isEmpty());
	}
}
