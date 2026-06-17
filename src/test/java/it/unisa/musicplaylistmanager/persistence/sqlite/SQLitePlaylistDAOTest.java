package it.unisa.musicplaylistmanager.persistence.sqlite;

import it.unisa.musicplaylistmanager.exceptions.PersistenceException;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.persistence.dao.PlaylistDAO;
import it.unisa.musicplaylistmanager.persistence.dao.SongDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SQLitePlaylistDAOTest {

	private PlaylistDAO playlistDAO;
	private SongDAO songDAO;

	@BeforeEach
	void setUp() throws IOException {
		Files.deleteIfExists(Path.of(TestDatabaseConfig.DB_PATH));

		DatabaseInitializer.initialize(TestDatabaseConfig.DB_URL);

		playlistDAO = new SQLitePlaylistDAO(TestDatabaseConfig.DB_URL);
		songDAO = new SQLiteSongDAO(TestDatabaseConfig.DB_URL);
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
		assertThrows(NullPointerException.class, () -> playlistDAO.save(null));
	}

	@Test
	void addDuplicatedPlaylists() {
		Playlist playlist = new Playlist("A");

		playlistDAO.save(playlist);

		assertThrows(PersistenceException.class, () -> playlistDAO.save(playlist));
	}

	@Test
	void updateValidPlaylists() {
		Playlist playlist = new Playlist("A");

		playlistDAO.save(playlist);

		playlist.setName("B");
		playlistDAO.update(playlist);

		List<Playlist> playlists = playlistDAO.getPlaylists();
		assertEquals(1, playlists.size());

		assertEquals(playlist.getName(), playlists.getFirst().getName());

		int oldPlayCount = playlist.getPlayCount();
		playlist.incrementPlayCount();
		playlistDAO.updatePlayCount(playlist.getId(), playlist.getPlayCount());
		playlists = playlistDAO.getPlaylists();
		assertEquals(playlist.getPlayCount(), playlists.getFirst().getPlayCount());
		assertEquals(oldPlayCount + 1, playlists.getFirst().getPlayCount());
	}

	@Test
	void updateNullPlaylist() {
		assertThrows(NullPointerException.class, () -> playlistDAO.update(null));
		assertThrows(NullPointerException.class, () -> playlistDAO.updatePlayCount(null, 2));
	}

	@Test
	void deleteNullPlaylist() {
		assertThrows(NullPointerException.class, () -> playlistDAO.delete(null));
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
	void replaceSongs() {
		Playlist playlist = new Playlist("A");
		Song firstSong = new Song("Prima", "Boh", Genre.ROCK, 2003, 180, "prima.mp3");
		Song secondSong = new Song("Seconda", "Boh", Genre.POP, 2004, 190, "seconda.mp3");

		playlistDAO.save(playlist);
		songDAO.save(firstSong);
		songDAO.save(secondSong);
		playlistDAO.addSong(playlist.getId(), firstSong.getId());
		playlistDAO.addSong(playlist.getId(), secondSong.getId());

		playlistDAO.replaceSongs(playlist.getId(), List.of(secondSong.getId(), firstSong.getId()));

		assertEquals(List.of(secondSong.getId(), firstSong.getId()), playlistDAO.getSongIds(playlist.getId()));
	}

	@Test
	void addSongToNullPlaylist() {
		Song song = new Song("Prova", "Boh", Genre.ROCK, 2003, 180, "pippo.mp3");

		songDAO.save(song);

		assertThrows(NullPointerException.class, () -> playlistDAO.addSong(null, song.getId()));
	}

	@Test
	void addNullSongToPlaylist() {
		Playlist playlist = new Playlist("A");

		playlistDAO.save(playlist);

		assertThrows(NullPointerException.class, () -> playlistDAO.addSong(playlist.getId(), null));
	}

	@Test
	void getSongIdsFromNullPlaylist() {
		assertThrows(NullPointerException.class, () -> playlistDAO.getSongIds(null));
	}
}
