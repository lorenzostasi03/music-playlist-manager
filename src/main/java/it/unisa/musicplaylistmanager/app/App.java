package it.unisa.musicplaylistmanager.app;

import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.library.MusicLibrary;
import it.unisa.musicplaylistmanager.model.playback.Playable;
import it.unisa.musicplaylistmanager.model.playback.Player;
import it.unisa.musicplaylistmanager.persistence.dao.PlaylistDAO;
import it.unisa.musicplaylistmanager.persistence.dao.SongDAO;
import it.unisa.musicplaylistmanager.persistence.sqlite.DatabaseConfig;
import it.unisa.musicplaylistmanager.persistence.sqlite.DatabaseInitializer;
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
	/**
	 * Avvia l'interfaccia grafica caricando la vista principale (MainView).
	 *
	 * @param stage
	 *            la finestra principale dell'applicazione fornita dal framework
	 *            JavaFX
	 */
	@Override
	public void start(Stage stage) {
		Scene scene = null;
		String path = "/views/MainView.fxml";

		AppContext.getInstance();

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
	 * @param args
	 *            argomenti passati da riga di comando
	 */
	public static void main(String[] args) {
		DatabaseInitializer.initialize();
		launch(args);
	}
}
