package it.unisa.musicplaylistmanager.controller.command;

import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.library.MusicLibrary;
import it.unisa.musicplaylistmanager.model.playback.playable.PlaylistPlayable;
import it.unisa.musicplaylistmanager.model.playback.player.Player;

/**
 * Comando concreto per rimuovere una playlist dal catalogo. La playlist viene
 * rimossa anche dalla coda di riproduzione se presente.
 */
public class RemovePlaylistCommand implements Command {
	private final MusicLibrary musicLibrary;
	private final Player player;
	private Playlist playlist;

	public RemovePlaylistCommand(MusicLibrary musicLibrary, Player player, Playlist playlist) {
		if (musicLibrary == null)
			throw new IllegalArgumentException("MusicLibrary non può essere null!");
		if (player == null)
			throw new IllegalArgumentException("Player non può essere null!");

		this.musicLibrary = musicLibrary;
		this.player = player;
		this.playlist = playlist;
	}

	@Override
	public void execute() {
		musicLibrary.removePlaylist(playlist);

		if (playlist.getSongs().isEmpty())
			return;

		player.removeFromQueue(new PlaylistPlayable(playlist));
	}

	@Override
	public void undo() {
		musicLibrary.addPlaylist(playlist);
	}
}
