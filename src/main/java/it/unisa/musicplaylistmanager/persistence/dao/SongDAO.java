package it.unisa.musicplaylistmanager.persistence.dao;

import it.unisa.musicplaylistmanager.model.entity.Song;

import java.util.List;
import java.util.UUID;

public interface SongDAO {

    /**
     * Salva un nuovo brano nel database.
     * @param song il brano da salvare.
     */
    void save(Song song);

    /**
     * Aggiorna un brano esistente nel database.
     * @param song il brano da aggiornare.
     */
    void update(Song song);

    /**
     * Elimina un brano dal database.
     * @param songId identificatore del brano da eliminare.
     */
    void delete(UUID songId);

    /**
     * Recupera tutti i brani presenti nel database.
     * @return lista di tutti i brani.
     */
    List<Song> getSongs();
}
