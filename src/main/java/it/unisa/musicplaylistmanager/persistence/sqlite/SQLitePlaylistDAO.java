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

	// CRUD Playlist

	@Override
	public void save(Playlist playlist) {
		String query = "INSERT INTO playlist (id, name) VALUES (?, ?)";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, playlist.getId().toString());
			stmt.setString(2, playlist.getName());

			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw new PersistenceException("Si è verificato un errore durante il salvataggio della playlist!");
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
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw new PersistenceException("Si è verificato un errore durante il caricamento delle playlist!");
		}

		return playlists;
	}

	@Override
	public void update(Playlist playlist) {
		String query = "UPDATE playlist SET name = ? WHERE id = ?";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, playlist.getName());
			stmt.setString(2, playlist.getId().toString());

			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw new PersistenceException("Si è verificato un errore durante la modifica della playlist!");
		}
	}

	@Override
	public void delete(UUID playlistId) {
		String query = "DELETE FROM playlist WHERE id = ?";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, playlistId.toString());

			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw new PersistenceException("Si è verificato un errore durante l'eliminazione della playlist!");
		}
	}

	// Gestione play count

	@Override
	public void updatePlayCount(UUID playlistId, int playCount) {
		String query = "UPDATE playlist SET play_count = ? WHERE id = ?";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setInt(1, playCount);
			stmt.setString(2, playlistId.toString());
			stmt.executeUpdate();

		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw new PersistenceException(
					"Si è verificato un errore durante l'aggiornamento del play count della playlist!");
		}
	}

	// Relazione Playlist - Song

	@Override
	public void addSong(UUID playlistId, UUID songId) {
		String query = """
				INSERT INTO playlist_song (playlist_id, song_id, position)
				VALUES (?, ?, (
				    SELECT COALESCE(MAX(position), -1) + 1
				    FROM playlist_song
				    WHERE playlist_id = ?
				))
				""";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, playlistId.toString());
			stmt.setString(2, songId.toString());
			stmt.setString(3, playlistId.toString());
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw new PersistenceException("Si è verificato un errore durante l'aggiunta del brano alla playlist!");
		}
	}

	@Override
	public void removeSong(UUID playlistId, UUID songId) {
		String getPositionQuery = """
				SELECT position
				FROM playlist_song
				WHERE playlist_id = ? AND song_id = ?
				""";

		String deleteQuery = """
				DELETE FROM playlist_song
				WHERE playlist_id = ? AND song_id = ?
				""";

		String updatePositionsQuery = """
				UPDATE playlist_song
				SET position = position - 1
				WHERE playlist_id = ?
				  AND position > ?
				""";

		try (Connection conn = getConnection()) {

			conn.setAutoCommit(false);

			try {
				int removedPosition;

				try (PreparedStatement stmt = conn.prepareStatement(getPositionQuery)) {
					stmt.setString(1, playlistId.toString());
					stmt.setString(2, songId.toString());

					try (ResultSet rs = stmt.executeQuery()) {
						if (!rs.next()) {
							conn.rollback();
							return;
						}

						removedPosition = rs.getInt("position");
					}
				}

				try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
					stmt.setString(1, playlistId.toString());
					stmt.setString(2, songId.toString());
					stmt.executeUpdate();
				}

				try (PreparedStatement stmt = conn.prepareStatement(updatePositionsQuery)) {
					stmt.setString(1, playlistId.toString());
					stmt.setInt(2, removedPosition);
					stmt.executeUpdate();
				}

				conn.commit();

			} catch (SQLException e) {
				conn.rollback();
				throw e;
			}

		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw new PersistenceException("Si è verificato un errore durante la rimozione del brano dalla playlist!");
		}
	}

	@Override
	public void replaceSongs(UUID playlistId, List<UUID> songIds) {

		String query = "UPDATE playlist_song SET position = ? WHERE playlist_id = ? AND song_id = ?";

		Connection conn = null;

		try {
			conn = getConnection();
			conn.setAutoCommit(false);

			try (PreparedStatement stmt = conn.prepareStatement(query)) {

				for (int i = 0; i < songIds.size(); i++) {
					stmt.setInt(1, i);
					stmt.setString(2, playlistId.toString());
					stmt.setString(3, songIds.get(i).toString());
					stmt.addBatch();
				}

				stmt.executeBatch();
				conn.commit();
			}

		} catch (SQLException e) {

			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException rollbackEx) {
					e.addSuppressed(rollbackEx);
				}
			}

			System.err.println(e.getMessage());
			throw new PersistenceException("Si è verificato un errore durante il riordinamento della playlist!");

		} finally {

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	@Override
	public List<UUID> getSongIds(UUID playlistId) {
		List<UUID> songIds = new ArrayList<>();
		String query = "SELECT song_id FROM playlist_song WHERE playlist_id = ? ORDER BY position";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, playlistId.toString());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				UUID id = UUID.fromString(rs.getString("song_id"));
				songIds.add(id);
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw new PersistenceException(
					"Si è verificato un errore durante il caricamento dei brani delle playlist!");
		}

		return songIds;
	}
}
