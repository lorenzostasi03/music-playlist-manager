package it.unisa.musicplaylistmanager.persistence.sqlite;

import it.unisa.musicplaylistmanager.exceptions.PersistenceException;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.entity.Tag;
import it.unisa.musicplaylistmanager.persistence.dao.SongDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLiteSongDAO extends SQLiteDAO implements SongDAO {

	public SQLiteSongDAO(String DB_URL) {
		super(DB_URL);
	}

	// CRUD Song

	@Override
	public void save(Song song) {
		String query = """
				INSERT INTO song (id, title, author, genre, year, duration, file_path)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, song.getId().toString());
			stmt.setString(2, song.getTitle());
			stmt.setString(3, song.getAuthor());
			stmt.setString(4, song.getGenre().toString());
			stmt.setInt(5, song.getYear());
			stmt.setInt(6, song.getDuration());
			stmt.setString(7, song.getFilePath());

			stmt.executeUpdate();
			saveTags(conn, song);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw new PersistenceException("Si è verificato un errore durante il salvataggio del brano!");
		}
	}

	@Override
	public List<Song> getSongs() {
		List<Song> songs = new ArrayList<>();
		String query = "SELECT * FROM song ORDER BY title ASC, author ASC";

		try (Connection conn = getConnection();
				PreparedStatement stmt = conn.prepareStatement(query);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				UUID id = UUID.fromString(rs.getString("id"));
				String title = rs.getString("title");
				String author = rs.getString("author");
				String genre = rs.getString("genre");
				int year = rs.getInt("year");
				int duration = rs.getInt("duration");
				String filePath = rs.getString("file_path");
				int playCount = rs.getInt("play_count");
				Song song = new Song(id, title, author, Genre.valueOf(genre), year, duration, filePath, playCount);
				loadTags(conn, song);
				songs.add(song);
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw new PersistenceException("Si è verificato un errore durante il caricamento dei brani!");
		}

		return songs;
	}

	@Override
	public void update(Song song) {
		String query = """
				UPDATE song
				SET title = ?, author = ?, genre = ?, year = ?, duration = ?, file_path = ?
				WHERE id = ?
				""";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, song.getTitle());
			stmt.setString(2, song.getAuthor());
			stmt.setString(3, song.getGenre().toString());
			stmt.setInt(4, song.getYear());
			stmt.setInt(5, song.getDuration());
			stmt.setString(6, song.getFilePath());
			stmt.setString(7, song.getId().toString());

			stmt.executeUpdate();
			deleteTags(conn, song.getId());
			saveTags(conn, song);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw new PersistenceException("Si è verificato un errore durante la modifica del brano!");
		}
	}

	@Override
	public void delete(UUID songId) {
		String query = "DELETE FROM song WHERE id = ?";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			deleteTags(conn, songId);
			stmt.setString(1, songId.toString());

			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw new PersistenceException("Si è verificato un errore durante l'eliminazione del brano!");
		}
	}

	// Gestione play count

	@Override
	public void updatePlayCount(UUID songId, int playCount) {
		String query = "UPDATE song SET play_count = ? WHERE id = ?";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setInt(1, playCount);
			stmt.setString(2, songId.toString());
			stmt.executeUpdate();

		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw new PersistenceException(
					"Si è verificato un errore durante l'aggiornamento del play count del brano!");
		}
	}

	// Gestione tags

	private void saveTags(Connection conn, Song song) throws SQLException {
		String query = "INSERT INTO song_tag (song_id, tag) VALUES (?, ?)";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			for (Tag tag : song.getTags()) {
				stmt.setString(1, song.getId().toString());
				stmt.setString(2, tag.name());
				stmt.addBatch();
			}
			stmt.executeBatch();
		}
	}

	private void loadTags(Connection conn, Song song) throws SQLException {
		String query = "SELECT tag FROM song_tag WHERE song_id = ?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, song.getId().toString());

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					song.addTag(Tag.valueOf(rs.getString("tag")));
				}
			}
		}
	}

	private void deleteTags(Connection conn, UUID songId) throws SQLException {
		String query = "DELETE FROM song_tag WHERE song_id = ?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, songId.toString());
			stmt.executeUpdate();
		}
	}
}
