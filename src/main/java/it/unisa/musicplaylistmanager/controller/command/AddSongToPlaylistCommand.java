package it.unisa.musicplaylistmanager.controller.command;

import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.library.MusicLibrary;

/**
 * Comando concreto per aggiungere un brano a una playlist.
 */
public class AddSongToPlaylistCommand implements Command {
    private final MusicLibrary musicLibrary;
    private final Playlist playlist;
    private final Song song;

    public AddSongToPlaylistCommand(MusicLibrary musicLibrary, Playlist playlist, Song song) {
        if (musicLibrary == null) throw new IllegalArgumentException("MusicLibrary non può essere null!");

        this.musicLibrary = musicLibrary;
        this.playlist = playlist;
        this.song = song;
    }

    @Override
    public void execute() { musicLibrary.addSongToPlaylist(song, playlist); }

    @Override
    public void undo() { musicLibrary.removeSongFromPlaylist(song, playlist); }
}
