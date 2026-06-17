package it.unisa.musicplaylistmanager.persistence.sqlite;

import it.unisa.musicplaylistmanager.exceptions.PersistenceException;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.persistence.dao.PlaylistDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLitePlaylistDAO extends SQLiteDAO implements PlaylistDAO {

	public SQLitePlaylistDAO(String DB_URL) {
		super(DB_URL);
	}

	@Override
	public void save(Playlist playlist) {
		String query = "INSERT INTO playlist (id, name) VALUES (?, ?)";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, playlist.getId().toString());
			stmt.setString(2, playlist.getName());

			stmt.executeUpdate();
		} catch (SQLException | NullPointerException e) {
			throw new PersistenceException("Si è verificato un errore durante il salvataggio della playlist!");
		}
	}

	@Override
	public void update(Playlist playlist) {
		String query = "UPDATE playlist SET name = ? WHERE id = ?";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, playlist.getName());
			stmt.setString(2, playlist.getId().toString());

			stmt.executeUpdate();
		} catch (SQLException | NullPointerException e) {
			throw new PersistenceException("Si è verificato un errore durante la modifica della playlist!");
		}
	}

	@Override
	public void updatePlayCount(UUID playlistId, int playCount) {
		String query = "UPDATE playlist SET play_count = ? WHERE id = ?";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setInt(1, playCount);
			stmt.setString(2, playlistId.toString());
			stmt.executeUpdate();

		} catch (SQLException | NullPointerException e) {
			throw new PersistenceException(
					"Si è verificato un errore durante l'aggiornamento del play count della playlist!");
		}
	}

	@Override
	public void delete(UUID playlistId) {
		String query = "DELETE FROM playlist WHERE id = ?";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, playlistId.toString());

			stmt.executeUpdate();
		} catch (SQLException | NullPointerException e) {
			throw new PersistenceException("Si è verificato un errore durante l'eliminazione della playlist!");
		}
	}

	@Override
	public List<Playlist> getPlaylists() {
		List<Playlist> playlists = new ArrayList<>();
		String query = "SELECT * FROM playlist ORDER BY name ASC";

		try (Connection conn = getConnection();
				PreparedStatement stmt = conn.prepareStatement(query);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				UUID id = UUID.fromString(rs.getString("id"));
				String name = rs.getString("name");
				int playCount = rs.getInt("play_count");
				playlists.add(new Playlist(id, name, playCount));
			}
		} catch (SQLException | NullPointerException e) {
			throw new PersistenceException("Si è verificato un errore durante il caricamento delle playlist!");
		}

		return playlists;
	}

	@Override
	public void addSong(UUID playlistId, UUID songId) {
		String query = "INSERT INTO playlist_song (playlist_id, song_id) VALUES (?, ?)";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, playlistId.toString());
			stmt.setString(2, songId.toString());

			stmt.executeUpdate();
		} catch (SQLException | NullPointerException e) {
			throw new PersistenceException("Si è verificato un errore durante l'aggiunta del brano alla playlist!");
		}
	}

	@Override
	public void removeSong(UUID playlistId, UUID songId) {
		String query = "DELETE FROM playlist_song WHERE playlist_id = ? AND song_id = ?";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, playlistId.toString());
			stmt.setString(2, songId.toString());

			stmt.executeUpdate();
		} catch (SQLException | NullPointerException e) {
			throw new PersistenceException("Si è verificato un errore durante la rimozione del brano dalla playlist!");
		}
	}

	@Override
	public void replaceSongs(UUID playlistId, List<UUID> songIds) {
		String deleteQuery = "DELETE FROM playlist_song WHERE playlist_id = ?";
		String insertQuery = "INSERT INTO playlist_song (playlist_id, song_id) VALUES (?, ?)";

		try (Connection conn = getConnection()) {
			conn.setAutoCommit(false);

			try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
					PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

				deleteStmt.setString(1, playlistId.toString());
				deleteStmt.executeUpdate();

				for (UUID songId : songIds) {
					insertStmt.setString(1, playlistId.toString());
					insertStmt.setString(2, songId.toString());
					insertStmt.addBatch();
				}

				insertStmt.executeBatch();
				conn.commit();
			} catch (SQLException | NullPointerException e) {
				conn.rollback();
				throw e;
			}
		} catch (SQLException | NullPointerException e) {
			throw new PersistenceException("Si è verificato un errore durante il riordinamento della playlist!");
		}
	}

	@Override
	public List<UUID> getSongIds(UUID playlistId) {
		List<UUID> songIds = new ArrayList<>();
		String query = "SELECT song_id FROM playlist_song WHERE playlist_id = ? ORDER BY rowid";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, playlistId.toString());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				UUID id = UUID.fromString(rs.getString("song_id"));
				songIds.add(id);
			}
		} catch (SQLException | NullPointerException e) {
			throw new PersistenceException(
					"Si è verificato un errore durante il caricamento dei brani delle playlist!");
		}

		return songIds;
	}
}
