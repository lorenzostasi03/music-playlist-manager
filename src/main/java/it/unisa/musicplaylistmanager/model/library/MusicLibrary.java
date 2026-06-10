package it.unisa.musicplaylistmanager.model.library;

import it.unisa.musicplaylistmanager.exceptions.DuplicatedPlaylistException;
import it.unisa.musicplaylistmanager.exceptions.DuplicatedSongException;
import it.unisa.musicplaylistmanager.exceptions.PersistenceException;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.persistence.dao.PlaylistDAO;
import it.unisa.musicplaylistmanager.persistence.dao.SongDAO;

import java.util.List;
import java.util.UUID;

/**
 * Facciata che espone un'interfaccia unificata per tutte le operazioni sul
 * catalogo musicale e sulle playlist.
 */
public class MusicLibrary {

	private final SongCatalog songCatalog;
	private final PlaylistCatalog playlistCatalog;
	private final SongDAO songDAO;
	private final PlaylistDAO playlistDAO;

	/**
	 * Crea una nuova libreria musicale con catalogo e collezione playlist vuoti.
	 */
	public MusicLibrary(SongDAO songDAO, PlaylistDAO playlistDAO) {
		this.songCatalog = new SongCatalog();
		this.playlistCatalog = new PlaylistCatalog();
		this.songDAO = songDAO;
		this.playlistDAO = playlistDAO;
	}

	/**
	 * Carica i brani e le playlist salvate precedentemente e inizializza
	 * songCatalog e playlistCatalog.
	 *
	 * @throws PersistenceException
	 *             se si verifica un errore durante il caricamento dal database
	 */
	public void init() {
		List<Song> songs = songDAO.getSongs();
		List<Playlist> playlists = playlistDAO.getPlaylists();

		songs.forEach(songCatalog::addSong);

		for (Playlist playlist : playlists) {
			List<UUID> songUUIDs = playlistDAO.getSongIds(playlist.getId());

			for (UUID songId : songUUIDs) {
				playlist.addSong(songCatalog.getSongById(songId));
			}

			playlistCatalog.addPlaylist(playlist);
		}
	}

	/**
	 * Aggiunge una traccia al catalogo globale.
	 *
	 * @param song
	 *            traccia da aggiungere
	 * @throws IllegalArgumentException
	 *             se la traccia non è valida
	 * @throws DuplicatedSongException
	 *             se la traccia è già presente nel catalogo
	 * @throws PersistenceException
	 *             se si verifica un errore durante il salvataggio nel database
	 */
	public void addSongToCatalog(Song song) {
		songCatalog.addSong(song);
		songDAO.save(song);
	}

	/**
	 * Rimuove definitivamente una traccia dal catalogo e da tutte le playlist.
	 *
	 * @param song
	 *            traccia da rimuovere
	 * @throws IllegalArgumentException
	 *             se la traccia è null o non presente nel catalogo
	 * @throws PersistenceException
	 *             se si verifica un errore durante la rimozione dal database
	 */
	public void removeSongFromCatalog(Song song) {
		if (!songCatalog.contains(song))
			return;

		List<Playlist> playlistsWithSong = playlistCatalog.getPlaylistsContaining(song);

		for (Playlist playlist : playlistsWithSong) {
			playlist.removeSong(song);
			playlistDAO.removeSong(playlist.getId(), song.getId());
		}

		songCatalog.removeSong(song);
		songDAO.delete(song.getId());
	}

	/**
	 * Aggiorna i metadati del brano all'interno del database.
	 *
	 * @param song
	 *            brano da aggiornare.
	 * @throws PersistenceException
	 *             in caso di errore durante l'operazione di persistenza.
	 */
	public void updateSong(Song song) {
		songDAO.update(song);
	}

    /**
     * Aggiorna il numero di riproduzioni di un brano.
     *
     * @param song
     *            brano di cui aggiornare il play count
     * @throws PersistenceException
     *             se si verifica un errore durante l'aggiornamento nel database
     */
    public void updateSongPlayCount(Song song) {
        songDAO.updatePlayCount(song.getId(), song.getPlayCount());
    }

	/**
	 * Cerca tracce nel catalogo per titolo e artista.
	 *
	 * @param query
	 *            testo da cercare
	 * @return lista delle tracce che corrispondono alla ricerca
	 */
	public List<Song> searchSong(String query) {
		return songCatalog.searchSong(query);
	}

	/**
	 * Verifica se una traccia è presente nel catalogo.
	 *
	 * @param song
	 *            traccia da verificare
	 * @return true se la traccia è presente
	 */
	public boolean catalogContains(Song song) {
		return songCatalog.contains(song);
	}

	/**
	 * Restituisce tutte le tracce del catalogo.
	 *
	 * @return lista non modificabile di tutte le tracce
	 */
	public List<Song> getAllSongs() {
		return songCatalog.getAllSongs();
	}

	/**
	 * Crea e aggiunge una nuova playlist alla collezione.
	 *
	 * @param playlist
	 *            playlist da aggiungere
	 * @throws IllegalArgumentException
	 *             se la playlist è null
	 * @throws DuplicatedPlaylistException
	 *             se esiste già una playlist con lo stesso nome
	 * @throws PersistenceException
	 *             se si verifica un errore durante il salvataggio nel database
	 */
	public void addPlaylist(Playlist playlist) {
		playlistCatalog.addPlaylist(playlist);
		playlistDAO.save(playlist);
	}

	/**
	 * Rimuove una playlist dalla collezione.
	 *
	 * @param playlist
	 *            playlist da rimuovere
	 * @throws IllegalArgumentException
	 *             se la playlist è null o non presente
	 * @throws PersistenceException
	 *             se si verifica un errore durante la cancellazione
	 */
	public void removePlaylist(Playlist playlist) {
		playlistCatalog.removePlaylist(playlist);
		playlistDAO.delete(playlist.getId());
	}

	/**
	 * Rinomina una playlist esistente.
	 *
	 * @param playlist
	 *            playlist da rinominare
	 * @param newName
	 *            nuovo nome della playlist
	 * @throws IllegalArgumentException
	 *             se playlist o nome non sono validi
	 * @throws DuplicatedPlaylistException
	 *             se esiste già una playlist con lo stesso nome
	 * @throws PersistenceException
	 *             se si verifica un errore durante l'aggiornamento nel database
	 */
	public void renamePlaylist(Playlist playlist, String newName) {
		if (playlist == null) {
			throw new IllegalArgumentException("La playlist non può essere null.");
		}
		if (newName == null || newName.trim().isEmpty()) {
			throw new IllegalArgumentException("Il nome della playlist non può essere vuoto.");
		}
		if (playlistCatalog.isNameTakenByOther(playlist.getName(), newName)) {
			throw new IllegalArgumentException("Esiste già una playlist con il nome '" + newName + "'.");
		}

		playlist.setName(newName);
		playlistDAO.update(playlist);
	}

    /**
     * Aggiorna il numero di riproduzioni di una playlist.
     *
     * @param playlist
     *            playlist di cui aggiornare il play count
     * @throws PersistenceException
     *             se si verifica un errore durante l'aggiornamento nel database
     */
    void updatePlaylistPlayCount(Playlist playlist) {
        playlistDAO.updatePlayCount(playlist.getId(), playlist.getPlayCount());
    }

	/**
	 * Aggiunge una traccia del catalogo a una playlist.
	 *
	 * @param song
	 *            traccia da aggiungere
	 * @param playlist
	 *            playlist destinazione
	 * @throws IllegalArgumentException
	 *             se il brano non è nel catalogo
	 * @throws PersistenceException
	 *             se si verifica un errore durante l'operazione sul database
	 */
	public void addSongToPlaylist(Song song, Playlist playlist) {
		if (!songCatalog.contains(song)) {
			throw new IllegalArgumentException("La traccia '" + song.getTitle() + "' non è presente nel catalogo.");
		}

		playlist.addSong(song);
		playlistDAO.addSong(playlist.getId(), song.getId());
	}

	/**
	 * Rimuove una traccia da una playlist.
	 *
	 * @param song
	 *            traccia da rimuovere
	 * @param playlist
	 *            playlist da cui rimuovere la traccia
	 * @throws IllegalArgumentException
	 *             se i parametri non sono validi
	 * @throws PersistenceException
	 *             se si verifica un errore durante la rimozione
	 */
	public void removeSongFromPlaylist(Song song, Playlist playlist) {
		playlist.removeSong(song);
		playlistDAO.removeSong(playlist.getId(), song.getId());
	}

	/**
	 * Restituisce tutte le playlist della collezione.
	 *
	 * @return lista non modificabile di tutte le playlist
	 */
	public List<Playlist> getAllPlaylists() {
		return playlistCatalog.getAllPlaylists();
	}
}
