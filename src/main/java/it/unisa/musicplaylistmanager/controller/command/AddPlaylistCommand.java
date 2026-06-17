package it.unisa.musicplaylistmanager.controller.command;

import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.library.MusicLibrary;

/**
 * Comando concreto per aggiungere una playlist al catalogo.
 */
public class AddPlaylistCommand implements Command {
    private final MusicLibrary musicLibrary;
    private final Playlist playlist;

    public AddPlaylistCommand(MusicLibrary musicLibrary, Playlist playlist) {
        if(musicLibrary == null) throw new IllegalArgumentException("MusicLibrary non può essere null!");

        this.musicLibrary = musicLibrary;
        this.playlist = playlist;
    }

    @Override
    public void execute() { musicLibrary.addPlaylist(playlist); }

    @Override
    public void undo() { musicLibrary.removePlaylist(playlist); }
}
