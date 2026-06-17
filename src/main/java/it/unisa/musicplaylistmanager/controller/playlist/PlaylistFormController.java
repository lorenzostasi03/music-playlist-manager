package it.unisa.musicplaylistmanager.controller.playlist;

import it.unisa.musicplaylistmanager.app.AppContext;
import it.unisa.musicplaylistmanager.controller.command.AddPlaylistCommand;
import it.unisa.musicplaylistmanager.controller.command.Command;
import it.unisa.musicplaylistmanager.controller.command.CommandExecutor;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.util.AlertManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Set;

/**
 * Controller per la finestra per creare o rinominare una playlist.
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
	private final CommandExecutor executor = CommandExecutor.getInstance();

	private final Set<String> defaultPlaylistNames = Set.of("Top 10");
	/**
	 * Inizializza il form.
	 */
	@FXML
	private void initialize() {
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
		try {
			String name = nameField.getText();

			boolean invalidName = defaultPlaylistNames.stream().map(String::toLowerCase)
					.anyMatch(s -> s.equals(name.toLowerCase()));

			if (invalidName) {
				AlertManager.showError("Non è possibile creare una playlist con questo nome!");
				return;
			}

			if (playlistToEdit == null) {
				Command cmd = new AddPlaylistCommand(appContext.getMusicLibrary(), new Playlist(name));
				executor.execute(cmd);
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
			AlertManager.showError(e.getMessage());
		}
	}

	private void closeWindow() {
		cancelButton.getScene().getWindow().hide();
	}
}
