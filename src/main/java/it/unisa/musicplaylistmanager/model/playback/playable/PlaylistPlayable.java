package it.unisa.musicplaylistmanager.model.playback.playable;

import it.unisa.musicplaylistmanager.app.AppContext;
import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.playback.events.EventType;
import it.unisa.musicplaylistmanager.model.playback.mode.*;
import it.unisa.musicplaylistmanager.model.playback.player.AudioPlayer;
import java.util.Objects;

/**
 * Adatta una playlist a un playable.
 *
 * <p>
 * La classe riproduce i brani della playlist secondo una strategia
 * configurabile e notifica il cambiamento del brano corrente e il completamento
 * della riproduzione.
 * </p>
 */
public class PlaylistPlayable extends Playable {

	private final Playlist playlist;
	private PlaybackMode playbackMode;
	private final AudioPlayer audioPlayer;

	private PlaylistIterator iterator;
	private PlaylistIteratorStrategy iteratorStrategy;
	private Song currentSong;
	private boolean subscribed;

	/**
	 * Crea un playable associato alla playlist specificata.
	 *
	 * @param playlist
	 *            playlist da riprodurre
	 * @throws IllegalArgumentException
	 *             se la playlist è {@code null} o vuota
	 */
	public PlaylistPlayable(Playlist playlist) {
		if (playlist == null) {
			throw new IllegalArgumentException("Playlist cannot be null.");
		}

		if (playlist.isEmpty()) {
			throw new IllegalArgumentException("Playlist cannot be empty.");
		}

		this.playlist = playlist;
		this.id = "playlist-" + playlist.getName();
		this.playbackMode = PlaybackMode.SEQUENTIAL;
		this.audioPlayer = AudioPlayer.getInstance();
		this.iteratorStrategy = new SequentialIteratorStrategy();
		this.iterator = new ConfigurablePlaylistIterator(playlist, iteratorStrategy);
		this.currentSong = null;
		this.subscribed = false;
	}

	/**
	 * Restituisce il nome della playlist.
	 *
	 * @return nome della playlist
	 */
	@Override
	public String getTitle() {
		return playlist.getName();
	}

	/**
	 * Avvia la riproduzione della playlist dal primo brano determinato dalla
	 * strategia corrente.
	 */
	@Override
	public void play() {
		subscribeToAudioPlayer();

		this.iterator = new ConfigurablePlaylistIterator(playlist, iteratorStrategy);
		playNextSong();
		updatePlayCount();
	}

	@Override
	public void pause() {
		audioPlayer.pause();
	}

	@Override
	public void resume() {
		audioPlayer.resume();
	}

	/**
	 * Interrompe la riproduzione e ripristina lo stato iniziale dell'iteratore.
	 */
	@Override
	public void stop() {
		audioPlayer.stop();
		unsubscribeFromAudioPlayer();

		this.iterator = new ConfigurablePlaylistIterator(playlist, iteratorStrategy);
		this.currentSong = null;
	}

	@Override
	public Song getCurrentSong() {
		return currentSong;
	}

	/**
	 * Incrementa il numero di riproduzioni della playlist.
	 */
	@Override
	protected void updatePlayCount() {
		playlist.incrementPlayCount();
		AppContext.getInstance().getMusicLibrary().updatePlaylistPlayCount(playlist);
	}

	/**
	 * Alla conclusione di un brano tenta di avviare quello successivo. Se non sono
	 * disponibili altri brani, notifica il completamento della playlist.
	 *
	 * @param eventType
	 *            tipo di evento ricevuto
	 */
	@Override
	public void update(EventType eventType) {
		if (eventType != EventType.AUDIO_COMPLETED) {
			return;
		}

		if (!playNextSong()) {
			unsubscribeFromAudioPlayer();
			getEvents().notifyListeners(EventType.PLAYABLE_COMPLETED);
		}
	}

	/**
	 * Imposta la modalità di riproduzione e la strategia corrispondente.
	 *
	 * @param mode
	 *            modalità di riproduzione da applicare
	 */
	@Override
	public void setPlaybackMode(PlaybackMode mode) {
		if (mode == null) {
			throw new IllegalArgumentException("Playback mode cannot be null.");
		}

		this.playbackMode = mode;

		if (mode == PlaybackMode.SEQUENTIAL) {
			setIteratorStrategy(new SequentialIteratorStrategy());
		} else if (mode == PlaybackMode.LOOP) {
			setIteratorStrategy(new LoopIteratorStrategy());
		} else if (mode == PlaybackMode.SHUFFLE) {
			setIteratorStrategy(new ShuffleIteratorStrategy());
		}
	}

	@Override
	public PlaybackMode getPlaybackMode() {
		return playbackMode;
	}

	/**
	 * Sostituisce la strategia utilizzata per selezionare il prossimo brano.
	 *
	 * @param strategy
	 *            nuova strategia di iterazione
	 */
	public void setIteratorStrategy(PlaylistIteratorStrategy strategy) {
		if (strategy == null) {
			throw new IllegalArgumentException("Playlist iterator strategy cannot be null.");
		}

		this.iteratorStrategy = strategy;

		if (iterator != null) {
			iterator.setStrategy(strategy);
		}
	}

	/**
	 * Seleziona e avvia il prossimo brano della playlist.
	 *
	 * @return {@code true} se è stato avviato un brano, {@code false} se la
	 *         riproduzione è terminata
	 */
	private boolean playNextSong() {
		currentSong = iterator.next();

		if (currentSong == null) {
			return false;
		}

		audioPlayer.play(currentSong.getFilePath());
		updateCurrentSongPlayCount();
		getEvents().notifyListeners(EventType.CURRENT_SONG_CHANGED);

		return true;
	}

	/**
	 * Passa al prossimo brano della playlist.
	 *
	 * @return {@code true} se esiste un altro brano, {@code false} altrimenti
	 */
	@Override
	public boolean skipToNextSong() {
		return playNextSong();
	}

	/**
	 * Incrementa il numero di riproduzioni del brano corrente.
	 */
	private void updateCurrentSongPlayCount() {
		currentSong.incrementPlayCount();
		AppContext.getInstance().getMusicLibrary().updateSongPlayCount(currentSong);
	}

	/**
	 * Registra il playable agli eventi di completamento dell'audio.
	 */
	private void subscribeToAudioPlayer() {
		if (!subscribed) {
			audioPlayer.getEvents().subscribe(EventType.AUDIO_COMPLETED, this);
			subscribed = true;
		}
	}

	/**
	 * Rimuove il playable dai listener del player audio.
	 */
	private void unsubscribeFromAudioPlayer() {
		if (subscribed) {
			audioPlayer.getEvents().unsubscribe(EventType.AUDIO_COMPLETED, this);
			subscribed = false;
		}
	}

	public String getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (this == o)
			return true;
		if (!(o instanceof PlaylistPlayable))
			return false;

		PlaylistPlayable p = (PlaylistPlayable) o;
		return this.id.equals(p.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return id + " " + playlist.toString();
	}
}
