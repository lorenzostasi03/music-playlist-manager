package it.unisa.musicplaylistmanager.controller.command;

import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.library.MusicLibrary;
import it.unisa.musicplaylistmanager.model.playback.playable.SongPlayable;
import it.unisa.musicplaylistmanager.model.playback.player.Player;

/**
 * Comando concreto per rimuovere un brano dal catalogo.
 * Il brano viene rimosso anche dalla coda di riproduzione se presente.
 */
public class RemoveSongFromCatalogCommand implements Command{
    private final MusicLibrary musicLibrary;
    private final Player player;
    private final Song song;

    public RemoveSongFromCatalogCommand(MusicLibrary musicLibrary, Player player, Song song) {
        if (musicLibrary == null) throw new IllegalArgumentException("MusicLibrary non può essere null!");
        if (player == null) throw new IllegalArgumentException("Player non può essere null!");

        this.musicLibrary = musicLibrary;
        this.player = player;
        this.song = song;
    }

    @Override
    public void execute() {
        musicLibrary.removeSongFromCatalog(song);
        player.removeFromQueue(new SongPlayable(song));
    }

    @Override
    public void undo() { musicLibrary.addSongToCatalog(song); }
}
