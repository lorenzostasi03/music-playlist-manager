package it.unisa.musicplaylistmanager.model.library;

import it.unisa.musicplaylistmanager.exceptions.DuplicatedPlaylistException;
import it.unisa.musicplaylistmanager.exceptions.DuplicatedSongException;
import it.unisa.musicplaylistmanager.exceptions.PersistenceException;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.entity.Tag;
import it.unisa.musicplaylistmanager.persistence.dao.PlaylistDAO;
import it.unisa.musicplaylistmanager.persistence.dao.SongDAO;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Facade che espone un'interfaccia unificata per tutte le operazioni sul
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

		init();
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

			songUUIDs.stream().map(songCatalog::getSongById).forEach(playlist::addSong);

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
	 * Filtra le tracce del catalogo globale.
	 *
	 * @param query
	 *            testo da cercare nel titolo
	 * @param genre
	 *            genere richiesto
	 * @param author
	 *            autore richiesto
	 * @param year
	 *            anno richiesto
	 * @param tag
	 *            tag richiesto
	 * @return lista delle tracce compatibili con i criteri indicati
	 */
	public List<Song> filterSongs(String query, Genre genre, String author, Integer year, Tag tag) {
		return songCatalog.filterSongs(query, genre, author, year, tag);
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
	 * Recupera i primi {@code n} brani più riprodotti del catalogo, ordinati per
	 * numero di riproduzioni in ordine decrescente.
	 *
	 * @param n
	 *            il numero massimo di brani da restituire; deve essere positivo
	 * @return lista di al più {@code n} brani ordinati per playCount decrescente;
	 *         può contenere meno di {@code n} elementi se il catalogo è più piccolo
	 */
	public List<Song> getTopSongs(int n) {
		if (n <= 0)
			return null;

		return songCatalog.getAllSongs().stream().filter(s -> s.getPlayCount() > 0)
				.sorted(Comparator.comparingInt(Song::getPlayCount).reversed()).limit(n).toList();
	}

	/**
	 * Aggiunge una nuova playlist alla collezione.
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
	public void updatePlaylistPlayCount(Playlist playlist) {
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
		playlistDAO.removeSong(playlist.getId(), song.getId());
		playlist.removeSong(song);
	}

    /**
     * Ripristina una playlist precedentemente rimossa dalla collezione.
     * Reintroduce la playlist nel catalogo e ricrea le associazioni con le
     * tracce contenute all'interno del database.
     *
     * @param playlist
     *            playlist da ripristinare
     * @throws IllegalArgumentException
     *             se la playlist è null o già presente nella collezione
     * @throws PersistenceException
     *             se si verifica un errore durante il salvataggio nel database
     */
    public void restorePlaylist(Playlist playlist) {

        playlistDAO.save(playlist);

        for (Song song : playlist.getSongs()) {
            playlistDAO.addSong(
                playlist.getId(),
                song.getId()
            );
        }

        playlistCatalog.addPlaylist(playlist);
    }

	/**
	 * Sposta una traccia in una nuova posizione della playlist.
	 *
	 * @param song
	 *            traccia da spostare
	 * @param playlist
	 *            playlist da modificare
	 * @param newPosition
	 *            nuovo indice della traccia nella playlist
	 * @throws IllegalArgumentException
	 *             se la traccia, la playlist o la posizione non sono validi
	 */
	public void moveSongInPlaylist(Song song, Playlist playlist, int newPosition) {
		if (playlist == null) {
			throw new IllegalArgumentException("La playlist non puo' essere null.");
		}

		playlist.moveSong(song, newPosition);
		playlistDAO.replaceSongs(playlist.getId(), playlist.getSongs().stream().map(Song::getId).toList());
	}

	/**
	 * Restituisce tutte le playlist della collezione.
	 *
	 * @return lista non modificabile di tutte le playlist
	 */
	public List<Playlist> getAllPlaylists() {
		return playlistCatalog.getAllPlaylists();
	}

	/**
	 * Cerca playlist in base al nome.
	 *
	 * @param query
	 *            testo da cercare; se vuoto restituisce tutte le playlist
	 * @return lista delle playlist compatibili con la ricerca
	 */
	public List<Playlist> searchPlaylists(String query) {
		return playlistCatalog.searchPlaylists(query);
	}

	/**
	 * Ordina i brani all'interno della playlist selezionata in base al titolo.
	 *
	 * @param playlist
	 *            playlist da ordinare
	 * @throws IllegalArgumentException
	 *             se la playlist è {@code null}
	 */
	public void sortPlaylistSongsByTitle(Playlist playlist) {
		if (playlist == null)
			throw new IllegalArgumentException("La playlist non può essere null!");

		playlist.sortSongsByTitle();
		playlistDAO.replaceSongs(playlist.getId(), playlist.getSongs().stream().map(Song::getId).toList());

	}

	/**
	 * Ordina i brani all'interno della playlist selezionata in base all'autore.
	 *
	 * @param playlist
	 *            playlist da ordinare
	 * @throws IllegalArgumentException
	 *             se la playlist è {@code null}
	 */
	public void sortPlaylistSongsByAuthor(Playlist playlist) {
		if (playlist == null)
			throw new IllegalArgumentException("La playlist non può essere null!");

		playlist.sortSongsByAuthor();
		playlistDAO.replaceSongs(playlist.getId(), playlist.getSongs().stream().map(Song::getId).toList());
	}

	/**
	 * Recupera le prime {@code n} playlist più riprodotte del catalogo, ordinate
	 * per numero di riproduzioni in ordine decrescente.
	 *
	 * @param n
	 *            il numero massimo di playlist da restituire; deve essere positivo
	 * @return lista di al più {@code n} playlist ordinate per playCount
	 *         decrescente; può contenere meno di {@code n} elementi se il catalogo
	 *         è più piccolo
	 */
	public List<Playlist> getTopPlaylists(int n) {
		if (n <= 0)
			return null;

		return playlistCatalog.getAllPlaylists().stream().filter(p -> p.getPlayCount() > 0)
				.sorted(Comparator.comparingInt(Playlist::getPlayCount).reversed()).limit(n).toList();
	}

	public Playlist createAutomaticPlaylist(String name, Set<Genre> genres, Set<Integer> years, Set<Tag> tags) {

		if (genres == null || years == null || tags == null) {
			throw new IllegalArgumentException("I criteri non possono essere null.");
		}

		if (genres.isEmpty() && years.isEmpty() && tags.isEmpty()) {
			throw new IllegalArgumentException("Selezionare almeno un criterio.");
		}

		List<Song> matchingSongs = songCatalog.findSongsMatchingAnyCriteria(genres, years, tags);

		if (matchingSongs.isEmpty()) {
			throw new IllegalArgumentException("Nessuna traccia soddisfa i criteri selezionati.");
		}

		Playlist playlist = new Playlist(name);

		// Prima registra la playlist nel catalogo e nel database.
		addPlaylist(playlist);

		// Poi aggiunge le canzoni
		for (Song song : matchingSongs) {
			addSongToPlaylist(song, playlist);
		}

		return playlist;
	}
}
