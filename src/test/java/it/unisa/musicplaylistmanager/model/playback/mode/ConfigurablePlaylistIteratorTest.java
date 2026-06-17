package it.unisa.musicplaylistmanager.model.playback.mode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import org.junit.jupiter.api.Test;

class ConfigurablePlaylistIteratorTest {

	@Test
	void changeSongPosition() {
		Playlist playlist = new Playlist("Playlist test");
		Song firstSong = new Song("Prima", "Autore", Genre.POP, 2024, 180, "/prima.mp3");
		Song secondSong = new Song("Seconda", "Autore", Genre.ROCK, 2024, 200, "/seconda.mp3");
		Song thirdSong = new Song("Terza", "Autore", Genre.JAZZ, 2024, 210, "/terza.mp3");

		playlist.addSong(firstSong);
		playlist.addSong(secondSong);
		playlist.addSong(thirdSong);

		PlaylistIterator iterator = new ConfigurablePlaylistIterator(playlist, new SequentialIteratorStrategy());

		assertEquals(firstSong, iterator.next());

		playlist.moveSong(secondSong, 0);

		assertEquals(thirdSong, iterator.next());
	}
}
