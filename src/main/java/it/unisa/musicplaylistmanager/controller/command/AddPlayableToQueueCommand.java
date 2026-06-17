package it.unisa.musicplaylistmanager.controller.command;

import it.unisa.musicplaylistmanager.model.playback.playable.Playable;
import it.unisa.musicplaylistmanager.model.playback.player.Player;

/**
 * Comando concreto per aggiungere un Playable alla coda di riproduzione.
 */
public class AddPlayableToQueueCommand implements Command {
	private final Player player;
	private final Playable playable;

	public AddPlayableToQueueCommand(Player player, Playable playable) {
		if (player == null)
			throw new IllegalArgumentException("Player non può essere null!");

		this.player = player;
		this.playable = playable;
	}

	@Override
	public void execute() {
		player.enqueue(playable);
	}

	@Override
	public void undo() {
		player.removeLast();
	}
}
