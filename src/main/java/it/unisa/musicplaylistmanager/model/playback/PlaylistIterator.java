package it.unisa.musicplaylistmanager.model.playback;

import it.unisa.musicplaylistmanager.model.entity.Song;

/**
 * Iteratore per la riproduzione dei brani di una playlist.
 */
public interface PlaylistIterator {

	Song next();

	void setStrategy(PlaylistIteratorStrategy strategy);

	int getCurrentIndex();
}