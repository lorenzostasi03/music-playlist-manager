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
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.executeUpdate("DELETE FROM playlist_song");
            stmt.executeUpdate("DELETE FROM playlist");
            stmt.executeUpdate("DELETE FROM song");

        } catch (SQLException ignored) {}
    }

    @Test
    void addValidPlaylists() {
        Playlist playlist1 = new Playlist("A");
        Playlist playlist2 = new Playlist("B");

        playlistDAO.save(playlist1);
        playlistDAO.save(playlist2);

        List<Playlist> playlists = playlistDAO.getPlaylists();

        assertEquals(2, playlists.size());
    }

    @Test
    void addNullPlaylist() {
        assertThrows(PersistenceException.class, () -> playlistDAO.save(null));
    }

    @Test
    void addDuplicatedPlaylists() {
        Playlist playlist = new Playlist("A");

        playlistDAO.save(playlist);

        assertThrows(PersistenceException.class, () -> playlistDAO.save(playlist));
    }

    @Test
    void updateNullPlaylist() {
        assertThrows(PersistenceException.class, () -> playlistDAO.update(null));
    }

    @Test
    void deleteNullPlaylist() {
        assertThrows(PersistenceException.class, () -> playlistDAO.delete(null));
    }

    @Test
    void addValidSongs() {
        Playlist playlist = new Playlist("A");
        Song song = new Song("Prova", "Boh", Genre.ROCK, 2003, 180, "pippo.mp3");

        playlistDAO.save(playlist);
        songDAO.save(song);

        playlistDAO.addSong(playlist.getId(), song.getId());

        List<UUID> songIds = playlistDAO.getSongIds(playlist.getId());

        assertEquals(1, songIds.size());
    }

    @Test
    void addSongToNullPlaylist() {
        Song song = new Song("Prova", "Boh", Genre.ROCK, 2003, 180, "pippo.mp3");

        songDAO.save(song);

        assertThrows(PersistenceException.class,
            () -> playlistDAO.addSong(null, song.getId()));
    }

    @Test
    void addNullSongToPlaylist() {
        Playlist playlist = new Playlist("A");

        playlistDAO.save(playlist);

        assertThrows(PersistenceException.class,
            () -> playlistDAO.addSong(playlist.getId(), null));
    }

    @Test
    void getSongIdsFromNullPlaylist() {
        assertThrows(PersistenceException.class,
            () -> playlistDAO.getSongIds(null));
    }
}
