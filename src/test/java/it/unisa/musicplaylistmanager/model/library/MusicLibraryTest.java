package it.unisa.musicplaylistmanager.model.library;

import it.unisa.musicplaylistmanager.fake.FakePlaylistDAO;
import it.unisa.musicplaylistmanager.fake.FakeSongDAO;
import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test della classe MusicLibrary.
 */
class MusicLibraryTest {

    private MusicLibrary library;
    private Song song;
    private Playlist playlist;

    @BeforeEach
    void setUp() {
        library = new MusicLibrary(new FakeSongDAO(), new FakePlaylistDAO());
        song = new Song("Yesterday", "Beatles", Genre.POP, 1965, 125, "/y.mp3");
        playlist = new Playlist("60s Classics");
    }

    @Test
    void aggiuntaBranoAlCatalogo() {
        library.addSongToCatalog(song);

        assertTrue(library.catalogContains(song));
        assertEquals(1, library.getAllSongs().size());
    }

    @Test
    void rimozioneBranoPropagataAllePlaylist() {
        library.addSongToCatalog(song);
        library.addPlaylist(playlist);
        library.addSongToPlaylist(song, playlist);

        library.removeSongFromCatalog(song);

        assertFalse(library.catalogContains(song));
        assertFalse(playlist.contains(song));
    }

    @Test
    void rimozioneBranoNonPresenteNonGeneraEccezione() {
        assertDoesNotThrow(() -> library.removeSongFromCatalog(song));

        assertFalse(library.catalogContains(song));
    }

    @Test
    void creazionePlaylist() {
        library.addPlaylist(playlist);

        assertTrue(library.getAllPlaylists().contains(playlist));
    }

    @Test
    void eliminazionePlaylist() {
        library.addPlaylist(playlist);
        library.removePlaylist(playlist);

        assertFalse(library.getAllPlaylists().contains(playlist));
    }

    @Test
    void rinominaPlaylistConNomeValido() {
        library.addPlaylist(playlist);

        library.renamePlaylist(playlist, "Best of 60s");

        assertEquals("Best of 60s", playlist.getName());
    }

    @Test
    void rinominaPlaylistConNomeDuplicatoLanciaEccezione() {
        library.addPlaylist(playlist);
        library.addPlaylist(new Playlist("Best of 60s"));

        assertThrows(IllegalArgumentException.class,
            () -> library.renamePlaylist(playlist, "Best of 60s"));
    }

    @Test
    void rinominaPlaylistNullLanciaEccezione() {
        assertThrows(IllegalArgumentException.class,
            () -> library.renamePlaylist(null, "NuovoNome"));
    }

    @Test
    void aggiuntaBranoAPlaylist() {
        library.addSongToCatalog(song);
        library.addPlaylist(playlist);

        library.addSongToPlaylist(song, playlist);

        assertTrue(playlist.contains(song));
    }

    @Test
    void aggiuntaBranoNonPresenteNelCatalogoLanciaEccezione() {
        library.addPlaylist(playlist);

        assertThrows(IllegalArgumentException.class,
            () -> library.addSongToPlaylist(song, playlist));
    }

    @Test
    void rimozioneBranoDaPlaylistNonInfluisceSulCatalogo() {
        library.addSongToCatalog(song);
        library.addPlaylist(playlist);
        library.addSongToPlaylist(song, playlist);

        library.removeSongFromPlaylist(song, playlist);

        assertFalse(playlist.contains(song));
        assertTrue(library.catalogContains(song));
    }
}
