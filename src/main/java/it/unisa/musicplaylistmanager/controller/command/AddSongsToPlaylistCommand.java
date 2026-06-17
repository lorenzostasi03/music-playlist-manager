package it.unisa.musicplaylistmanager.controller.command;

import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.library.MusicLibrary;

import java.util.List;

/**
 * Comando concreto per aggiungere una lista di brani a una playlist.
 */
public class AddSongsToPlaylistCommand implements Command {
    private final MusicLibrary musicLibrary;
    private final Playlist playlist;
    private final List<Song> songs;

    public AddSongsToPlaylistCommand(MusicLibrary musicLibrary, Playlist playlist, List<Song> songs) {
        if (musicLibrary == null) throw new IllegalArgumentException("MusicLibrary non può essere null!");

        this.musicLibrary = musicLibrary;
        this.playlist = playlist;
        this.songs = songs;
    }

    @Override
    public void execute() {
        for (Song song : songs)
            musicLibrary.addSongToPlaylist(song, playlist);
    }

    @Override
    public void undo() {
        for (Song song : songs)
            musicLibrary.removeSongFromPlaylist(song, playlist);
    }
}
