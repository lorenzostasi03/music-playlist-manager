package it.unisa.musicplaylistmanager.app;

import it.unisa.musicplaylistmanager.model.library.MusicLibrary;
import it.unisa.musicplaylistmanager.model.playback.Player;
import it.unisa.musicplaylistmanager.persistence.sqlite.SQLitePlaylistDAO;
import it.unisa.musicplaylistmanager.persistence.sqlite.SQLiteSongDAO;

/**
 * Contesto globale dell'applicazione.
 *
 * <p>Questa classe fornisce un punto di accesso centralizzato ai
 * componenti condivisi dell'applicazione, come la libreria musicale
 * e il player audio.</p>
 *
 * <p>Il contesto è implementato come singleton e viene inizializzato
 * una sola volta durante il ciclo di vita dell'applicazione.</p>
 */
public class AppContext {
    private static AppContext instance;

    private final String DB_URL = "jdbc:sqlite:database.db";
    private final MusicLibrary musicLibrary;
    private final Player player;

    private AppContext() {
        musicLibrary = new MusicLibrary(new SQLiteSongDAO(DB_URL), new SQLitePlaylistDAO(DB_URL));
        musicLibrary.init();
        player = new Player();
    }

    public static AppContext getInstance() {
        if (instance == null) instance = new  AppContext();

        return instance;
    }

    public MusicLibrary getMusicLibrary() { return this.musicLibrary; }
    public Player getPlayer() { return this.player; }
}
