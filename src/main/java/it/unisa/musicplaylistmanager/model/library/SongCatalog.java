package it.unisa.musicplaylistmanager.model.library;

import it.unisa.musicplaylistmanager.model.entity.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Catalogo globale delle tracce musicali del sistema.
 */
public class SongCatalog {


    private final List<Song> songs;

    /**
     * Crea un nuovo catalogo vuoto.
     */
    public SongCatalog() {
        this.songs = new ArrayList<>();
    }

    /**
     * Aggiunge una traccia al catalogo
     *
     * @param song traccia da aggiungere;
     */
    public void addSong(Song song) {
        if (song == null) {
            throw new IllegalArgumentException("La traccia non può essere null.");
        }
        if (contains(song)) {
            throw new IllegalArgumentException(
                "Una traccia con ID '" + song.getId() + "' è già presente nel catalogo.");
        }
        songs.add(song);
    }

    /**
     * Rimuove una traccia dal catalogo
     *
     * @param song traccia da rimuovere;
     */
    public void removeSong(Song song) {
        if (song == null) {
            throw new IllegalArgumentException("La traccia non può essere null.");
        }
        boolean rimossa = songs.removeIf(s -> s.getId().equals(song.getId()));
        if (!rimossa) {
            throw new IllegalArgumentException(
                "La traccia '" + song.getTitle() + "' non è presente nel catalogo.");
        }
    }

    /**
     * Cerca tracce il cui titolo contenga la stringa specificata
     *
     * @param query testo da cercare;
     * @return lista delle tracce
     */
    public List<Song> searchSong(String query) {
        if (query == null) {
            throw new IllegalArgumentException("La query di ricerca non può essere null.");
        }
        String queryLower = query.trim().toLowerCase();
        return songs.stream()
            .filter(s -> s.getTitle().toLowerCase().contains(queryLower))
            .collect(Collectors.toList());
    }



    /**
     * Verifica se una traccia  è presente nel catalogo.
     *
     * @param song traccia da cercare
     * @return {@code true} se la traccia è presente
     */
    public boolean contains(Song song) {
        if (song == null) return false;
        return songs.contains(song);
    }


    /**
     * Restituisce una lista di tutte le tracce del catalogo.
     *
     * @return lista non modificabile delle tracce
     */
    public List<Song> getAllSongs() {

        return Collections.unmodifiableList(songs);
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
     * @return  true se non ci sono tracce nel catalogo
     */
    public boolean isEmpty() {

        return songs.isEmpty();
    }


}
