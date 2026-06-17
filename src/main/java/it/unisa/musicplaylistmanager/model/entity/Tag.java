package it.unisa.musicplaylistmanager.model.entity;
/**
 * Enumerazione dei tag associabili a una traccia musicale.
 */
public enum Tag {

	FAVOURITE("Preferito"),
    EXPLICIT("Esplicito"),
    NEW_RELEASE("Nuova uscita");

	private final String label;

	Tag(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
