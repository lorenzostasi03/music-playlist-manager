package it.unisa.musicplaylistmanager.model.entity;

import java.util.UUID;

/**
 * Playlist la cui composizione viene determinata automaticamente attraverso
 * criteri persistenti.
 */
public class AutomaticPlaylist extends Playlist {

	private final PlaylistCriteria criteria;

	/**
	 * Crea una nuova playlist automatica.
	 *
	 * @param name
	 *            nome della playlist
	 * @param criteria
	 *            criteri di appartenenza
	 */
	public AutomaticPlaylist(String name, PlaylistCriteria criteria) {

		super(name);
		this.criteria = requireCriteria(criteria);
	}

	/**
	 * Ricostruisce una playlist automatica già persistita.
	 *
	 * @param id
	 *            identificatore della playlist
	 * @param name
	 *            nome della playlist
	 * @param playCount
	 *            numero di riproduzioni
	 * @param criteria
	 *            criteri di appartenenza
	 */
	public AutomaticPlaylist(UUID id, String name, int playCount, PlaylistCriteria criteria) {

		super(id, name, playCount);
		this.criteria = requireCriteria(criteria);
	}

	/**
	 * Restituisce i criteri che determinano la composizione della playlist.
	 *
	 * @return criteri della playlist automatica
	 */
	public PlaylistCriteria getCriteria() {
		return criteria;
	}

	/**
	 * Verifica se una traccia soddisfa almeno uno dei criteri della playlist.
	 *
	 * @param song
	 *            traccia da verificare
	 * @return {@code true} se la traccia è compatibile con la playlist
	 */
	public boolean matches(Song song) {
		return criteria.matches(song);
	}

	/**
	 * Verifica che i criteri della playlist siano presenti.
	 *
	 * @param criteria
	 *            criteri da validare
	 * @return gli stessi criteri ricevuti
	 * @throws IllegalArgumentException
	 *             se i criteri sono {@code null}
	 */
	private static PlaylistCriteria requireCriteria(PlaylistCriteria criteria) {
		if (criteria == null) {
			throw new IllegalArgumentException("I criteri non possono essere null.");
		}

		return criteria;
	}
}
