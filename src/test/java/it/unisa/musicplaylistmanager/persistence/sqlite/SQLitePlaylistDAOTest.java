package it.unisa.musicplaylistmanager.persistence.sqlite;

import it.unisa.musicplaylistmanager.exceptions.PersistenceException;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.persistence.dao.PlaylistDAO;
import it.unisa.musicplaylistmanager.persistence.dao.SongDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SQLitePlaylistDAOTest {
    private PlaylistDAO playlistDAO;
    private SongDAO songDAO;
    private final String DB_URL = "jdbc:sqlite:test.db";

    @BeforeEach
    void setUp() {
        playlistDAO = new SQLitePlaylistDAO(DB_URL);
        songDAO = new SQLiteSongDAO(DB_URL);
    }

    @AfterEach
    void cleanDb() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();) {

            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.executeUpdate("DELETE FROM playlist_song");
            stmt.executeUpdate("DELETE FROM playlist");
            stmt.executeUpdate("DELETE FROM song");

        } catch (SQLException sqle) {
            return;
        }
    }

    @Test
    void addValidPlaylists() {
        Playlist playlist1 = new Playlist("A");
        Playlist playlist2 = new Playlist("B");

        playlistDAO.save(playlist1);
        playlistDAO.save(playlist2);

        List<Playlist>playlists = playlistDAO.getPlaylists();

        assertEquals(2, playlists.size());

        assertEquals(playlist1.getId(), playlists.get(0).getId());
        assertEquals(playlist2.getId(), playlists.get(1).getId());

        assertEquals(playlist1.getName(), playlists.get(0).getName());
        assertEquals(playlist2.getName(), playlists.get(1).getName());
    }

    @Test
    void addNullPlaylist() {
        assertThrows(PersistenceException.class, () -> playlistDAO.save(null));
    }

    @Test
    void addDuplicatedPlaylists() {
        Playlist playlist1 = new Playlist("A");
        Playlist playlist2 = new Playlist("A");

        playlistDAO.save(playlist1);

        // Aggiunta della stessa playlist
        assertThrows(PersistenceException.class, () -> playlistDAO.save(playlist1));

        List<Playlist> playlists = playlistDAO.getPlaylists();

        assertEquals(1, playlists.size());

        // Aggiunta playlist con ID diverso ma stesso nome
        assertThrows(PersistenceException.class, () -> playlistDAO.save(playlist2));

        playlists = playlistDAO.getPlaylists();

        assertEquals(1, playlists.size());
    }

    @Test
    void updateValidPlaylists() {
        Playlist playlist1 = new Playlist("A");
        Playlist playlist2 = new Playlist("B");

        playlistDAO.save(playlist1);
        playlistDAO.save(playlist2);

        List<Playlist>playlists = playlistDAO.getPlaylists();

        assertEquals(2, playlists.size());

        playlist2.setName("C");

        playlistDAO.update(playlist2);

        playlists = playlistDAO.getPlaylists();

        assertEquals(2, playlists.size());

        assertEquals(playlist1.getId(), playlists.get(0).getId());
        assertEquals(playlist1.getName(), playlists.get(0).getName());
        assertEquals(playlist2.getId(), playlists.get(1).getId());
        assertEquals(playlist2.getName(), playlists.get(1).getName());
    }

    @Test
    void updateAbsentPlaylist() {
        Playlist playlist1 = new Playlist("A");

        playlistDAO.update(playlist1);

        List<Playlist>playlists = playlistDAO.getPlaylists();

        assertEquals(0, playlists.size());
    }

    @Test
    void updateNullPlaylist() {
        assertThrows(PersistenceException.class, () -> playlistDAO.update(null));
    }

    @Test
    void updateWithSameName() {
        Playlist playlist1 = new Playlist("A");
        Playlist playlist2 = new Playlist("B");

        playlistDAO.save(playlist1);
        playlistDAO.save(playlist2);

        List<Playlist> playlists = playlistDAO.getPlaylists();

        assertEquals(2, playlists.size());

        String oldName = playlist2.getName();
        playlist2.setName(playlist1.getName());
        assertThrows(PersistenceException.class, () -> playlistDAO.update(playlist2));

        playlists = playlistDAO.getPlaylists();

        assertEquals(2, playlists.size());
        assertEquals(playlist1.getName(), playlists.get(0).getName());
        assertEquals(oldName, playlists.get(1).getName());
    }

    @Test
    void deleteValidPlaylists() {
        Playlist playlist1 = new Playlist("A");
        Playlist playlist2 = new Playlist("B");

        playlistDAO.save(playlist1);
        playlistDAO.save(playlist2);

        List<Playlist> playlists = playlistDAO.getPlaylists();

        assertEquals(2, playlists.size());

        playlistDAO.delete(playlist2.getId());
        playlists = playlistDAO.getPlaylists();
        assertEquals(1, playlists.size());

        playlistDAO.delete(playlist1.getId());
        playlists = playlistDAO.getPlaylists();
        assertEquals(0, playlists.size());
    }

    @Test
    void deleteNullPlaylist() {
        assertThrows(PersistenceException.class, () -> playlistDAO.delete(null));
    }

    @Test
    void deleteAbsentPlaylist() {
        Playlist playlist1 = new Playlist("A");

        playlistDAO.delete(playlist1.getId());

        List<Playlist> playlists = playlistDAO.getPlaylists();

        assertEquals(0, playlists.size());
    }

    @Test
    void addValidSongs() {
        Playlist playlist = new Playlist("A");
        Song song1 = new Song("Prova", "Boh", Genre.ROCK, 2003, 180, "pippo.mp3");
        Song song2 = new Song("Test", "Arty", Genre.ELECTRONIC, 1995, 155, "pluto.mp3");

        playlistDAO.save(playlist);
        songDAO.save(song1);
        songDAO.save(song2);

        playlistDAO.addSong(playlist.getId(), song1.getId());
        playlistDAO.addSong(playlist.getId(), song2.getId());

        List<UUID> songIds = playlistDAO.getSongIds(playlist.getId());

        assertAll(
            () -> assertEquals(2, songIds.size()),
            () -> assertTrue(songIds.contains(song1.getId())),
            () -> assertTrue(songIds.contains(song2.getId()))
        );
    }

    @Test
    void addSongToNullPlaylist() {
        Song song = new Song("Prova", "Boh", Genre.ROCK, 2003, 180, "pippo.mp3");

        songDAO.save(song);

        assertThrows(
            PersistenceException.class,
            () -> playlistDAO.addSong(null, song.getId())
        );
    }

    @Test
    void addNullSongToPlaylist() {
        Playlist playlist = new Playlist("A");

        playlistDAO.save(playlist);

        assertThrows(
            PersistenceException.class,
            () -> playlistDAO.addSong(playlist.getId(), null)
        );
    }

    @Test
    void addSongToAbsentPlaylist() {
        Song song = new Song("Prova", "Boh", Genre.ROCK, 2003, 180, "pippo.mp3");

        songDAO.save(song);

        assertThrows(
            PersistenceException.class,
            () -> playlistDAO.addSong(UUID.randomUUID(), song.getId())
        );
    }

    @Test
    void addAbsentSongToPlaylist() {
        Playlist playlist = new Playlist("A");

        playlistDAO.save(playlist);

        assertThrows(
            PersistenceException.class,
            () -> playlistDAO.addSong(playlist.getId(), UUID.randomUUID())
        );
    }

    @Test
    void addDuplicatedSong() {
        Playlist playlist = new Playlist("A");
        Song song = new Song("Prova", "Boh", Genre.ROCK, 2003, 180, "pippo.mp3");

        playlistDAO.save(playlist);
        songDAO.save(song);

        playlistDAO.addSong(playlist.getId(), song.getId());

        assertThrows(
            PersistenceException.class,
            () -> playlistDAO.addSong(playlist.getId(), song.getId())
        );

        assertEquals(1,
            playlistDAO.getSongIds(playlist.getId()).size());
    }

    @Test
    void deletePlaylistWithSongs() {
        Playlist playlist = new Playlist("A");
        Song song = new Song("Prova", "Boh", Genre.ROCK, 2003, 180, "pippo.mp3");

        playlistDAO.save(playlist);
        songDAO.save(song);

        playlistDAO.addSong(playlist.getId(), song.getId());

        playlistDAO.delete(playlist.getId());

        List<UUID> songIds = playlistDAO.getSongIds(playlist.getId());

        assertTrue(songIds.isEmpty());
    }

    @Test
    void deleteSongRemovesAssociations() {
        Playlist playlist = new Playlist("A");
        Song song = new Song("Prova", "Boh", Genre.ROCK, 2003, 180, "pippo.mp3");

        playlistDAO.save(playlist);
        songDAO.save(song);

        playlistDAO.addSong(playlist.getId(), song.getId());

        songDAO.delete(song.getId());

        assertTrue(
            playlistDAO.getSongIds(playlist.getId()).isEmpty()
        );
    }

    @Test
    void getSongIdsFromEmptyPlaylist() {
        Playlist playlist = new Playlist("A");

        playlistDAO.save(playlist);

        List<UUID> songIds = playlistDAO.getSongIds(playlist.getId());

        assertTrue(songIds.isEmpty());
    }

    @Test
    void getSongIdsFromAbsentPlaylist() {
        List<UUID> songIds =
            playlistDAO.getSongIds(UUID.randomUUID());

        assertTrue(songIds.isEmpty());
    }

    @Test
    void getSongIdsFromNullPlaylist() {
        assertThrows(
            PersistenceException.class,
            () -> playlistDAO.getSongIds(null)
        );
    }
}
