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

	public PlaylistCriteria getCriteria() {
		return criteria;
	}

	public boolean matches(Song song) {
		return criteria.matches(song);
	}

	private static PlaylistCriteria requireCriteria(PlaylistCriteria criteria) {

		if (criteria == null) {
			throw new IllegalArgumentException("I criteri non possono essere null.");
		}

		return criteria;
	}
}
