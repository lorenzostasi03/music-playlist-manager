package it.unisa.musicplaylistmanager.model.playback;

/**
 * Gestisce la riproduzione corrente dell'applicazione.
 *
 * <p>
 * Il player mantiene il riferimento all'oggetto riproducibile attualmente in
 * esecuzione e coordina le operazioni di avvio, pausa, ripresa e interruzione
 * della riproduzione.
 */
public class Player implements EventListener {

	private Playable currentPlayable;
	private PlayerState state;

	/**
	 * Crea un nuovo player senza alcun oggetto in riproduzione.
	 */
	public Player() {
		this.currentPlayable = null;
		this.state = PlayerState.STOPPED;
	}

	/**
	 * Avvia la riproduzione dell'oggetto specificato.
	 *
	 * <p>
	 * Se un altro oggetto è già in riproduzione, questo viene interrotto prima di
	 * avviare il nuovo oggetto. Il player si registra inoltre agli eventi di
	 * completamento del nuovo oggetto riproducibile.
	 *
	 * @param playable
	 *            oggetto da riprodurre
	 * @throws IllegalArgumentException
	 *             se l'oggetto riproducibile è {@code null}
	 */
	public void play(Playable playable) {
		if (playable == null) {
			throw new IllegalArgumentException("Playable cannot be null.");
		}

		if (currentPlayable != null) {
			currentPlayable.getEvents().unsubscribe(EventType.PLAYABLE_COMPLETED, this);
			currentPlayable.stop();
		}

		currentPlayable = playable;
		currentPlayable.getEvents().subscribe(EventType.PLAYABLE_COMPLETED, this);
		currentPlayable.play();
		state = PlayerState.PLAYING;
	}

	/**
	 * Mette in pausa la riproduzione corrente, se il player è in stato
	 * {@link PlayerState#PLAYING}.
	 */
	public void pause() {
		if (currentPlayable != null && state == PlayerState.PLAYING) {
			currentPlayable.pause();
			state = PlayerState.PAUSED;
		}
	}

	/**
	 * Riprende la riproduzione corrente, se il player è in stato
	 * {@link PlayerState#PAUSED}.
	 */
	public void resume() {
		if (currentPlayable != null && state == PlayerState.PAUSED) {
			currentPlayable.resume();
			state = PlayerState.PLAYING;
		}
	}

	/**
	 * Interrompe la riproduzione corrente e rimuove il riferimento all'oggetto
	 * riproducibile.
	 */
	public void stop() {
		if (currentPlayable != null) {
			currentPlayable.stop();
			currentPlayable.getEvents().unsubscribe(EventType.PLAYABLE_COMPLETED, this);
			currentPlayable = null;
		}

		state = PlayerState.STOPPED;
	}

	/**
	 * Restituisce lo stato corrente del player.
	 *
	 * @return stato corrente del player
	 */
	public PlayerState getState() {
		return state;
	}

	/**
	 * Restituisce l'oggetto attualmente gestito dal player.
	 *
	 * @return oggetto riproducibile corrente, oppure {@code null} se non è presente
	 *         alcuna riproduzione
	 */
	public Playable getCurrentPlayable() {
		return currentPlayable;
	}

	/**
	 * Gestisce gli eventi ricevuti dall'oggetto riproducibile corrente.
	 *
	 * @param eventType
	 *            tipo di evento ricevuto
	 */
	@Override
	public void update(EventType eventType) {
		if (eventType == EventType.PLAYABLE_COMPLETED) {
			stop();
		}
	}
}
