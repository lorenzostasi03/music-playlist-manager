package it.unisa.musicplaylistmanager.persistence.dao;

import it.unisa.musicplaylistmanager.exceptions.PersistenceException;
import it.unisa.musicplaylistmanager.model.entity.Song;

import java.util.List;
import java.util.UUID;

/**
 * DAO per la gestione della persistenza dei brani musicali.
 */
public interface SongDAO {

	/**
	 * Salva un nuovo brano nel database.
	 *
	 * @param song
	 *            il brano da salvare
	 * @throws PersistenceException
	 *             se si verifica un errore durante il salvataggio
	 */
	void save(Song song);

	/**
	 * Aggiorna un brano esistente nel database.
	 *
	 * @param song
	 *            il brano da aggiornare
	 * @throws PersistenceException
	 *             se si verifica un errore durante l'aggiornamento
	 */
	void update(Song song);

	/**
	 * Elimina un brano dal database.
	 *
	 * @param songId
	 *            identificatore del brano da eliminare
	 * @throws PersistenceException
	 *             se si verifica un errore durante l'eliminazione
	 */
	void delete(UUID songId);

	/**
	 * Recupera tutti i brani presenti nel database.
	 *
	 * @return lista di tutti i brani
	 * @throws PersistenceException
	 *             se si verifica un errore durante il recupero dei brani
	 */
	List<Song> getSongs();
}
