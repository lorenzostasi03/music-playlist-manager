package it.unisa.musicplaylistmanager.model.playback;

import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;

/**
 * Adatta una playlist al sistema di riproduzione.
 */
public class PlaylistPlayable extends Playable {

	private final Playlist playlist;
	private final AudioPlayer audioPlayer;
	private final ConfigurablePlaylistIterator iterator;
	private PlaybackMode playbackMode;
	private boolean subscribed;

	/**
	 * Crea un nuovo oggetto riproducibile a partire da una playlist.
	 *
	 * @param playlist
	 *            playlist da rendere riproducibile
	 * @throws IllegalArgumentException
	 *             se la playlist e' {@code null}
	 */
	public PlaylistPlayable(Playlist playlist) {
		if (playlist == null) {
			throw new IllegalArgumentException("Playlist cannot be null.");
		}

		this.playlist = playlist;
		this.audioPlayer = AudioPlayer.getInstance();
		this.iterator = (ConfigurablePlaylistIterator) playlist.createIterator();
		this.playbackMode = PlaybackMode.SEQUENTIAL;
		this.subscribed = false;
	}

	/**
	 * Avvia la riproduzione della playlist dalla traccia corrente.
	 */
	@Override
	public void play() {
		if (playlist.isEmpty()) {
			getEvents().notifyListeners(EventType.PLAYABLE_COMPLETED);
			return;
		}

		playCurrentSong();
	}

	/**
	 * Mette in pausa la riproduzione della traccia corrente.
	 */
	@Override
	public void pause() {
		audioPlayer.pause();
	}

	/**
	 * Riprende la riproduzione della traccia corrente.
	 */
	@Override
	public void resume() {
		audioPlayer.resume();
	}

	/**
	 * Interrompe la riproduzione della playlist e rimuove l'iscrizione agli eventi
	 * audio.
	 */
	@Override
	public void stop() {
		audioPlayer.setLoopMode(false);
		audioPlayer.stop();
		unsubscribeFromAudioCompleted();
	}

	/**
	 * Restituisce la traccia attualmente selezionata nella playlist.
	 *
	 * @return traccia corrente, oppure {@code null} se la playlist e' vuota
	 */
	@Override
	public Song getCurrentSong() {
		return iterator.getCurrentSong();
	}

	/**
	 * Passa manualmente alla traccia successiva rispettando la modalita' corrente.
	 */
	@Override
	public void skipNext() {
		if (playlist.isEmpty()) {
			completePlaylist();
			return;
		}

		playNextOrComplete();
	}

	/**
	 * Passa manualmente alla traccia precedente rispettando la modalita' corrente.
	 */
	@Override
	public void skipPrevious() {
		if (playlist.isEmpty()) {
			return;
		}

		iterator.previous(playbackMode == PlaybackMode.LOOP_PLAYLIST);
		playCurrentSong();
	}

	/**
	 * Imposta la modalita' di riproduzione della playlist.
	 *
	 * @param playbackMode
	 *            modalita' da applicare
	 * @throws IllegalArgumentException
	 *             se la modalita' e' {@code null}
	 */
	@Override
	public void setPlaybackMode(PlaybackMode playbackMode) {
		if (playbackMode == null) {
			throw new IllegalArgumentException("Playback mode cannot be null.");
		}

		this.playbackMode = playbackMode;
		setIteratorStrategy(strategyFor(playbackMode));
		audioPlayer.setLoopMode(playbackMode == PlaybackMode.LOOP_TRACK);
	}

	/**
	 * Cambia la strategia usata dall'iteratore della playlist.
	 *
	 * @param strategy
	 *            strategia da applicare
	 */
	public void setIteratorStrategy(PlaylistIteratorStrategy strategy) {
		iterator.setStrategy(strategy);
	}

	/**
	 * Restituisce la modalita' di riproduzione corrente.
	 *
	 * @return modalita' corrente
	 */
	@Override
	public PlaybackMode getPlaybackMode() {
		return playbackMode;
	}

	/**
	 * Gestisce il completamento della traccia corrente e decide la traccia
	 * successiva in base alla modalita' impostata.
	 *
	 * @param eventType
	 *            tipo di evento ricevuto
	 */
	@Override
	public void update(EventType eventType) {
		if (eventType != EventType.AUDIO_COMPLETED) {
			return;
		}

		if (playbackMode == PlaybackMode.LOOP_TRACK) {
			return;
		}

		playNextOrComplete();
	}

	/**
	 * Avvia la traccia corrente. Il metodo e' protetto per poter controllare la
	 * logica della playlist nei test senza aprire file audio reali.
	 */
	protected void playCurrentSong() {
		subscribeToAudioCompleted();
		audioPlayer.setLoopMode(playbackMode == PlaybackMode.LOOP_TRACK);
		audioPlayer.play(getCurrentSong().getFilePath());
	}

	private void playNextOrComplete() {
		if (iterator.hasNext()) {
			iterator.next();
			playCurrentSong();
		} else {
			completePlaylist();
		}
	}

	private PlaylistIteratorStrategy strategyFor(PlaybackMode playbackMode) {
		if (playbackMode == PlaybackMode.LOOP_PLAYLIST) {
			return new LoopIteratorStrategy();
		}

		return new SequentialIteratorStrategy();
	}

	private void completePlaylist() {
		audioPlayer.setLoopMode(false);
		unsubscribeFromAudioCompleted();
		getEvents().notifyListeners(EventType.PLAYABLE_COMPLETED);
	}

	private void subscribeToAudioCompleted() {
		if (!subscribed) {
			audioPlayer.getEvents().subscribe(EventType.AUDIO_COMPLETED, this);
			subscribed = true;
		}
	}

	private void unsubscribeFromAudioCompleted() {
		if (subscribed) {
			audioPlayer.getEvents().unsubscribe(EventType.AUDIO_COMPLETED, this);
			subscribed = false;
		}
	}
}
