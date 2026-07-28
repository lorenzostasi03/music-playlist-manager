package it.unisa.musicplaylistmanager.util;

import it.unisa.musicplaylistmanager.controller.Refreshable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

/**
 * Classe che gestisce la navigazione tra le schermate dell'applicazione.
 *
 * Le view FXML vengono caricate e mostrate nella regione centrale del layout
 * principale dell'applicazione.
 */
public final class ViewSwitcher {

	private static BorderPane mainRoot;
    private static Refreshable refreshable;
    private static Object currentController;

	private ViewSwitcher() {
	}

	/**
	 * Imposta il contenitore principale dell'applicazione nel quale verranno
	 * visualizzate le schermate.
	 *
	 * Questo metodo deve essere invocato prima di effettuare qualsiasi operazione
	 * di navigazione.
	 *
	 * @param root
	 *            BorderPane principale dell'applicazione
	 */
	public static void setMainRoot(BorderPane root) {
		mainRoot = root;
	}

	/**
	 * Carica la view specificata e la visualizza nella regione centrale del layout
	 * principale dell'applicazione.
	 *
	 * @param fxml
	 *            nome del file FXML da visualizzare
	 */
	public static void switchTo(String fxml) {
		if (mainRoot == null || fxml == null || fxml.isBlank()) {
			return;
		}

		String path = "/views/" + fxml;

		try {
			FXMLLoader loader = new FXMLLoader(ViewSwitcher.class.getResource(path));

			Parent view = loader.load();

            currentController = loader.getController();

			mainRoot.setCenter(view);

		} catch (IOException e) {
			System.err.println("File non trovato: " + path);
		}
	}

    public static void refreshCurrentView() {
        if (currentController == null) return;

        if (currentController instanceof Refreshable) {
            refreshable = (Refreshable) currentController;
            refreshable.refresh();
        }
    }
}
