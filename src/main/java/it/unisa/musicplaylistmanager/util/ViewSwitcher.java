package it.unisa.musicplaylistmanager.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe di utilità che gestisce la navigazione tra le schermate
 * dell'applicazione.
 *
 * Le view FXML vengono caricate e mostrate nella regione centrale
 * del layout principale dell'applicazione. Le view già caricate
 * vengono memorizzate in cache per evitarne il caricamento ripetuto.
 */
public final class ViewSwitcher {

    private static final Map<String, Parent> cache = new HashMap<>();

    private static BorderPane mainRoot;

    private ViewSwitcher() {}

    /**
     * Imposta il contenitore principale dell'applicazione nel quale
     * verranno visualizzate le schermate.
     *
     * Questo metodo deve essere invocato prima di effettuare
     * qualsiasi operazione di navigazione.
     *
     * @param root il BorderPane principale dell'applicazione
     */
    public static void setMainRoot(BorderPane root) {
        mainRoot = root;
    }

    /**
     * Carica la view specificata e la visualizza nella regione centrale
     * del layout principale dell'applicazione.
     *
     * Se la view è già presente in cache, viene riutilizzata senza
     * eseguire nuovamente il caricamento del file FXML.
     *
     * @param fxml nome del file FXML da visualizzare
     */
    public static void switchTo(String fxml) {
        if (mainRoot == null || fxml == null || fxml.isBlank()) {
            return;
        }

        String path = "/views/" + fxml;

        try {
            Parent view;

            if (cache.containsKey(path)) {
                view = cache.get(path);
            } else {
                FXMLLoader loader =
                    new FXMLLoader(ViewSwitcher.class.getResource(path));

                view = loader.load();
                cache.put(path, view);
            }

            mainRoot.setCenter(view);

        } catch (IOException e) {
            System.err.println("File non trovato: " + path);
        }
    }
}
