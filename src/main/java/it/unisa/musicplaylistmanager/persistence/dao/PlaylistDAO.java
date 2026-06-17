package it.unisa.musicplaylistmanager.persistence.dao;

import it.unisa.musicplaylistmanager.exceptions.PersistenceException;
import it.unisa.musicplaylistmanager.model.entity.Playlist;

import java.util.List;
import java.util.UUID;

/**
 * DAO per la gestione della persistenza delle playlist.
 */
public interface PlaylistDAO {

	/**
	 * Salva una nuova playlist nel database.
	 *
	 * @param playlist
	 *            la playlist da salvare
	 * @throws PersistenceException
	 *             se si verifica un errore durante il salvataggio
	 */
	void save(Playlist playlist);

    /**
     * Recupera tutte le playlist presenti nel database.
     *
     * @return lista di tutte le playlist
     * @throws PersistenceException
     *             se si verifica un errore durante il recupero
     */
    List<Playlist> getPlaylists();

	/**
	 * Aggiorna una playlist esistente nel database.
	 *
	 * @param playlist
	 *            la playlist da aggiornare
	 * @throws PersistenceException
	 *             se si verifica un errore durante l'aggiornamento
	 */
	void update(Playlist playlist);

    /**
     * Elimina una playlist dal database.
     *
     * @param playlistId
     *            identificatore della playlist da eliminare
     * @throws PersistenceException
     *             se si verifica un errore durante l'eliminazione
     */
    void delete(UUID playlistId);

	/**
	 * Aggiorna il numero di riproduzioni di una playlist.
	 *
	 * @param playlistId
	 *            identificativo della playlist da aggiornare
	 * @param playCount
	 *            nuovo numero di riproduzioni della playlist
	 * @throws PersistenceException
	 *             se si verifica un errore durante l'aggiornamento nel database
	 */
	void updatePlayCount(UUID playlistId, int playCount);

	/**
	 * Aggiunge un brano a una playlist.
	 *
	 * @param playlistId
	 *            identificatore della playlist
	 * @param songId
	 *            identificatore del brano da aggiungere
	 * @throws PersistenceException
	 *             se si verifica un errore durante l'inserimento del brano nella
	 *             playlist
	 */
	void addSong(UUID playlistId, UUID songId);

	/**
	 * Rimuove un brano da una playlist.
	 *
	 * @param playlistId
	 *            identificatore della playlist
	 * @param songId
	 *            identificatore del brano da rimuovere
	 * @throws PersistenceException
	 *             se si verifica un errore durante la rimozione del brano dalla
	 *             playlist
	 */
	void removeSong(UUID playlistId, UUID songId);

	/**
	 * Sostituisce l'elenco dei brani di una playlist mantenendo l'ordine fornito.
	 *
	 * @param playlistId
	 *            identificatore della playlist
	 * @param songIds
	 *            identificatori dei brani nel nuovo ordine
	 * @throws PersistenceException
	 *             se si verifica un errore durante l'aggiornamento
	 */
	void replaceSongs(UUID playlistId, List<UUID> songIds);

	/**
	 * Recupera gli identificatori dei brani contenuti in una playlist.
	 *
	 * @param playlistId
	 *            identificatore della playlist
	 * @return lista degli ID dei brani contenuti nella playlist
	 * @throws PersistenceException
	 *             se si verifica un errore durante il recupero dei brani
	 */
	List<UUID> getSongIds(UUID playlistId);
}
