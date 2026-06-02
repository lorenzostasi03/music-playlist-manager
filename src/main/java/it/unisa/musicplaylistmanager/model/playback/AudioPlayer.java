package it.unisa.musicplaylistmanager.model.playback;

import java.io.File;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Gestisce la riproduzione effettiva dei file audio.
 *
 * <p>
 * La classe è implementata come Singleton, in modo da avere un solo componente
 * responsabile della riproduzione audio nell'applicazione.
 */
public class AudioPlayer {

	private static AudioPlayer instance;

	private final EventManager events;
	private MediaPlayer mediaPlayer;

	/**
	 * Crea un nuovo player audio.
	 *
	 * <p>
	 * Il costruttore è privato perché la classe segue il pattern Singleton.
	 */
	private AudioPlayer() {
		this.events = new EventManager();
	}

	/**
	 * Restituisce l'unica istanza di {@code AudioPlayer}.
	 *
	 * @return istanza singleton di {@code AudioPlayer}
	 */
	public static AudioPlayer getInstance() {
		if (instance == null) {
			instance = new AudioPlayer();
		}

		return instance;
	}

	/**
	 * Restituisce il gestore degli eventi associato al player audio.
	 *
	 * @return event manager usato per notificare gli eventi audio
	 */
	public EventManager getEvents() {
		return events;
	}

	/**
	 * Avvia la riproduzione del file audio specificato.
	 *
	 * <p>
	 * Se un altro file è già in riproduzione, questo viene interrotto prima
	 * dell'avvio del nuovo file. Al termine della riproduzione viene notificato
	 * l'evento {@link EventType#AUDIO_COMPLETED}.
	 *
	 * @param filePath
	 *            percorso del file audio da riprodurre
	 * @throws IllegalArgumentException
	 *             se il percorso è {@code null}, vuoto o se il file non esiste
	 */
	public void play(String filePath) {
		if (filePath == null || filePath.trim().isEmpty()) {
			throw new IllegalArgumentException("File path cannot be null or empty.");
		}

		stop();

		File audioFile = new File(filePath);

		if (!audioFile.exists()) {
			throw new IllegalArgumentException("Audio file does not exist: " + filePath);
		}

		Media media = new Media(audioFile.toURI().toString());
		mediaPlayer = new MediaPlayer(media);

		mediaPlayer.setOnEndOfMedia(() -> events.notifyListeners(EventType.AUDIO_COMPLETED));

		mediaPlayer.play();
	}

	/**
	 * Mette in pausa la riproduzione corrente, se presente.
	 */
	public void pause() {
		if (mediaPlayer != null) {
			mediaPlayer.pause();
		}
	}

	/**
	 * Riprende la riproduzione corrente, se presente.
	 */
	public void resume() {
		if (mediaPlayer != null) {
			mediaPlayer.play();
		}
	}

	/**
	 * Interrompe la riproduzione corrente e rilascia le risorse associate.
	 */
	public void stop() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.dispose();
			mediaPlayer = null;
		}
	}
}
