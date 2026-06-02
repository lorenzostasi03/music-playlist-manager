package it.unisa.musicplaylistmanager.persistence.dao;

import it.unisa.musicplaylistmanager.model.entity.Playlist;

import java.util.List;
import java.util.UUID;

public interface PlaylistDAO {

    /**
     * Salva una nuova playlist nel database.
     * @param playlist la playlist da salvare.
     */
    void save(Playlist playlist);

    /**
     * Aggiorna una playlist esistente nel database.
     * @param playlist la playlist da aggiornare.
     */
    void update(Playlist playlist);

    /**
     * Elimina una playlist dal database.
     * @param playlistId identificatore della playlist da eliminare.
     */
    void delete(UUID playlistId);

    /**
     * Recupera tutte le playlist presenti nel database.
     * @return lista di tutte le playlist.
     */
    List<Playlist> getPlaylists();

    /**
     * Aggiunge un brano a una playlist.
     * @param playlistId identificatore della playlist.
     * @param songId identificatore del brano da aggiungere.
     */
    void addSong(UUID playlistId, UUID songId);

    /**
     * Rimuove un brano da una playlist.
     * @param playlistId identificatore della playlist.
     * @param songId identificatore del brano da rimuovere.
     */
    void removeSong(UUID playlistId, UUID songId);

    /**
     * Recupera gli identificatori dei brani contenuti in una playlist.
     * @param playlistId identificatore della playlist.
     * @return lista degli id dei brani contenuti nella playlist.
     */
    List<UUID> getSongIds(UUID playlistId);
}
