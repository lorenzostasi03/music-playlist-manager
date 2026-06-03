package it.unisa.musicplaylistmanager.persistence.sqlite;

import it.unisa.musicplaylistmanager.exceptions.PersistenceException;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.persistence.dao.SongDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLiteSongDAO extends SQLiteDAO implements SongDAO {
    @Override
    public void save(Song song) {
        String query = """
                INSERT INTO song (id, title, author, genre, year, duration, file_path)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, song.getId().toString());
            stmt.setString(2, song.getTitle());
            stmt.setString(3, song.getAuthor());
            stmt.setString(4, song.getGenre().toString());
            stmt.setInt(5, song.getYear());
            stmt.setInt(6, song.getDuration());
            stmt.setString(7, song.getFilePath());

            stmt.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            throw new PersistenceException("Si è verificato un errore durante il salvataggio del brano!");
        }
    }

    @Override
    public void update(Song song) {
        String query = """
                UPDATE song
                SET title = ?, author = ?, genre = ?, year = ?, duration = ?, file_path = ?, play_count = ?
                WHERE id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, song.getTitle());
            stmt.setString(2, song.getAuthor());
            stmt.setString(3, song.getGenre().toString());
            stmt.setInt(4, song.getYear());
            stmt.setInt(5, song.getDuration());
            stmt.setString(6, song.getFilePath());
            stmt.setInt(7, song.getPlayCount());
            stmt.setString(8, song.getId().toString());

            stmt.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            throw new PersistenceException("Si è verificato un errore durante la modifica del brano!");
        }
    }

    @Override
    public void delete(UUID songId) {
        String query = "DELETE FROM song WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, songId.toString());

            stmt.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            throw new PersistenceException("Si è verificato un errore durante l'eliminazione del brano!");
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
                int playCount =  rs.getInt("play_count");
                songs.add(new Song(id, title, author, Genre.valueOf(genre), year, duration, filePath, playCount));
            }
        } catch (SQLException | NullPointerException e) {
            throw new PersistenceException("Si è verificato un errore durante il caricamento dei brani!");
        }

        return songs;
    }
}
