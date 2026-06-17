package it.unisa.musicplaylistmanager.model.library;

import it.unisa.musicplaylistmanager.exceptions.DuplicatedSongException;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.entity.Tag;

import java.util.*;

/**
 * Catalogo globale delle tracce musicali del sistema.
 */
public class SongCatalog {

	private final Map<UUID, Song> songs;

	/**
	 * Crea un nuovo catalogo vuoto.
	 */
	public SongCatalog() {
		this.songs = new HashMap<>();
	}

	/**
	 * Aggiunge una traccia al catalogo
	 *
	 * @param song
	 *            traccia da aggiungere;
     * @throws IllegalArgumentException
     *             se la traccia è {@code null}
     * @throws DuplicatedSongException
     *              se il brano è già presente nel catalogo
	 */
	public void addSong(Song song) throws IllegalArgumentException, DuplicatedSongException {
		if (song == null) {
			throw new IllegalArgumentException("La traccia non può essere null.");
		}
		if (contains(song)) {
			throw new DuplicatedSongException("Una traccia con ID '" + song.getId() + "' è già presente nel catalogo.");
		}
		songs.put(song.getId(), song);
	}

	/**
	 * Rimuove una traccia dal catalogo
	 *
	 * @param song
	 *            traccia da rimuovere;
     * @throws IllegalArgumentException
     *             se la traccia è {@code null} o non è presente nel
     *             catalogo
	 */
	public void removeSong(Song song) throws IllegalArgumentException {
		if (song == null) {
			throw new IllegalArgumentException("La traccia non può essere null.");
		}
		if (!contains(song)) {
			throw new IllegalArgumentException("La traccia '" + song.getTitle() + "' non è presente nel catalogo.");
		}

		songs.remove(song.getId());
	}

	/**
	 * Cerca tracce il cui titolo contenga la stringa specificata
	 *
	 * @param query
	 *            testo da cercare;
	 * @return lista delle tracce
     * @throws IllegalArgumentException
     *          se la query di ricerca è vuota
	 */
	public List<Song> searchSong(String query) throws IllegalArgumentException {
		if (query == null) {
			throw new IllegalArgumentException("La query di ricerca non può essere null.");
		}
		String queryLower = query.trim().toLowerCase();
		return songs.values().stream().filter(s -> s.getTitle().toLowerCase().contains(queryLower)).toList();
	}

	/**
	 * Filtra le tracce del catalogo combinando testo, autore, genere, anno e tag.
	 *
	 * @param query
	 *            testo da cercare nel titolo; se vuoto non viene applicato
	 * @param genre
	 *            genere richiesto; se null non viene applicato
	 * @param author
	 *            autore richiesto; se vuoto non viene applicato
	 * @param year
	 *            anno richiesto; se null non viene applicato
	 * @param tag
	 *            tag richiesto; se null non viene applicato
	 * @return lista delle tracce compatibili con i criteri indicati
	 */
	public List<Song> filterSongs(String query, Genre genre, String author, Integer year, Tag tag) {
		String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
		String normalizedAuthor = author == null ? "" : author.trim().toLowerCase();

		return songs.values().stream()
				.filter(song -> normalizedQuery.isEmpty() || song.getTitle().toLowerCase().contains(normalizedQuery))
				.filter(song -> genre == null || song.getGenre() == genre)
				.filter(song -> normalizedAuthor.isEmpty() || song.getAuthor().toLowerCase().equals(normalizedAuthor))
				.filter(song -> year == null || song.getYear() == year).filter(song -> tag == null || song.hasTag(tag))
				.toList();
	}

	/**
	 * Verifica se una traccia è presente nel catalogo.
	 *
	 * @param song
	 *            traccia da cercare
	 * @return {@code true} se la traccia è presente
	 */
	public boolean contains(Song song) {
		if (song == null)
			return false;
		return songs.containsKey(song.getId());
	}

	/**
	 * Restituisce una lista di tutte le tracce del catalogo.
	 *
	 * @return lista non modificabile delle tracce
	 */
	public List<Song> getAllSongs() {

		return songs.values().stream().toList();
	}

	public Song getSongById(UUID id) {
		return songs.get(id);
	}

	/**
	 * Restituisce il numero di tracce presenti nel catalogo.
	 *
	 * @return numero di tracce
	 */
	public int size() {

		return songs.size();
	}

	/**
	 * Indica se il catalogo è vuoto.
	 *
	 * @return true se non ci sono tracce nel catalogo
	 */
	public boolean isEmpty() {

		return songs.isEmpty();
	}

	/**
	 * Restituisce le tracce che soddisfano almeno uno dei criteri indicati.
	 *
	 * <p>
	 * I criteri vengono combinati mediante OR: una traccia è inclusa se appartiene
	 * a uno dei generi selezionati, se è stata pubblicata in uno degli anni
	 * selezionati oppure se possiede almeno uno dei tag selezionati.
	 *
	 * @param genres
	 *            generi selezionati
	 * @param years
	 *            anni selezionati
	 * @param tags
	 *            tag selezionati
	 * @return tracce che soddisfano almeno uno dei criteri
     * @throws IllegalArgumentException
     *            se i criteri per il filtraggio sono vuoti
	 */
	public List<Song> findSongsMatchingAnyCriteria(Set<Genre> genres, Set<Integer> years, Set<Tag> tags) {

		if (genres == null || years == null || tags == null) {
			throw new IllegalArgumentException("I criteri non possono essere null.");
		}

		return songs.values().stream().filter(song -> genres.contains(song.getGenre()) || years.contains(song.getYear())
				|| tags.stream().anyMatch(song::hasTag)).toList();
	}

}
