package it.unisa.musicplaylistmanager.model.entity;
/**
 * Enumerazione dei generi musicali supportati dall'applicazione.
 */
public enum Genre {
    POP("Pop"),
    ROCK("Rock"),
    HIP_HOP("Hip-Hop"),
    JAZZ("Jazz"),
    CLASSICAL("Classical"),
    ELECTRONIC("Electronic"),
    RNB("R&B"),
    COUNTRY("Country"),
    METAL("Metal"),
    INDIE("Indie"),
    FOLK("Folk"),
    REGGAE("Reggae"),
    BLUES("Blues"),
    ALTRO("Altro");

    private final String displayName;

    Genre(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
