package it.unisa.musicplaylistmanager.app;

import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.library.MusicLibrary;
import it.unisa.musicplaylistmanager.model.playback.Playable;
import it.unisa.musicplaylistmanager.model.playback.Player;
import it.unisa.musicplaylistmanager.persistence.dao.PlaylistDAO;
import it.unisa.musicplaylistmanager.persistence.dao.SongDAO;
import it.unisa.musicplaylistmanager.persistence.sqlite.SQLitePlaylistDAO;
import it.unisa.musicplaylistmanager.persistence.sqlite.SQLiteSongDAO;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe principale dell'applicazione Music Playlist Manager. Gestisce l'avvio
 * dell'interfaccia grafica e mantiene i dati di sessione globali come la
 * libreria musicale e la playlist correntemente selezionata.
 */
public class App extends Application {

    private final String DB_URL = "jdbc:sqlite:database.db";

    private static MusicLibrary MUSIC_LIBRARY;
    private static Playlist selectedPlaylist;

    private static final Player PLAYER = new Player();
    private static Playable currentPlayable;

    /**
     * Restituisce la libreria musicale condivisa dai controller JavaFX.
     *
     * @return libreria musicale dell'applicazione
     */
    public static MusicLibrary getMusicLibrary() {
        return MUSIC_LIBRARY;
    }

    /**
     * Imposta la playlist selezionata nella Home.
     *
     * @param playlist playlist da visualizzare
     */
    public static void setSelectedPlaylist(Playlist playlist) {
        selectedPlaylist = playlist;
    }

    /**
     * Restituisce la playlist selezionata nella Home.
     *
     * @return playlist selezionata
     */
    public static Playlist getSelectedPlaylist() {
        return selectedPlaylist;
    }

    /**
     * Restituisce il player condiviso dell'applicazione.
     *
     * @return player usato per la riproduzione
     */
    public static Player getPlayer() {
        return PLAYER;
    }

    /**
     * Imposta l'oggetto attualmente riproducibile.
     *
     * @param playable oggetto riproducibile corrente
     */
    public static void setCurrentPlayable(Playable playable) {
        currentPlayable = playable;
    }

    /**
     * Restituisce l'oggetto attualmente riproducibile.
     *
     * @return oggetto riproducibile corrente
     */
    public static Playable getCurrentPlayable() {
        return currentPlayable;
    }

    public void initMusicLibrary() {
        SongDAO songDAO = new SQLiteSongDAO(DB_URL);
        PlaylistDAO playlistDAO = new SQLitePlaylistDAO(DB_URL);

        MUSIC_LIBRARY = new MusicLibrary(songDAO, playlistDAO);
        MUSIC_LIBRARY.init();
    }

    /**
     * Avvia l'interfaccia grafica caricando la vista principale (MainView).
     *
     * @param stage la finestra principale dell'applicazione fornita dal framework
     *        JavaFX
     */
    @Override
    public void start(Stage stage) {
//        initMusicLibrary();

        Scene scene = null;
        String path = "/views/MainView.fxml";

        try {
            scene = new Scene(FXMLLoader.load(getClass().getResource(path)));
        } catch (IOException e) {
            System.err.println("File non trovato: " + path);
        }

        stage.setTitle("Music Playlist Manager");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Metodo di ingresso (entry-point) per l'avvio dell'applicazione.
     *
     * @param args argomenti passati da riga di comando
     */
    public static void main(String[] args) {
        launch(args);
    }
}
