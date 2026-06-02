package it.unisa.musicplaylistmanager.model.library;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;

import java.util.List;

/**
 * Facciata  che espone un'interfaccia unificata
 * per tutte le operazioni sul catalogo musicale e sulle playlist.
 */
public class MusicLibrary {
    private final SongCatalog songCatalog;
    private final PlaylistCatalog playlistCollection;

    /**
     * Crea una nuova libreria musicale con catalogo e collezione playlist vuoti.
     */
    public MusicLibrary() {
        this.songCatalog = new SongCatalog();
        this.playlistCollection = new PlaylistCatalog();
    }


    /**
     * Aggiunge una traccia al catalogo globale
     *
     * @param song traccia da aggiungere;
     */
    public void addSongToCatalog(Song song) {
        songCatalog.addSong(song);
    }

    /**
     * Rimuove definitivamente una traccia dal catalogo
     *
     * @param song traccia da rimuovere;
     */
    public void removeSongFromCatalog(Song song) {
        List<Playlist> playlistConTraccia =
            playlistCollection.getPlaylistsContaining(song);
        for (Playlist playlist : playlistConTraccia) {
            playlist.removeSong(song);
        }
        songCatalog.removeSong(song);
    }

    /**
     * Cerca tracce nel catalogo per titolo
     *
     * @param query testo da cercare;
     * @return lista delle tracce il cui titolo contiene {@code query}
     */
    public List<Song> searchSong(String query) {
        return songCatalog.searchSong(query);
    }


    /**
     * Verifica se una traccia è presente nel catalogo.
     *
     * @param song traccia da verificare
     * @return true se la traccia è nel catalogo
     */
    public boolean catalogContains(Song song) {

        return songCatalog.contains(song);
    }

    /**
     * Restituisce tutte le tracce del catalogo
     *
     * @return lista non modificabile di tutte le tracce
     */
    public List<Song> getAllSongs() {

        return songCatalog.getAllSongs();
    }

    /**
     * Crea e aggiunge una nuova playlist alla collezione
     *
     * @param playlist playlist da aggiungere;
     */
    public void addPlaylist(Playlist playlist) {

        playlistCollection.addPlaylist(playlist);
    }

    /**
     * Rimuove una playlist dalla collezione
     *
     * @param playlist playlist da rimuovere;
     */
    public void removePlaylist(Playlist playlist) {

        playlistCollection.removePlaylist(playlist);
    }

    /**
     * Rinomina una playlist esistente
     *
     * @param playlist playlist da rinominare;
     * @param newName  nuovo nome; non deve essere vuoto
     */
    public void renamePlaylist(Playlist playlist, String newName) {
        if (playlist == null) {
            throw new IllegalArgumentException("La playlist non può essere null.");
        }
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Il nuovo nome della playlist non può essere vuoto.");
        }
        if (playlistCollection.isNameTakenByOther(playlist.getName(), newName)) {
            throw new IllegalArgumentException(
                "Esiste già una playlist con il nome '" + newName + "'.");
        }
        playlist.setName(newName);
    }

    /**
     * Aggiunge una traccia del catalogo a una playlist
     *
     * @param song     traccia da aggiungere; deve essere nel catalogo
     * @param playlist playlist destinazione;
     */
    public void addSongToPlaylist(Song song, Playlist playlist) {
        if (!songCatalog.contains(song)) {
            throw new IllegalArgumentException(
                "La traccia '" + song.getTitle() +
                    "' non è presente nel catalogo. Aggiungila prima al catalogo.");
        }
        playlist.addSong(song);
    }

    /**
     * Rimuove una traccia da una playlist
     *
     * @param song     traccia da rimuovere dalla playlist
     * @param playlist playlist da cui rimuovere la traccia
     */
    public void removeSongFromPlaylist(Song song, Playlist playlist) {

        playlist.removeSong(song);
    }

    /**
     * Restituisce tutte le playlist della collezione
     *
     * @return lista non modificabile di tutte le playlist
     */
    public List<Playlist> getAllPlaylists() {
        return playlistCollection.getAllPlaylists();
    }
}
