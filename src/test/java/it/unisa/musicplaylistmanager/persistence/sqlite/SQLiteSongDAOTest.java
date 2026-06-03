package it.unisa.musicplaylistmanager.persistence.sqlite;

import it.unisa.musicplaylistmanager.exceptions.PersistenceException;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Song;
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

class SQLiteSongDAOTest {

    private SongDAO songDAO;
    private final String DB_URL = "jdbc:sqlite:test.db";

    @BeforeEach
    void setUp() {
        songDAO = new SQLiteSongDAO(DB_URL);
    }

    @AfterEach
    void cleanDb() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("DELETE FROM song");

        } catch (SQLException sqle) {
            return;
        }
    }

    @Test
    void addValidSongs() {
        Song song1 = new Song("Brano 1", "Boh", Genre.ROCK, 2003, 180, "pippo.mp3");
        Song song2 = new Song("Brano 2", "Arty", Genre.ELECTRONIC, 1995, 155, "pluto.mp3");

        songDAO.save(song1);
        songDAO.save(song2);

        List<Song> songs = songDAO.getSongs();

        assertEquals(2, songs.size());

        Song loadedSong = songs.stream()
            .filter(s -> s.getId().equals(song2.getId()))
            .findFirst()
            .orElseThrow();

        assertEquals(song2.getTitle(), loadedSong.getTitle());
        assertEquals(song2.getAuthor(), loadedSong.getAuthor());
        assertEquals(song2.getGenre(), loadedSong.getGenre());
        assertEquals(song2.getYear(), loadedSong.getYear());
        assertEquals(song2.getDuration(), loadedSong.getDuration());
        assertEquals(song2.getFilePath(), loadedSong.getFilePath());
    }

    @Test
    void addNullSong() {
        Song song = null;

        assertThrows(PersistenceException.class, () -> songDAO.save(song));

        List<Song> songs = songDAO.getSongs();

        assertEquals(0, songs.size());
    }

    @Test
    void addDuplicateSongs() {
        Song song1 = new Song("Brano 1", "Boh", Genre.ROCK, 2003, 180, "pippo.mp3");

        songDAO.save(song1);

        assertThrows(PersistenceException.class, () -> songDAO.save(song1));
        assertEquals(1, songDAO.getSongs().size());
    }

    @Test
    void updatePresentSong() {
        Song song = new Song("Prova", "Boh", Genre.ROCK, 2003, 180, "pippo.mp3");

        songDAO.save(song);

        List<Song> songs = songDAO.getSongs();
        assertEquals(1, songs.size());

        song.setTitle("Test");
        song.setAuthor("A");
        song.setGenre(Genre.POP);
        song.setYear(2016);
        song.setDuration(120);
        song.setFilePath("pluto.mp3");

        songDAO.update(song);

        songs = songDAO.getSongs();

        assertEquals(1, songs.size());

        assertEquals(song.getTitle(), songs.getFirst().getTitle());
        assertEquals(song.getAuthor(), songs.getFirst().getAuthor());
        assertEquals(song.getGenre(), songs.getFirst().getGenre());
        assertEquals(song.getYear(), songs.getFirst().getYear());
        assertEquals(song.getDuration(), songs.getFirst().getDuration());
        assertEquals(song.getFilePath(), songs.getFirst().getFilePath());

        assertEquals(song.getId(), songs.getFirst().getId());
    }

    @Test
    void updateAbsentSong() {
        Song song1 = new Song("Prova", "Boh", Genre.ROCK, 2003, 180, "pippo.mp3");
        Song song2 = new Song("Test", "Arty", Genre.ELECTRONIC, 1995, 155, "pluto.mp3");

        songDAO.save(song1);

        songDAO.update(song2);

        List<Song> songs = songDAO.getSongs();

        assertEquals(1, songs.size());

        Song dbSong = songs.getFirst();

        assertEquals(song1.getId(), dbSong.getId());
        assertEquals(song1.getTitle(), dbSong.getTitle());
        assertEquals(song1.getAuthor(), dbSong.getAuthor());
        assertEquals(song1.getGenre(), dbSong.getGenre());
        assertEquals(song1.getYear(), dbSong.getYear());
        assertEquals(song1.getDuration(), dbSong.getDuration());
        assertEquals(song1.getFilePath(), dbSong.getFilePath());

        assertNotEquals(song2.getId(), dbSong.getId());
    }

    @Test
    void updateWithSameData() {
        Song song = new Song("Prova", "Boh", Genre.ROCK, 2003, 180, "pippo.mp3");

        songDAO.save(song);
        songDAO.update(song);

        List<Song> songs = songDAO.getSongs();

        assertEquals(1, songs.size());

        Song dbSong = songs.getFirst();

        assertEquals(song.getId(), dbSong.getId());
        assertEquals(song.getTitle(), dbSong.getTitle());
        assertEquals(song.getAuthor(), dbSong.getAuthor());
        assertEquals(song.getGenre(), dbSong.getGenre());
        assertEquals(song.getYear(), dbSong.getYear());
        assertEquals(song.getDuration(), dbSong.getDuration());
        assertEquals(song.getFilePath(), dbSong.getFilePath());
    }

    @Test
    void updateNullSong() {
        assertThrows(PersistenceException.class, () -> songDAO.update(null));
    }

    @Test
    void deletePresentSongs() {
        Song song1 = new Song("Brano 1", "Boh", Genre.ROCK, 2003, 180, "pippo.mp3");
        Song song2 = new Song("Brano 2", "Arty", Genre.ELECTRONIC, 1995, 155, "pluto.mp3");

        songDAO.save(song1);
        songDAO.save(song2);

        songDAO.delete(song1.getId());

        List<Song> songs = songDAO.getSongs();
        assertEquals(1, songs.size());

        assertFalse(songs.stream().anyMatch(s -> s.getId().equals(song1.getId())));

        songDAO.delete(song2.getId());

        songs = songDAO.getSongs();
        assertEquals(0, songs.size());
    }

    @Test
    void deleteNullSong() {
        assertThrows(PersistenceException.class, () -> songDAO.delete(null));
    }

    @Test
    void deleteAbsentSong() {
        Song song1 = new Song("Brano 1", "Boh", Genre.ROCK, 2003, 180, "pippo.mp3");

        songDAO.save(song1);

        songDAO.delete(UUID.randomUUID());

        List<Song> songs = songDAO.getSongs();

        assertEquals(1, songs.size());
    }

    @Test
    void songsInOrder() {
        Song song1 = new Song("Prova", "Boh", Genre.ROCK, 2003, 180, "pippo.mp3");
        Song song2 = new Song("Test", "Arty", Genre.ELECTRONIC, 1995, 155, "pluto.mp3");
        Song song3 = new Song("Test", "Ciao", Genre.ELECTRONIC, 1995, 155, "pluto.mp3");

        songDAO.save(song1);
        songDAO.save(song2);
        songDAO.save(song3);

        List<Song> songs = songDAO.getSongs();

        List<String> titles = songs.stream()
            .map(Song::getTitle)
            .toList();

        assertEquals(List.of("Prova", "Test", "Test"), titles);

        List<String> authors = songs.stream()
            .map(Song::getAuthor)
            .toList();

        assertEquals(List.of("Boh", "Arty", "Ciao"), authors);
    }

    @Test
    void getSongsFromEmptyDatabase() {
        List<Song> songs = songDAO.getSongs();

        assertTrue(songs.isEmpty());
    }
}
