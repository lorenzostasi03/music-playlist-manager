package it.unisa.musicplaylistmanager.controller.command;

import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Tag;
import it.unisa.musicplaylistmanager.model.library.MusicLibrary;

import java.util.Set;

/**
 * Comando concreto per creare una playlist automatica.
 */
public class AddAutomaticPlaylistCommand implements Command {
    private final MusicLibrary musicLibrary;
    private Playlist playlist;

    private final String name;
    private final Set<Genre> genres;
    private final Set<Integer> years;
    private final Set<Tag> tags;

    public  AddAutomaticPlaylistCommand(MusicLibrary musicLibrary, String name,
                                        Set<Genre> genres, Set<Integer> years, Set<Tag> tags) {

        if (musicLibrary == null) throw new IllegalArgumentException("MusicLibrary non può essere null!");

        this.musicLibrary = musicLibrary;
        this.name = name;
        this.genres = genres;
        this.years = years;
        this.tags = tags;
    }

    @Override
    public void execute() {
        playlist = musicLibrary.createAutomaticPlaylist(name, genres, years, tags);
    }

    @Override
    public void undo() {
        musicLibrary.removePlaylist(playlist);
    }
}
