package it.unisa.musicplaylistmanager.model.entity;

import java.util.*;

/**
 * Rappresenta una playlist musicale.
 */
public class Playlist {

	private final UUID id;
	private String name;
	private final List<Song> songs;
	private int playCount;

	/**
	 * Crea una nuova playlist con il nome specificato. La playlist viene creata
	 * vuota, senza tracce.
	 *
	 * @param name
	 *            nome della playlist;
	 */
	public Playlist(String name) {
		validateName(name);
		this.id = UUID.randomUUID();
		this.name = name.trim();
		this.songs = new ArrayList<>();
		this.playCount = 0;
	}

	/**
	 * Crea una playlist con stato già esistente. La lista dei brani non è inclusa e
	 * deve essere gestita separatamente.
	 *
	 * @param id
	 *            identificatore della playlist.
	 * @param name
	 *            nome della playlist.
	 * @param playCount
	 *            numero di riproduzioni della playlist.
	 */
	public Playlist(UUID id, String name, int playCount) {
		validateName(name);
		this.id = id;
		this.name = name;
		this.songs = new ArrayList<>();
		this.playCount = playCount;
	}

	/**
	 * Restituisce l'id della playlist.
	 *
	 * @return id della playlist.
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Restituisce il nome della playlist.
	 *
	 * @return nome della playlist
	 */
	public String getName() {
		return name;
	}

	/**
	 * Restituisce la lista delle tracce.
	 *
	 * @return lista non modificabile delle tracce
	 */
	public List<Song> getSongs() {
		return Collections.unmodifiableList(songs);
	}

	/**
	 * Restituisce il numero di riproduzioni della playlist.
	 *
	 * @return contatore di riproduzioni
	 */
	public int getPlayCount() {
		return playCount;
	}

	/**
	 * Restituisce il numero di tracce presenti nella playlist.
	 *
	 * @return numero di tracce
	 */
	public int size() {
		return songs.size();
	}

	/**
	 * Indica se la playlist non contiene tracce.
	 *
	 * @return {@code true} se la playlist è vuota
	 */
	public boolean isEmpty() {
		return songs.isEmpty();
	}

	/**
	 * Imposta un nuovo nome per la playlist
	 *
	 * @param name
	 *            nuovo nome;
	 */
	public void setName(String name) {
		validateName(name);
		this.name = name.trim();
	}

	/**
	 * Aggiunge una traccia in coda alla playlist
	 *
	 * @param song
	 *            traccia da aggiungere;
	 */
	public void addSong(Song song) {
		if (song == null) {
			throw new IllegalArgumentException("La traccia non può essere null.");
		}
		if (contains(song)) {
			throw new IllegalArgumentException("La traccia '" + song.getTitle() + "' è già presente nella playlist.");
		}
		songs.add(song);
	}

	/**
	 * Rimuove una traccia dalla playlist
	 *
	 * @param song
	 *            traccia da rimuovere;
	 */
	public void removeSong(Song song) {
		if (song == null) {
			throw new IllegalArgumentException("La traccia non può essere null.");
		}
		if (!contains(song)) {
			throw new IllegalArgumentException("La traccia '" + song.getTitle() + "' non è presente nella playlist.");
		}
		songs.remove(song);
	}

	/**
	 * Sposta una traccia in una nuova posizione all'interno della playlist
	 *
	 * @param song
	 *            traccia da spostare che deve essere presente nella playlist
	 * @param newPosition
	 *            nuova posizione
	 */
	public void moveSong(Song song, int newPosition) {
		if (song == null) {
			throw new IllegalArgumentException("La traccia non può essere null.");
		}
		if (!contains(song)) {
			throw new IllegalArgumentException("La traccia '" + song.getTitle() + "' non è presente nella playlist.");
		}
		if (newPosition < 0 || newPosition >= songs.size()) {
			throw new IllegalArgumentException(
					"Posizione non valida: " + newPosition + ". Range consentito: [0, " + (songs.size() - 1) + "].");
		}
		songs.remove(song);
		songs.add(newPosition, song);
	}

	/**
	 * Restituisce la traccia alla posizione specificata (zero-based).
	 *
	 * @param index
	 *            indice della traccia;
	 * @return traccia all'indice indicato
	 */
	public Song getSongAt(int index) {
		if (index < 0 || index >= songs.size()) {
			throw new IndexOutOfBoundsException("Indice non valido: " + index);
		}
		return songs.get(index);
	}

	/**
	 * Verifica se la traccia è già presente nella playlist.
	 *
	 * @param song
	 *            traccia da cercare
	 * @return true se la traccia è contenuta nella playlist
	 */
	public boolean contains(Song song) {
		return songs.contains(song);
	}

	/**
	 * Cerca le tracce della playlist in base al titolo.
	 *
	 * @param query
	 *            testo da cercare nel titolo; se vuoto restituisce tutte le tracce
	 * @return lista delle tracce compatibili con la ricerca
	 */
	public List<Song> searchSongs(String query) {
		String normalizedQuery = query == null ? "" : query.trim().toLowerCase();

		if (normalizedQuery.isEmpty()) {
			return getSongs();
		}

		return songs.stream().filter(song -> song.getTitle().toLowerCase().contains(normalizedQuery)).toList();
	}

	/**
	 * Incrementa il contatore di riproduzioni della playlist.
	 */
	public void incrementPlayCount() {
		this.playCount++;
	}

	/**
	 * Due playlist sono considerate uguali se e solo se hanno lo stesso nome
	 *
	 * @param o
	 *            oggetto da confrontare
	 * @return true se i nomi coincidono
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Playlist))
			return false;
		Playlist playlist = (Playlist) o;
		return name.equalsIgnoreCase(playlist.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name.toLowerCase());
	}

	/**
	 * Restituisce una rappresentazione testuale della playlist.
	 *
	 * @return stringa descrittiva
	 */
	@Override
	public String toString() {
		return String.format("Playlist{name='%s', tracce=%d, riproduzioni=%d}", name, songs.size(), playCount);
	}

	/**
	 * Valida che il nome non sia null né composto solo da spazi.
	 *
	 * @param name
	 *            nome da validare
	 */
	private void validateName(String name) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Il nome della playlist non può essere vuoto.");
		}
	}
}
