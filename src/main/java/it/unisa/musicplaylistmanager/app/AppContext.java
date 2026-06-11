package it.unisa.musicplaylistmanager.app;

import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.library.MusicLibrary;
import it.unisa.musicplaylistmanager.model.playback.playable.Playable;
import it.unisa.musicplaylistmanager.model.playback.player.Player;
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

	private AppContext() {
		musicLibrary = new MusicLibrary(new SQLiteSongDAO(DatabaseConfig.DB_URL),
				new SQLitePlaylistDAO(DatabaseConfig.DB_URL));
		musicLibrary.init();

		player = new Player();
	}

	public static AppContext getInstance() {
		if (instance == null) {
			instance = new AppContext();
		}

		return instance;
	}

	public MusicLibrary getMusicLibrary() {
		return musicLibrary;
	}

	public Player getPlayer() {
		return player;
	}

	public Playlist getSelectedPlaylist() {
		return selectedPlaylist;
	}

	public void setSelectedPlaylist(Playlist playlist) {
		selectedPlaylist = playlist;
	}

    public boolean isSelectedPlaylistReadOnly() { return selectedPlaylistReadOnly; }
    public void setSelectedPlaylistReadOnly(boolean readOnly) { this.selectedPlaylistReadOnly = readOnly; }

	public Playable getCurrentPlayable() {
		return player.getCurrentPlayable();
	}

	public void playPlayable(Playable playable) {
		player.play(playable);
	}

	public void enqueuePlayable(Playable playable) {
		player.enqueue(playable);
	}

	public void clearPlaybackQueue() {
		player.clearQueue();
	}

}
