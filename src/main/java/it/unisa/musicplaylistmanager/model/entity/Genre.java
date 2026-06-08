package it.unisa.musicplaylistmanager.model.entity;
/**
 * Enumerazione dei generi musicali supportati dall'applicazione.
 */
public enum Genre {
	POP("Pop"), ROCK("Rock"), HIP_HOP("Hip-Hop"), JAZZ("Jazz"), CLASSICAL("Classical"), ELECTRONIC("Electronic"), RNB(
			"R&B"), COUNTRY("Country"), METAL(
					"Metal"), INDIE("Indie"), FOLK("Folk"), REGGAE("Reggae"), BLUES("Blues"), ALTRO("Altro");

	private final String label;

	Genre(String label) {
		this.label = label;
	}

	public static Genre fromLabel(String value) {
		if (value == null) {
			throw new IllegalArgumentException("Genere nullo");
		}

		for (Genre g : values()) {
			if (g.label.equals(value)) {
				return g;
			}
		}

		throw new IllegalArgumentException("Genere non valido: " + value);
	}

	public String getLabel() {
		return label;
	}
}
