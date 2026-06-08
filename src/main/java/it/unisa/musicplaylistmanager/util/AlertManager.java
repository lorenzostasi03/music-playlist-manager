package it.unisa.musicplaylistmanager.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Utility statica che centralizza la visualizzazione dei messaggi all'utente
 * tramite finestre di dialogo JavaFX.
 *
 * Fornisce metodi per mostrare messaggi informativi, messaggi di errore e
 * richieste di conferma, evitando la duplicazione del codice di creazione e
 * configurazione degli alert nei controller.
 */
public final class AlertManager {

	private AlertManager() {
	}

	/**
	 * Mostra un messaggio informativo all'utente.
	 *
	 * @param message
	 *            il messaggio da visualizzare
	 */
	public static void showInfo(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setContentText(message);
		alert.showAndWait();
	}

	/**
	 * Mostra un messaggio di errore all'utente.
	 *
	 * @param message
	 *            il messaggio di errore da visualizzare
	 */
	public static void showError(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setContentText(message);
		alert.showAndWait();
	}

	/**
	 * Mostra una finestra di conferma e attende la scelta dell'utente.
	 *
	 * @param message
	 *            il messaggio da visualizzare
	 * @return {@code true} se l'utente conferma l'operazione, {@code false}
	 *         altrimenti
	 */
	public static boolean showConfirmation(String message) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setContentText(message);

		Optional<ButtonType> result = alert.showAndWait();

		return result.isPresent() && result.get() == ButtonType.OK;
	}
}
