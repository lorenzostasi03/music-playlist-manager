package it.unisa.musicplaylistmanager.model.playback;

import it.unisa.musicplaylistmanager.app.AppContext;
import it.unisa.musicplaylistmanager.model.entity.Song;

/**
 * Adatta una singola traccia al concetto di oggetto riproducibile.
 *
 * <p>
 * La classe utilizza {@link AudioPlayer} per riprodurre il file audio associato
 * alla traccia e traduce l'evento di completamento audio in un evento di
 * completamento del riproducibile.
 */
public class SongPlayable extends Playable {

	private final Song song;
	private final AudioPlayer audioPlayer;
	private boolean subscribed;

	/**
	 * Crea un nuovo oggetto riproducibile a partire da una traccia.
	 *
	 * @param song
	 *            traccia da rendere riproducibile
	 * @throws IllegalArgumentException
	 *             se la traccia è {@code null}
	 */
	public SongPlayable(Song song) {
		if (song == null) {
			throw new IllegalArgumentException("Song cannot be null.");
		}

		this.song = song;
		this.audioPlayer = AudioPlayer.getInstance();
		this.subscribed = false;
	}

	/**
	 * Avvia la riproduzione della traccia.
	 *
	 * <p>
	 * L'oggetto si registra agli eventi di completamento audio prima di avviare la
	 * riproduzione, così da poter notificare il completamento del riproducibile.
	 */
	@Override
	public void play() {
		subscribeToAudioCompleted();
		audioPlayer.play(song.getFilePath());
        updatePlayCount();
	}

	/**
	 * Mette in pausa la riproduzione della traccia.
	 */
	@Override
	public void pause() {
		audioPlayer.pause();
	}

	/**
	 * Riprende la riproduzione della traccia.
	 */
	@Override
	public void resume() {
		audioPlayer.resume();
	}

	/**
	 * Interrompe la riproduzione della traccia e rimuove l'iscrizione agli eventi
	 * di completamento audio.
	 */
	@Override
	public void stop() {
		audioPlayer.stop();
		unsubscribeFromAudioCompleted();
	}

	/**
	 * Restituisce la traccia associata a questo oggetto riproducibile.
	 *
	 * @return traccia corrente
	 */
	@Override
	public Song getCurrentSong() {
		return song;
	}

    @Override
    protected void updatePlayCount() {
        song.incrementPlayCount();
        AppContext.getInstance().getMusicLibrary().updateSongPlayCount(song);
    }

    /**
	 * Gestisce gli eventi ricevuti dal player audio.
	 *
	 * <p>
	 * Quando il file audio termina, l'oggetto si disiscrive dagli eventi audio e
	 * notifica il completamento del riproducibile.
	 *
	 * @param eventType
	 *            tipo di evento ricevuto
	 */
	@Override
	public void update(EventType eventType) {
		if (eventType == EventType.AUDIO_COMPLETED) {
			unsubscribeFromAudioCompleted();
			getEvents().notifyListeners(EventType.PLAYABLE_COMPLETED);
		}
	}

	/**
	 * Registra questo oggetto come listener degli eventi di completamento audio, se
	 * non è già registrato.
	 */
	private void subscribeToAudioCompleted() {
		if (!subscribed) {
			audioPlayer.getEvents().subscribe(EventType.AUDIO_COMPLETED, this);
			subscribed = true;
		}
	}

	/**
	 * Rimuove questo oggetto dai listener degli eventi di completamento audio, se
	 * risulta registrato.
	 */
	private void unsubscribeFromAudioCompleted() {
		if (subscribed) {
			audioPlayer.getEvents().unsubscribe(EventType.AUDIO_COMPLETED, this);
			subscribed = false;
		}
	}
}
