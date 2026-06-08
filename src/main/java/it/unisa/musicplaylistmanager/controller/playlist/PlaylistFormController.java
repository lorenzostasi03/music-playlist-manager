package it.unisa.musicplaylistmanager.controller.playlist;

import it.unisa.musicplaylistmanager.app.AppContext;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.util.AlertManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller per la finestra modale dedicata alla creazione di una nuova
 * playlist o alla rinomina di una playlist esistente.
 */
public class PlaylistFormController {

	@FXML
	private Label formTitleLabel;

	@FXML
	private TextField nameField;

	@FXML
	private Label nameErrorLabel;
	@FXML
	private Label globalErrorLabel;

	@FXML
	private Button cancelButton;
	@FXML
	private Button confirmButton;

	private Playlist playlistToEdit;
	private Runnable onSave;

	private final AppContext appContext = AppContext.getInstance();
	/**
	 * Inizializza il form nascondendo preventivamente tutte le etichette di errore.
	 */
	@FXML
	private void initialize() {
		hideErrors();
	}

	/**
	 * Imposta la playlist da rinominare.
	 *
	 * @param playlist
	 *            playlist esistente da modificare
	 */
	public void setPlaylistToEdit(Playlist playlist) {
		this.playlistToEdit = playlist;

		if (playlist == null) {
			return;
		}

		formTitleLabel.setText("Rinomina Playlist");
		confirmButton.setText("Salva nome");
		nameField.setText(playlist.getName());
	}

	/**
	 * Imposta l'azione da eseguire dopo un salvataggio corretto.
	 *
	 * @param onSave
	 *            callback di aggiornamento della vista chiamante
	 */
	public void setOnSave(Runnable onSave) {
		this.onSave = onSave;
	}

	@FXML
	private void onCancel() {
		closeWindow();
	}

	/**
	 * Gestisce il salvataggio o la modifica della playlist. Valida l'input e, in
	 * caso di successo, aggiorna la libreria e chiude la finestra.
	 */
	@FXML
	private void onConfirm() {
		hideErrors();

		try {
			String name = nameField.getText();

			if (playlistToEdit == null) {
				appContext.getMusicLibrary().addPlaylist(new Playlist(name));
				AlertManager.showInfo("Playlist creata correttamente.");
			} else {
				appContext.getMusicLibrary().renamePlaylist(playlistToEdit, name);
				AlertManager.showInfo("Playlist rinominata correttamente.");
			}

			if (onSave != null) {
				onSave.run();
			}
			closeWindow();
		} catch (IllegalArgumentException e) {
			showError(e.getMessage());
		}
	}

	private void hideErrors() {
		nameErrorLabel.setVisible(false);
		nameErrorLabel.setManaged(false);
		globalErrorLabel.setVisible(false);
		globalErrorLabel.setManaged(false);
	}

	private void showError(String message) {
		nameErrorLabel.setVisible(true);
		nameErrorLabel.setManaged(true);
		globalErrorLabel.setText(message);
		globalErrorLabel.setVisible(true);
		globalErrorLabel.setManaged(true);
		AlertManager.showError(message);
	}

	private void closeWindow() {
		cancelButton.getScene().getWindow().hide();
	}
}
