package it.unisa.musicplaylistmanager.app;

import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.library.MusicLibrary;
import it.unisa.musicplaylistmanager.model.playback.Playable;
import it.unisa.musicplaylistmanager.model.playback.Player;
import it.unisa.musicplaylistmanager.persistence.sqlite.DatabaseConfig;
import it.unisa.musicplaylistmanager.persistence.sqlite.SQLitePlaylistDAO;
import it.unisa.musicplaylistmanager.persistence.sqlite.SQLiteSongDAO;

/**
 * Contesto globale dell'applicazione.
 *
 * <p>
 * Questa classe fornisce un punto di accesso centralizzato ai componenti
 * condivisi dell'applicazione, come la libreria musicale e il player audio.
 * </p>
 *
 * <p>
 * Il contesto è implementato come singleton e viene inizializzato una sola
 * volta durante il ciclo di vita dell'applicazione.
 * </p>
 */
public class AppContext {
	private static AppContext instance;

	private final MusicLibrary musicLibrary;
	private final Player player;

	private Playlist selectedPlaylist;
    private boolean selectedPlaylistReadOnly;

	private Playable currentPlayable;

	private AppContext() {
		musicLibrary = new MusicLibrary(new SQLiteSongDAO(DatabaseConfig.DB_URL),
				new SQLitePlaylistDAO(DatabaseConfig.DB_URL));
		musicLibrary.init();
		player = new Player();
	}

	public static AppContext getInstance() {
		if (instance == null)
			instance = new AppContext();

		return instance;
	}

	public MusicLibrary getMusicLibrary() {
		return this.musicLibrary;
	}
	public Player getPlayer() {
		return this.player;
	}

	public Playlist getSelectedPlaylist() {
		return this.selectedPlaylist;
	}
	public void setSelectedPlaylist(Playlist playlist) {
		this.selectedPlaylist = playlist;
	}

    public boolean isSelectedPlaylistReadOnly() { return selectedPlaylistReadOnly; }
    public void setSelectedPlaylistReadOnly(boolean readOnly) { this.selectedPlaylistReadOnly = readOnly; }

	public Playable getCurrentPlayable() {
		return currentPlayable;
	}

	public void setCurrentPlayable(Playable playable) {
		this.currentPlayable = playable;
	}

	public void playPlayable(Playable playable) {
		if (playable == null) {
			throw new IllegalArgumentException("Playable cannot be null.");
		}

		currentPlayable = playable;
		player.play(playable);
	}
}
