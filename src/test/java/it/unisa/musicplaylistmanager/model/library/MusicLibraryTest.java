package it.unisa.musicplaylistmanager.model.library;

import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per la classe MusicLibrary.
 */
class MusicLibraryTest {

    private MusicLibrary library;
    private Song song;
    private Playlist playlist;

    @BeforeEach
    void setUp() {
        library = new MusicLibrary();
        song = new Song("Yesterday", "Beatles", Genre.POP, 1965, 125, "/y.mp3");
        playlist = new Playlist("60s Classics");
    }


    /**
     * Verifica che una traccia aggiunta al catalogo sia recuperabile.
     */
    @Test
    void testAggiungiTracciaCatalogo() {
        library.addSongToCatalog(song);
        assertTrue(library.catalogContains(song));
        assertEquals(1, library.getAllSongs().size());
    }


    /**
     * Verifica che rimuovere una traccia dal catalogo la elimini
     * automaticamente da tutte le playlist in cui era presente.
     */
    @Test
    void testRimozioneTracciaPropagataAllePlaylist() {
        library.addSongToCatalog(song);
        library.addPlaylist(playlist);
        library.addSongToPlaylist(song, playlist);

        library.removeSongFromCatalog(song);

        assertFalse(library.catalogContains(song));
        assertFalse(playlist.contains(song));
    }

    /**
     * Verifica che rimuovere una traccia non presente nel catalogo lanci un'eccezione.
     */
    @Test
    void testRimozioneTrAcciaNonPresenteLanciaEccezione() {
        assertThrows(IllegalArgumentException.class,
            () -> library.removeSongFromCatalog(song));
    }


    /**
     * Verifica che una playlist possa essere aggiunta al catalogo delle playlist.
     */
    @Test
    void testCreazionePlaylist() {
        library.addPlaylist(playlist);
        assertTrue(library.getAllPlaylists().contains(playlist));
    }


    /**
     * Verifica che una playlist possa essere rimossa dal catalogo delle playlist.
     */
    @Test
    void testEliminazionePlaylist() {
        library.addPlaylist(playlist);
        library.removePlaylist(playlist);
        assertFalse(library.getAllPlaylists().contains(playlist));
    }

    /**
     * Verifica che una playlist possa essere rinominata con un nome valido.
     */
    @Test
    void testRinominaPlaylistNomeValido() {
        library.addPlaylist(playlist);
        library.renamePlaylist(playlist, "Best of 60s");
        assertEquals("Best of 60s", playlist.getName());
    }

    /**
     * Verifica che rinominare una playlist con un nome già esistente lanci un'eccezione.
     */
    @Test
    void testRinominaPlaylistNomeDuplicato() {
        library.addPlaylist(playlist);
        library.addPlaylist(new Playlist("Best of 60s"));
        assertThrows(IllegalArgumentException.class,
            () -> library.renamePlaylist(playlist, "Best of 60s"));
    }

    /**
     * Verifica che rinominare una playlist null lanci un'eccezione.
     */
    @Test
    void testRinominaPlaylistNullLanciaEccezione() {
        assertThrows(IllegalArgumentException.class,
            () -> library.renamePlaylist(null, "NuovoNome"));
    }


    /**
     * Verifica che una traccia del catalogo possa essere aggiunta a una playlist.
     */
    @Test
    void testAggiuntaTracciaaPlaylist() {
        library.addSongToCatalog(song);
        library.addPlaylist(playlist);
        library.addSongToPlaylist(song, playlist);
        assertTrue(playlist.contains(song));
    }

    /**
     * Verifica che non sia possibile aggiungere a una playlist una traccia
     * non presente nel catalogo.
     */
    @Test
    void testAggiuntaTracciaFuoriCatalogo() {
        library.addPlaylist(playlist);
        assertThrows(IllegalArgumentException.class,
            () -> library.addSongToPlaylist(song, playlist));
    }


    /**
     * Verifica che una traccia possa essere rimossa da una playlist senza
     * influenzare il catalogo globale.
     */
    @Test
    void testRimozioneTracciaPlaylistNonToccaCatalogo() {
        library.addSongToCatalog(song);
        library.addPlaylist(playlist);
        library.addSongToPlaylist(song, playlist);

        library.removeSongFromPlaylist(song, playlist);

        assertFalse(playlist.contains(song));
        assertTrue(library.catalogContains(song));
    }
}
