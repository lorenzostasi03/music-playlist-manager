package it.unisa.musicplaylistmanager.app;

import it.unisa.musicplaylistmanager.model.library.MusicLibrary;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.persistence.dao.PlaylistDAO;
import it.unisa.musicplaylistmanager.persistence.dao.SongDAO;
import it.unisa.musicplaylistmanager.persistence.sqlite.SQLitePlaylistDAO;
import it.unisa.musicplaylistmanager.persistence.sqlite.SQLiteSongDAO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
/**
 * Classe principale dell'applicazione Music Playlist Manager.
 * Gestisce l'avvio dell'interfaccia grafica e mantiene i dati di sessione globali
 * come la libreria musicale e la playlist correntemente selezionata.
 */
public class App extends Application {

  private static MusicLibrary MUSIC_LIBRARY;
  private static Playlist selectedPlaylist;

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

  public static void initMusicLibrary() {
      SongDAO songDAO = new SQLiteSongDAO();
      PlaylistDAO playlistDAO = new SQLitePlaylistDAO();

      MUSIC_LIBRARY = new MusicLibrary(songDAO, playlistDAO);
      MUSIC_LIBRARY.init();
  }

/**
 * Avvia l'interfaccia grafica caricando la vista principale (MainView).
 *
 * @param stage la finestra principale dell'applicazione fornita dal framework JavaFX
 */
  @Override
  public void start(Stage stage) {
      initMusicLibrary();

      Scene scene = null;
      String path = "/views/MainView.fxml";

      try {
          scene = new Scene(
              FXMLLoader.load(getClass().getResource(path))
          );
      } catch (IOException e) {
          System.err.println("File non trovato: " + path);
      }

      initMusicLibrary();
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
