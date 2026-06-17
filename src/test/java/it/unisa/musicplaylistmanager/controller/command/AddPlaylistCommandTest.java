package it.unisa.musicplaylistmanager.controller.command;

import it.unisa.musicplaylistmanager.fake.FakePlaylistDAO;
import it.unisa.musicplaylistmanager.fake.FakeSongDAO;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.library.MusicLibrary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddPlaylistCommandTest {

    private MusicLibrary musicLibrary;
    private Playlist playlist;
    private AddPlaylistCommand command;

    @BeforeEach
    void setUp() {
        musicLibrary = new MusicLibrary(
            new FakeSongDAO(),
            new FakePlaylistDAO()
        );

        playlist = new Playlist("Rock Classics");
        command = new AddPlaylistCommand(musicLibrary, playlist);
    }

    @Test
    void constructorThrowsExceptionWhenMusicLibraryIsNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new AddPlaylistCommand(null, playlist)
        );
    }

    @Test
    void constructorAcceptsNullPlaylist() {
        assertDoesNotThrow(
            () -> new AddPlaylistCommand(musicLibrary, null)
        );
    }

    @Test
    void executeAddsPlaylistToMusicLibrary() {
        command.execute();

        assertEquals(1, musicLibrary.getAllPlaylists().size());
        assertTrue(musicLibrary.getAllPlaylists().contains(playlist));
    }

    @Test
    void executeTwiceThrowsExceptionDueToDuplicateName() {
        command.execute();

        assertThrows(
            IllegalArgumentException.class,
            command::execute
        );
    }

    @Test
    void undoWithoutExecuteDoesNothing() {
        assertDoesNotThrow(command::undo);
        assertTrue(musicLibrary.getAllPlaylists().isEmpty());
    }

    @Test
    void executeThenUndoLeavesLibraryEmpty() {
        command.execute();
        command.undo();

        assertTrue(musicLibrary.getAllPlaylists().isEmpty());
    }

    @Test
    void executeUndoExecuteWorksCorrectly() {
        command.execute();
        command.undo();
        command.execute();

        assertEquals(1, musicLibrary.getAllPlaylists().size());
        assertTrue(musicLibrary.getAllPlaylists().contains(playlist));
    }
}
