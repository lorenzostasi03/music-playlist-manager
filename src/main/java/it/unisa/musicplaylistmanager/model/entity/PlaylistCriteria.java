package it.unisa.musicplaylistmanager.model.entity;

import java.util.Set;

/**
 * Rappresenta i criteri di appartenenza di una playlist automatica.
 *
 * <p>
 * I criteri vengono combinati mediante OR: una traccia è compatibile se
 * soddisfa almeno uno dei generi, degli anni o dei tag selezionati.
 */
public record PlaylistCriteria(Set<Genre> genres, Set<Integer> years, Set<Tag> tags) {

	public PlaylistCriteria {
		if (genres == null || years == null || tags == null) {
			throw new IllegalArgumentException("I criteri non possono essere null.");
		}

		genres = Set.copyOf(genres);
		years = Set.copyOf(years);
		tags = Set.copyOf(tags);

		if (genres.isEmpty() && years.isEmpty() && tags.isEmpty()) {
			throw new IllegalArgumentException("Selezionare almeno un criterio.");
		}
	}

	/**
	 * Verifica se una traccia soddisfa almeno uno dei criteri.
	 *
	 * @param song
	 *            traccia da verificare
	 * @return {@code true} se la traccia è compatibile
	 */
	public boolean matches(Song song) {
		if (song == null) {
			return false;
		}

		return genres.contains(song.getGenre()) || years.contains(song.getYear())
				|| tags.stream().anyMatch(song::hasTag);
	}
}
