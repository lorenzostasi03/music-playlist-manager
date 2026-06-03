package it.unisa.musicplaylistmanager.fake;

import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.persistence.dao.PlaylistDAO;

import java.util.*;

public class FakePlaylistDAO implements PlaylistDAO {

    private final List<Playlist> playlists = new ArrayList<>();
    private final Map<UUID, Set<UUID>> playlistSongs = new HashMap<>();

    @Override
    public void save(Playlist playlist) {
        playlists.add(playlist);
        playlistSongs.putIfAbsent(playlist.getId(), new HashSet<>());
    }

    @Override
    public void update(Playlist playlist) {
        delete(playlist.getId());
        save(playlist);
    }

    @Override
    public void delete(UUID playlistId) {
        playlists.removeIf(p -> p.getId().equals(playlistId));
        playlistSongs.remove(playlistId);
    }

    @Override
    public List<Playlist> getPlaylists() {
        return new ArrayList<>(playlists);
    }

    @Override
    public void addSong(UUID playlistId, UUID songId) {
        playlistSongs.computeIfAbsent(playlistId, k -> new HashSet<>()).add(songId);
    }

    @Override
    public void removeSong(UUID playlistId, UUID songId) {
        Set<UUID> songs = playlistSongs.get(playlistId);
        if (songs != null) {
            songs.remove(songId);
        }
    }

    @Override
    public List<UUID> getSongIds(UUID playlistId) {
        Set<UUID> songs = playlistSongs.get(playlistId);
        if (songs == null) return new ArrayList<>();
        return new ArrayList<>(songs);
    }

    public void clear() {
        playlists.clear();
        playlistSongs.clear();
    }

    public boolean contains(Playlist playlist) {
        return playlists.contains(playlist);
    }
}
