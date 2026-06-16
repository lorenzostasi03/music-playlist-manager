package it.unisa.musicplaylistmanager.controller.command;

import it.unisa.musicplaylistmanager.model.playback.playable.Playable;
import it.unisa.musicplaylistmanager.model.playback.player.Player;

/**
 * Comando concreto per rimuovere un Playable dalla coda.
 */
public class RemovePlayableFromQueueCommand implements Command {
    private final Playable playable;
    private final Player player;

    public RemovePlayableFromQueueCommand(Player player, Playable playable) {
        if (player == null) throw new IllegalArgumentException("Player non può essere null!");

        this.player = player;
        this.playable = playable;
    }

    @Override
    public void execute() { player.removeFromQueue(playable); }

    @Override
    public void undo() { player.enqueue(playable); }
}
