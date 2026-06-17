package it.unisa.musicplaylistmanager.model.playback.playable;

import static org.junit.jupiter.api.Assertions.*;

import it.unisa.musicplaylistmanager.model.entity.Genre;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.playback.mode.PlaybackMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test della classe {@link PlaylistPlayable}.
 */
class PlaylistPlayableTest {

	private Song song;
	private Playlist playlist;
	private PlaylistPlayable playable;

	@BeforeEach
	void setUp() {
		song = new Song("Immigrant song", "Led Zeppelin", Genre.ROCK, 1970, 259, "/immigrant-song.mp3");

		playlist = new Playlist("Mia playlist");
		playlist.addSong(song);

		playable = new PlaylistPlayable(playlist);
	}

	@Test
	void costruttoreConPlaylistNullLanciaEccezione() {
		assertThrows(IllegalArgumentException.class, () -> new PlaylistPlayable(null));
	}

	@Test
	void costruttoreConPlaylistVuotaLanciaEccezione() {
		Playlist emptyPlaylist = new Playlist("Playlist vuota");

		assertThrows(IllegalArgumentException.class, () -> new PlaylistPlayable(emptyPlaylist));
	}

	@Test
	void modalitaPredefinitaSequenziale() {
		assertEquals(PlaybackMode.SEQUENTIAL, playable.getPlaybackMode());
	}

	@Test
	void modificaModalitaDiRiproduzione() {
		playable.setPlaybackMode(PlaybackMode.LOOP);
		assertEquals(PlaybackMode.LOOP, playable.getPlaybackMode());

		playable.setPlaybackMode(PlaybackMode.SHUFFLE);
		assertEquals(PlaybackMode.SHUFFLE, playable.getPlaybackMode());

		playable.setPlaybackMode(PlaybackMode.SEQUENTIAL);
		assertEquals(PlaybackMode.SEQUENTIAL, playable.getPlaybackMode());
	}

	@Test
	void impostazioneModalitaNullLanciaEccezione() {
		assertThrows(IllegalArgumentException.class, () -> playable.setPlaybackMode(null));
	}

	@Test
	void impostazioneStrategiaNullLanciaEccezione() {
		assertThrows(IllegalArgumentException.class, () -> playable.setIteratorStrategy(null));
	}
}
