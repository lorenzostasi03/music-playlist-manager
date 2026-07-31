package it.unisa.musicplaylistmanager.persistence.sqlite;

import it.unisa.musicplaylistmanager.exceptions.PersistenceException;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.AutomaticPlaylist;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.PlaylistCriteria;
import it.unisa.musicplaylistmanager.model.entity.Tag;
import it.unisa.musicplaylistmanager.persistence.dao.PlaylistDAO;

import java.sql.*;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementazione SQLite del DAO delle playlist.
 *
 * <p>
 * Gestisce la persistenza delle playlist manuali e automatiche, dei relativi
 * criteri e delle associazioni ordinate tra playlist e tracce.
 */
public class SQLitePlaylistDAO extends SQLiteDAO implements PlaylistDAO {

	/**
	 * Contiene i dati di base letti dalla tabella {@code playlist} prima della
	 * ricostruzione del tipo concreto della playlist.
	 */
	private record PlaylistData(UUID id, String name, int playCount) {
	}

	/**
	 * Crea un DAO collegato al database indicato.
	 *
	 * @param DB_URL
	 *            URL JDBC del database SQLite
	 */
	public SQLitePlaylistDAO(String DB_URL) {
		super(DB_URL);
	}

	// CRUD Playlist

	/**
	 * Salva una nuova playlist nel database.
	 *
	 * <p>
	 * Se la playlist è automatica, salva nella stessa transazione anche i criteri
	 * che ne determinano la composizione.
	 *
	 * @param playlist
	 *            playlist da salvare
	 * @throws PersistenceException
	 *             se si verifica un errore durante il salvataggio
	 */
	@Override
	public void save(Playlist playlist) {
		String query = "INSERT INTO playlist (id, name) VALUES (?, ?)";

		try (Connection conn = getConnection()) {
			conn.setAutoCommit(false);

			try {
				try (PreparedStatement stmt = conn.prepareStatement(query)) {

					stmt.setString(1, playlist.getId().toString());

					stmt.setString(2, playlist.getName());

					stmt.executeUpdate();
				}

				if (playlist instanceof AutomaticPlaylist automaticPlaylist) {

					saveCriteria(conn, automaticPlaylist);
				}

				conn.commit();
			} catch (SQLException e) {
				conn.rollback();
				throw e;
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());

			throw new PersistenceException("Si è verificato un errore durante " + "il salvataggio della playlist!");
		}
	}

	/**
	 * Salva i criteri associati a una playlist automatica utilizzando la
	 * connessione della transazione corrente.
	 *
	 * @param conn
	 *            connessione al database
	 * @param playlist
	 *            playlist automatica di cui salvare i criteri
	 * @throws SQLException
	 *             se si verifica un errore durante l'inserimento
	 */
	private void saveCriteria(Connection conn, AutomaticPlaylist playlist) throws SQLException {

		String query = """
				INSERT INTO playlist_criterion (
				    playlist_id,
				    criterion_type,
				    criterion_value
				)
				VALUES (?, ?, ?)
				""";

		PlaylistCriteria criteria = playlist.getCriteria();

		try (PreparedStatement stmt = conn.prepareStatement(query)) {

			for (Genre genre : criteria.genres()) {
				addCriterionToBatch(stmt, playlist.getId(), "GENRE", genre.name());
			}

			for (Integer year : criteria.years()) {
				addCriterionToBatch(stmt, playlist.getId(), "YEAR", year.toString());
			}

			for (Tag tag : criteria.tags()) {
				addCriterionToBatch(stmt, playlist.getId(), "TAG", tag.name());
			}

			stmt.executeBatch();
		}
	}

	/**
	 * Aggiunge al batch un criterio da associare alla playlist.
	 *
	 * @param stmt
	 *            statement usato per l'inserimento
	 * @param playlistId
	 *            identificatore della playlist
	 * @param type
	 *            tipo del criterio
	 * @param value
	 *            valore del criterio
	 * @throws SQLException
	 *             se si verifica un errore nella configurazione dello statement
	 */
	private void addCriterionToBatch(PreparedStatement stmt, UUID playlistId, String type, String value)
			throws SQLException {

		stmt.setString(1, playlistId.toString());
		stmt.setString(2, type);
		stmt.setString(3, value);
		stmt.addBatch();
	}

	/**
	 * Carica tutte le playlist ordinate per nome.
	 *
	 * <p>
	 * Le playlist prive di criteri vengono ricostruite come {@link Playlist},
	 * mentre quelle con almeno un criterio vengono ricostruite come
	 * {@link AutomaticPlaylist}.
	 *
	 * @return lista delle playlist salvate
	 * @throws PersistenceException
	 *             se si verifica un errore durante il caricamento
	 */
	@Override
	public List<Playlist> getPlaylists() {
		List<Playlist> playlists = new ArrayList<>();
		List<PlaylistData> playlistData = new ArrayList<>();

		String query = "SELECT * FROM playlist ORDER BY name ASC";

		try (Connection conn = getConnection()) {
			try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

				while (rs.next()) {
					playlistData.add(new PlaylistData(UUID.fromString(rs.getString("id")), rs.getString("name"),
							rs.getInt("play_count")));
				}
			}

			for (PlaylistData data : playlistData) {
				PlaylistCriteria criteria = loadCriteria(conn, data.id());

				if (criteria == null) {
					playlists.add(new Playlist(data.id(), data.name(), data.playCount()));
				} else {
					playlists.add(new AutomaticPlaylist(data.id(), data.name(), data.playCount(), criteria));
				}
			}
		} catch (SQLException | IllegalArgumentException e) {
			System.err.println(e.getMessage());

			throw new PersistenceException("Si è verificato un errore durante " + "il caricamento delle playlist!");
		}

		return playlists;
	}

	/**
	 * Carica i criteri associati a una playlist.
	 *
	 * @param conn
	 *            connessione al database
	 * @param playlistId
	 *            identificatore della playlist
	 * @return criteri caricati, oppure {@code null} se la playlist è manuale
	 * @throws SQLException
	 *             se si verifica un errore durante il caricamento
	 * @throws IllegalArgumentException
	 *             se un valore persistito non può essere convertito nel tipo atteso
	 */
	private PlaylistCriteria loadCriteria(Connection conn, UUID playlistId) throws SQLException {

		String query = """
				SELECT criterion_type, criterion_value
				FROM playlist_criterion
				WHERE playlist_id = ?
				""";

		Set<Genre> genres = EnumSet.noneOf(Genre.class);

		Set<Integer> years = new HashSet<>();

		Set<Tag> tags = EnumSet.noneOf(Tag.class);

		try (PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, playlistId.toString());

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					String type = rs.getString("criterion_type");

					String value = rs.getString("criterion_value");

					switch (type) {
						case "GENRE" -> genres.add(Genre.valueOf(value));

						case "YEAR" -> years.add(Integer.valueOf(value));

						case "TAG" -> tags.add(Tag.valueOf(value));

						default -> throw new SQLException("Tipo di criterio non valido: " + type);
					}
				}
			}
		}

		if (genres.isEmpty() && years.isEmpty() && tags.isEmpty()) {

			return null;
		}

		return new PlaylistCriteria(genres, years, tags);
	}

	/**
	 * Aggiorna il nome di una playlist esistente.
	 *
	 * @param playlist
	 *            playlist da aggiornare
	 * @throws PersistenceException
	 *             se si verifica un errore durante l'aggiornamento
	 */
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

	/**
	 * Elimina una playlist dal database.
	 *
	 * <p>
	 * Le associazioni con le tracce e gli eventuali criteri vengono eliminati
	 * tramite le regole di cancellazione in cascata definite nello schema.
	 *
	 * @param playlistId
	 *            identificatore della playlist da eliminare
	 * @throws PersistenceException
	 *             se si verifica un errore durante l'eliminazione
	 */
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

	/**
	 * Aggiorna il numero di riproduzioni di una playlist.
	 *
	 * @param playlistId
	 *            identificatore della playlist
	 * @param playCount
	 *            nuovo numero di riproduzioni
	 * @throws PersistenceException
	 *             se si verifica un errore durante l'aggiornamento
	 */
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

	/**
	 * Aggiunge una traccia in fondo alla playlist.
	 *
	 * @param playlistId
	 *            identificatore della playlist
	 * @param songId
	 *            identificatore della traccia
	 * @throws PersistenceException
	 *             se si verifica un errore durante l'inserimento
	 */
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

	/**
	 * Rimuove una traccia dalla playlist e compatta le posizioni successive.
	 *
	 * @param playlistId
	 *            identificatore della playlist
	 * @param songId
	 *            identificatore della traccia
	 * @throws PersistenceException
	 *             se si verifica un errore durante la rimozione
	 */
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

	/**
	 * Aggiorna l'ordine delle tracce presenti in una playlist.
	 *
	 * @param playlistId
	 *            identificatore della playlist
	 * @param songIds
	 *            identificatori delle tracce nel nuovo ordine
	 * @throws PersistenceException
	 *             se si verifica un errore durante il riordinamento
	 */
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

	/**
	 * Carica gli identificatori delle tracce di una playlist rispettandone
	 * l'ordinamento.
	 *
	 * @param playlistId
	 *            identificatore della playlist
	 * @return identificatori delle tracce ordinati per posizione
	 * @throws PersistenceException
	 *             se si verifica un errore durante il caricamento
	 */
	@Override
	public List<UUID> getSongIds(UUID playlistId) {
		List<UUID> songIds = new ArrayList<>();
		String query = "SELECT song_id FROM playlist_song WHERE playlist_id = ? ORDER BY position";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, playlistId.toString());
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					UUID id = UUID.fromString(rs.getString("song_id"));

					songIds.add(id);
				}
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw new PersistenceException(
					"Si è verificato un errore durante il caricamento dei brani delle playlist!");
		}

		return songIds;
	}
}
