package it.unisa.musicplaylistmanager.persistence.sqlite;

import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.persistence.dao.PlaylistDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLitePlaylistDAO extends SQLiteDAO implements PlaylistDAO {
    @Override
    public void save(Playlist playlist) {
        String query = "INSERT INTO playlist (id, name) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, playlist.getId().toString());
            stmt.setString(2, playlist.getName());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Playlist playlist) {
        String query = "UPDATE playlist SET name = ?, play_count = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, playlist.getName());
            stmt.setInt(2, playlist.getPlayCount());
            stmt.setString(3, playlist.getId().toString());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(UUID playlistId) {
        String query =  "DELETE FROM playlist WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, playlistId.toString());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Playlist> getPlaylists() {
        List<Playlist> playlists = new ArrayList<>();
        String query = "SELECT * FROM playlist";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("id"));
                String name = rs.getString("name");
                int playCount =  rs.getInt("play_count");
                playlists.add(new Playlist(id, name, playCount));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return playlists;
    }

    @Override
    public void addSong(UUID playlistId, UUID songId) {
        String query = "INSERT INTO playlist_song (playlist_id, song_id) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, playlistId.toString());
            stmt.setString(2, songId.toString());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeSong(UUID playlistId, UUID songId) {
        String query = "DELETE FROM playlist_song WHERE playlist_id = ? AND song_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, playlistId.toString());
            stmt.setString(2, songId.toString());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UUID> getSongIds(UUID playlistId) {
        List<UUID> songIds = new ArrayList<>();
        String query = "SELECT song_id FROM playlist_song WHERE playlist_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("id"));
                songIds.add(id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return  songIds;
    }
}
