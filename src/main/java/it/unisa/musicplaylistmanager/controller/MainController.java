package it.unisa.musicplaylistmanager.controller;

import it.unisa.musicplaylistmanager.controller.command.CommandExecutor;
import it.unisa.musicplaylistmanager.util.AlertManager;
import it.unisa.musicplaylistmanager.util.ViewSwitcher;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;

/**
 * Controller principale dell'applicazione. Gestisce la barra di navigazione per
 * spostarsi tra le sezioni principali del programma (Home, Catalogo,
 * Riproduzione).
 */
public class MainController {

	@FXML
	private BorderPane root;

	@FXML
	private Button undoCommandButton;

	@FXML

	private final CommandExecutor executor = CommandExecutor.getInstance();

	@FXML
	private void initialize() {
		ViewSwitcher.setMainRoot(root);
		ViewSwitcher.switchTo("HomeView.fxml");

		initUndoButton();
		setupUndoShortcut();
	}

	private void initUndoButton() {
		ReadOnlyBooleanProperty canUndo = executor.canUndoProperty();

		undoCommandButton.visibleProperty().bind(canUndo);
		undoCommandButton.managedProperty().bind(canUndo);
		undoCommandButton.disableProperty().bind(canUndo.not());
	}

	private void setupUndoShortcut() {
		root.sceneProperty().addListener((observable, oldScene, newScene) -> {

			if (newScene != null) {
				newScene.getAccelerators().put(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN), () -> {
					if (executor.canUndoProperty().get()) {
						onUndoCommand();
					}
				});
			}

		});
	}

	@FXML
	private void onHome() {
		ViewSwitcher.switchTo("HomeView.fxml");
	}

	@FXML
	private void onCatalog() {
		ViewSwitcher.switchTo("CatalogView.fxml");
	}

	@FXML
	private void onPlayback() {
		ViewSwitcher.switchTo("PlaybackView.fxml");
	}

	@FXML
	public void onUndoCommand() {
		executor.undo();

		AlertManager.showInfo("L'operazione è stata annullata.");

		ViewSwitcher.refreshCurrentView();
	}
}
