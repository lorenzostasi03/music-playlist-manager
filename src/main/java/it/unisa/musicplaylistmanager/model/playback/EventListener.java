package it.unisa.musicplaylistmanager.model.playback;

/**
 * Rappresenta un oggetto che può ricevere notifiche relative agli eventi di
 * riproduzione.
 */
public interface EventListener {

	/**
	 * Gestisce la ricezione di un evento.
	 *
	 * @param eventType
	 *            tipo di evento ricevuto
	 */
	void update(EventType eventType);
}
