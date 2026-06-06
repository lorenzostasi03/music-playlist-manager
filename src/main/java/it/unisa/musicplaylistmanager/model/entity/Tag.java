package it.unisa.musicplaylistmanager.model.entity;
/**
 * Enumerazione dei tag associabili a una traccia musicale.
 */
public enum Tag {

    FAVOURITE("Preferito"),
    EXPLICIT("Esplicito"),
    NEW_RELEASE("Nuova uscita");

    private final String displayName;

    Tag(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
