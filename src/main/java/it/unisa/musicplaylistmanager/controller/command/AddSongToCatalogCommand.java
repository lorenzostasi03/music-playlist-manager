package it.unisa.musicplaylistmanager.controller.command;

import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.library.MusicLibrary;

/**
 * Comando concreto per aggiungere un brano al catalogo.
 */
public class AddSongToCatalogCommand implements Command {
	private final MusicLibrary musicLibrary;
	private final Song song;

	public AddSongToCatalogCommand(MusicLibrary musicLibrary, Song song) {
		if (musicLibrary == null)
			throw new IllegalArgumentException("MusicLibrary non può essere null!");

		this.musicLibrary = musicLibrary;
		this.song = song;
	}

	@Override
	public void execute() {
		musicLibrary.addSongToCatalog(song);
	}

	@Override
	public void undo() {
		musicLibrary.removeSongFromCatalog(song);
	}
}
