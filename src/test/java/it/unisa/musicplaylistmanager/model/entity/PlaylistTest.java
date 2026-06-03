package it.unisa.musicplaylistmanager.model.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di unità per la classe Playlist.
 */
class PlaylistTest {

    private Playlist playlist;


    private Song song1;

    private Song song2;


    @BeforeEach
    void setUp() {
        playlist = new Playlist("La mia playlist");
        song1 = new Song("Song A", "Artist A", Genre.POP, 2020, 180, "/a.mp3");
        song2 = new Song("Song B", "Artist B", Genre.ROCK, 2019, 240, "/b.mp3");
    }

    /**
     * Verifica che una playlist creata con un nome valido sia inizialmente vuota.
     */
    @Test
    void testCreazionePlaylistVuota() {
        assertEquals("La mia playlist", playlist.getName());
        assertTrue(playlist.isEmpty());
        assertEquals(0, playlist.size());
    }

    /**
     * Verifica che creare una playlist con nome null lanci un'eccezione.
     */
    @Test
    void testNomeNullLanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> new Playlist(null));
    }

    /**
     * Verifica che creare una playlist con il campo nome vuoto lanci un'eccezione.
     */
    @Test
    void testNomeVuotoLanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> new Playlist("   "));
    }


    /**
     * Verifica che il nome della playlist venga aggiornato correttamente.
     */
    @Test
    void testRinominaPlaylist() {
        playlist.setName("Nuovo nome");
        assertEquals("Nuovo nome", playlist.getName());
    }

    /**
     * Verifica che il nome non possa essere impostato a una stringa vuota.
     */
    @Test
    void testRinominaConNomeVuoto() {
        assertThrows(IllegalArgumentException.class, () -> playlist.setName(""));
    }


    /**
     * Verifica che una traccia possa essere aggiunta con successo alla playlist.
     */
    @Test
    void testAggiuntaTraccia() {
        playlist.addSong(song1);
        assertTrue(playlist.contains(song1));
        assertEquals(1, playlist.size());
    }

    /**
     * Verifica che aggiungere lo stesso brano nella playlist due volte lanci un'eccezione.
     */
    @Test
    void testAggiuntaTracciaDuplicataLanciaEccezione() {
        playlist.addSong(song1);
        assertThrows(IllegalArgumentException.class, () -> playlist.addSong(song1));
    }

    /**
     * Verifica che aggiungere una traccia null in una playlist lanci un'eccezione.
     */
    @Test
    void testAggiuntaTracciaNullLanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> playlist.addSong(null));
    }


    /**
     * Verifica che la playlist restituisca l'elenco esatto delle canzoni inserite.
     */
    @Test
    void testGetSongs() {
        playlist.addSong(song1);
        playlist.addSong(song2);
        assertEquals(2, playlist.getSongs().size());
        assertTrue(playlist.getSongs().contains(song1));
        assertTrue(playlist.getSongs().contains(song2));
    }

    /**
     * Verifica che la lista restituita sia non modificabile dall'esterno.
     */
    @Test
    void testGetSongsListaImmutabile() {
        assertThrows(UnsupportedOperationException.class,
            () -> playlist.getSongs().add(song1));
    }

    /**
     * Controlla che sia possibile recuperare un brano specifico
     * fornendo la sua esatta posizione (indice) all'interno della playlist.
     */
    @Test
    void testGetSongAt() {
        playlist.addSong(song1);
        playlist.addSong(song2);
        assertEquals(song1, playlist.getSongAt(0));
        assertEquals(song2, playlist.getSongAt(1));
    }

    /**
     * Assicura che la ricerca di un brano in una posizione inesistente lanci un'eccezione.
     */
    @Test
    void testGetSongAtFuoriRange() {
        assertThrows(IndexOutOfBoundsException.class, () -> playlist.getSongAt(0));
    }


    /**
     * Verifica che una traccia possa essere rimossa da una playlist con successo.
     */
    @Test
    void testRimozioneTraccia() {
        playlist.addSong(song1);
        playlist.removeSong(song1);
        assertFalse(playlist.contains(song1));
        assertTrue(playlist.isEmpty());
    }

    /**
     * Verifica che rimuovere una traccia dalla playlist non presente lanci un'eccezione.
     */
    @Test
    void testRimozioneTracciaNonPresenteLanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> playlist.removeSong(song1));
    }

    /**
     * Verifica che rimuovere una traccia null lanci un'eccezione.
     */
    @Test
    void testRimozioneTracciaNullLanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> playlist.removeSong(null));
    }


    /**
     * Verifica che se sposto un brano in una nuova posizione (es. lo porto in cima), la lista deve aggiornarsi di conseguenza.
     */
    @Test
    void testMoveSong() {
        playlist.addSong(song1);
        playlist.addSong(song2);
        playlist.moveSong(song2, 0);
        assertEquals(song2, playlist.getSongAt(0));
        assertEquals(song1, playlist.getSongAt(1));
    }

    /**
     * Verifica che spostare un brano in una posizione non valida lanci un'eccezione.
     */
    @Test
    void testMoveSongPosizioneNonValida() {
        playlist.addSong(song1);
        assertThrows(IllegalArgumentException.class, () -> playlist.moveSong(song1, 5));
    }

    /**
     * Controlla che le statistiche di ascolto dell'intera playlist
     * avanzino di un'unità ad ogni riproduzione registrata.
     */
    @Test
    void testIncrementPlayCount() {
        playlist.incrementPlayCount();
        assertEquals(1, playlist.getPlayCount());
    }

    /**
     * Verifica che due playlist con lo stesso nome (case-insensitive) siano uguali.
     */
    @Test
    void testEqualsStesoNome() {
        Playlist altra = new Playlist("LA MIA PLAYLIST");
        assertEquals(playlist, altra);
    }

    /**
     * Verifica che due playlist con nomi diversi non siano uguali.
     */
    @Test
    void testEqualsNomiDiversiNonUguali() {
        Playlist altra = new Playlist("Altra playlist");
        assertNotEquals(playlist, altra);
    }
}
