package it.unisa.musicplaylistmanager.model.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di unità per la classe  Song.

 */
class SongTest {


    private Song song;

    @BeforeEach
    void setUp() {
        song = new Song("Bohemian Rhapsody", "Queen", Genre.ROCK, 1975, 354, "/music/br.mp3");
    }


    /**
     * Verifica che una traccia creata con dati validi abbia tutti
     * i campi impostati correttamente e che l'ID non sia nullo.
     */
    @Test
    void testCreazioneTrAcciaDatiValidi() {
        assertNotNull(song.getId(), "L'ID non deve essere null");
        assertEquals("Bohemian Rhapsody", song.getTitle());
        assertEquals("Queen", song.getAuthor());
        assertEquals(Genre.ROCK, song.getGenre());
        assertEquals(1975, song.getYear());
        assertEquals(354, song.getDuration());
        assertEquals("/music/br.mp3", song.getFilePath());
        assertEquals(0, song.getPlayCount());
        assertTrue(song.getTags().isEmpty());
    }

    /**
     * Verifica che due tracce create separatamente abbiano ID diversi.
     */
    @Test
    void testIdUnicoPerOgniTraccia() {
        Song altra = new Song("Another Song", "Artist", Genre.POP, 2020, 200, "/path.mp3");
        assertNotEquals(song.getId(), altra.getId());
    }

    /**
     * Verifica che la creazione di una traccia con titolo vuoto
     * lanci {@link IllegalArgumentException}.
     */
    @Test
    void testTitoloVuotoLanciaEccezione() {
        assertThrows(IllegalArgumentException.class,
            () -> new Song("", "Queen", Genre.ROCK, 1975, 354, "/path.mp3"));
    }

    /**
     * Verifica che la creazione di una traccia con titolo null
     * lanci {@link IllegalArgumentException}.
     */
    @Test
    void testTitoloNullLanciaEccezione() {
        assertThrows(IllegalArgumentException.class,
            () -> new Song(null, "Queen", Genre.ROCK, 1975, 354, "/path.mp3"));
    }

    /**
     * Verifica che la creazione di una traccia con autore vuoto
     * lanci {@link IllegalArgumentException}.
     */
    @Test
    void testAutoreVuotoLanciaEccezione() {
        assertThrows(IllegalArgumentException.class,
            () -> new Song("Titolo", "", Genre.ROCK, 1975, 354, "/path.mp3"));
    }

    /**
     * Verifica che un anno pari a zero non sia accettato.
     */
    @Test
    void testAnnoZeroLanciaEccezione() {
        assertThrows(IllegalArgumentException.class,
            () -> new Song("Titolo", "Autore", Genre.POP, 0, 200, "/path.mp3"));
    }

    /**
     * Verifica che un anno futuro  non sia accettato.
     */
    @Test
    void testAnnoFuturoLanciaEccezione() {
        int annoFuturo = java.time.Year.now().getValue() + 5;
        assertThrows(IllegalArgumentException.class,
            () -> new Song("Titolo", "Autore", Genre.POP, annoFuturo, 200, "/path.mp3"));
    }

    /**
     * Verifica che una durata negativa non sia accettata.
     */
    @Test
    void testDurataNegativaLanciaEccezione() {
        assertThrows(IllegalArgumentException.class,
            () -> new Song("Titolo", "Autore", Genre.POP, 2000, -1, "/path.mp3"));
    }


    /**
     * Verifica che il setter del titolo aggiorni correttamente il campo.
     */
    @Test
    void testSetTitoloValido() {
        song.setTitle("  We Will Rock You  ");
        assertEquals("We Will Rock You", song.getTitle(), "Il titolo deve essere trimmato");
    }

    /**
     * Verifica che impostare un titolo vuoto lanci un'eccezione.
     */
    @Test
    void testSetTitoloVuotoLanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> song.setTitle("   "));
    }

    /**
     * Verifica che il setter dell'autore aggiorni correttamente il campo.
     */
    @Test
    void testSetAutoreValido() {
        song.setAuthor("David Bowie");
        assertEquals("David Bowie", song.getAuthor());
    }

    /**
     * Verifica che il setter del genere aggiorni correttamente il campo.
     */
    @Test
    void testSetGenere() {
        song.setGenre(Genre.JAZZ);
        assertEquals(Genre.JAZZ, song.getGenre());
    }

    /**
     * Verifica che il setter dell'anno accetti un valore valido.
     */
    @Test
    void testSetAnnoValido() {
        song.setYear(2000);
        assertEquals(2000, song.getYear());
    }

    /**
     * Verifica che il setter della durata accetti un valore non negativo.
     */
    @Test
    void testSetDurataValida() {
        song.setDuration(0);
        assertEquals(0, song.getDuration());
    }

    /**
     * Verifica che sia possibile aggiungere un tag alla traccia.
     */
    @Test
    void testAddTag() {
        song.addTag(Tag.FAVOURITE);
        assertTrue(song.hasTag(Tag.FAVOURITE));
    }

    /**
     * Verifica che aggiungere lo stesso tag due volte non produca duplicati.
     */
    @Test
    void testAddTagDoppio() {
        song.addTag(Tag.FAVOURITE);
        song.addTag(Tag.FAVOURITE);
        assertEquals(1, song.getTags().size());
    }

    /**
     * Verifica che sia possibile rimuovere un tag presente.
     */
    @Test
    void testRemoveTag() {
        song.addTag(Tag.EXPLICIT);
        song.removeTag(Tag.EXPLICIT);
        assertFalse(song.hasTag(Tag.EXPLICIT));
    }

    /**
     * Verifica che aggiungere un tag null lanci eccezione.
     */
    @Test
    void testAddTagNullLanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> song.addTag(null));
    }

    /**
     * Verifica che rimuovere un tag null lanci eccezione.
     */
    @Test
    @DisplayName("Rimozione tag null deve lanciare eccezione")
    void testRemoveTagNullLanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> song.removeTag(null));
    }


    /**
     * Controlla che la durata totale in secondi venga convertita
     * correttamente in una stringa nel formato classico "minuti:secondi".
     */
    @Test
    void testGetDurationFormatted() {
        assertEquals("5:54", song.getDurationFormatted());
    }

    /**
     * Assicura che la formattazione aggiunga lo zero iniziale
     * quando i secondi rimanenti sono inferiori a 10 (es. "1:05" e non "1:5").
     */
    @Test
    void testGetDurationFormattedPaddingSecondi() {
        song.setDuration(65);
        assertEquals("1:05", song.getDurationFormatted());
    }

    /**
     * Verifica che il contatore delle riproduzioni aumenti
     * esattamente di uno ad ogni singola chiamata del metodo.
     */
    @Test
    void testIncrementPlayCount() {
        song.incrementPlayCount();
        assertEquals(1, song.getPlayCount());
        song.incrementPlayCount();
        assertEquals(2, song.getPlayCount());
    }

    /**
     * Verifica che due tracce con lo stesso ID siano considerate uguali.
     */
    @Test
    void testEqualsStessaTraccia() {
        assertEquals(song, song);
    }

    /**
     * Verifica che due tracce diverse con metadati identici non siano uguali
     * (l'uguaglianza è basata sull'ID).
     */
    @Test
    void testEqualsTrAcceDiverseNonUguali() {
        Song copia = new Song("Bohemian Rhapsody", "Queen", Genre.ROCK, 1975, 354, "/music/br.mp3");
        assertNotEquals(song, copia);
    }
}
