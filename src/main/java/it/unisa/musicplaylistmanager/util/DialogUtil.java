package it.unisa.musicplaylistmanager.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.function.Consumer;

public class DialogUtil {

    /**
     * Apre una finestra modale caricata da FXML e permette di inizializzare il suo controller.
     *
     * @param fxml file FXML della view da caricare
     * @param title titolo mostrato nella finestra
     * @param owner finestra principale che “possiede” il dialog
     * @param controllerInit funzione per configurare il controller appena creato
     */
    public static <T> void open(String fxml, String title, Window owner, Consumer<T> controllerInit
    ) {
        String path = "/views/" + fxml;

        try {
            FXMLLoader loader = new FXMLLoader(DialogUtil.class.getResource(path));

            Parent root = loader.load();
            T controller = loader.getController();

            controllerInit.accept(controller);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(owner);
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Impossibile aprire il dialog: " + path);
        }
    }
}
