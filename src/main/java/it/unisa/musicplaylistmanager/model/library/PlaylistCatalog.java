package it.unisa.musicplaylistmanager.model.library;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Collezione di tutte le playlist dell'utente.
 */
public class PlaylistCatalog {
    private final List<Playlist> playlists;

    /**
     * Crea una nuova collezione vuota di playlist.
     */
    public PlaylistCatalog() {
        this.playlists = new ArrayList<>();
    }

    /**
     * Aggiunge una playlist
     * <p>
     * Non possono esistere due playlist con lo stesso nome
     * </p>
     *
     * @param playlist playlist da aggiungere;
     */
    public void addPlaylist(Playlist playlist) {
        if (playlist == null) {
            throw new IllegalArgumentException("La playlist non può essere null.");
        }
        if (existsByName(playlist.getName())) {
            throw new IllegalArgumentException(
                "Esiste già una playlist con il nome '" + playlist.getName() + "'.");
        }
        playlists.add(playlist);
    }

    /**
     * Rimuove una playlist
     * <p>
     * L'eliminazione della playlist non elimina le tracce dal catalogo globale.
     * </p>
     *
     * @param playlist playlist da rimuovere;
     */
    public void removePlaylist(Playlist playlist) {
        if (playlist == null) {
            throw new IllegalArgumentException("La playlist non può essere null.");
        }
        boolean rimossa = playlists.removeIf(
            p -> p.getName().equalsIgnoreCase(playlist.getName()));
        if (!rimossa) {
            throw new IllegalArgumentException(
                "La playlist '" + playlist.getName() + "' non è presente nella collezione.");
        }
    }

    /**
     * Verifica che il nuovo nome di una playlist non sia già in uso da
     * un'altra playlist
     *
     * @param currentName nome attuale della playlist da rinominare
     * @param newName     nuovo nome proposto
     * @return  true se il nuovo nome è già in uso da un'altra playlist
     */
    public boolean isNameTakenByOther(String currentName, String newName) {
        return playlists.stream()
            .filter(p -> !p.getName().equalsIgnoreCase(currentName))
            .anyMatch(p -> p.getName().equalsIgnoreCase(newName));
    }


    /**
     * Restituisce tutte le playlist che contengono la traccia specificata.
     *
     * @param song traccia da cercare nelle playlist
     * @return lista delle playlist che contengono la traccia
     */
    public List<Playlist> getPlaylistsContaining(Song song) {
        if (song == null) return Collections.emptyList();
        return playlists.stream()
            .filter(p -> p.contains(song))
            .collect(Collectors.toList());
    }

    /**
     * Verifica se esiste una playlist con il nome specificato
     *
     * @param name nome da cercare
     * @return true se esiste una playlist con quel nome
     */
    public boolean existsByName(String name) {
        if (name == null) return false;
        return playlists.stream()
            .anyMatch(p -> p.getName().equalsIgnoreCase(name.trim()));
    }

    /**
     * Restituisce una lista di tutte le playlist.
     *
     * @return lista non modificabile delle playlist
     */
    public List<Playlist> getAllPlaylists() {
        return Collections.unmodifiableList(playlists);
    }

    /**
     * Restituisce il numero di playlist nella collezione.
     *
     * @return numero di playlist
     */
    public int size() {
        return playlists.size();
    }

    /**
     * Indica se la collezione è vuota.
     *
     * @return true se non ci sono playlist
     */
    public boolean isEmpty() {
        return playlists.isEmpty();
    }
}
