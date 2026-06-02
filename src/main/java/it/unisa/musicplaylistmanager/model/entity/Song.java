package it.unisa.musicplaylistmanager.model.entity;
import java.util.*;

/**
 * Rappresenta una traccia musicale all'interno del catalogo.
 */
public class Song {
    private final UUID id;

    private String title;
    private String author;
    private Genre genre;
    private int year;
    private int duration;
    private String filePath;
    private int playCount;
    private Set<Tag> tags;


    /**
     * Crea una nuova traccia.
     *
     * @param title    titolo della traccia;
     * @param author   autore della traccia;
     * @param genre    genere musicale;
     * @param year     anno di pubblicazione;
     * @param duration durata in secondi;
     * @param filePath percorso del file audio;
     * @throws IllegalArgumentException se uno dei parametri non è valido.
     */
    public Song(String title, String author, Genre genre, int year, int duration, String filePath) {
        validateTitle(title);
        validateAuthor(author);
        validateYear(year);
        validateDuration(duration);

        this.id = UUID.randomUUID();
        this.title = title.trim();
        this.author = author.trim();
        this.genre = genre;
        this.year = year;
        this.duration = duration;
        this.filePath = filePath;
        this.playCount = 0;
        this.tags = EnumSet.noneOf(Tag.class);
    }

    /**
     * Crea una traccia musicale a partire da dati già persistenti.
     * Questo costruttore viene utilizzato per ricostruire un oggetto Song
     * già esistente nel sistema.
     *
     * @param id identificatore univoco del brano.
     * @param title titolo della traccia.
     * @param author autore della traccia.
     * @param genre genere musicale.
     * @param year anno di pubblicazione.
     * @param duration durata in secondi.
     * @param filePath percorso del file audio.
     * @param playCount numero di riproduzioni.
     */
    public Song(UUID id, String title, String author, Genre genre, int year, int duration, String filePath, int playCount) {
        this.id = id;
        this.title = title.trim();
        this.author = author.trim();
        this.genre = genre;
        this.year = year;
        this.duration = duration;
        this.filePath = filePath;
        this.playCount = playCount;
    }

    /**
     * Restituisce l'identificativo univoco della traccia.
     *
     * @return ID della traccia
     */
    public UUID getId() {
        return id;
    }

    /**
     * Restituisce il titolo della traccia.
     *
     * @return titolo
     */
    public String getTitle() {
        return title;
    }

    /**
     * Restituisce l'autore della traccia.
     *
     * @return autore
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Restituisce il genere musicale della traccia.
     *
     * @return genere
     */
    public Genre getGenre() {
        return genre;
    }

    /**
     * Restituisce l'anno di pubblicazione della traccia.
     *
     * @return anno
     */
    public int getYear() {
        return year;
    }

    /**
     * Restituisce la durata della traccia in secondi.
     *
     * @return durata in secondi
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Restituisce il percorso del file audio.
     *
     * @return percorso file
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Restituisce il numero di riproduzioni della traccia.
     *
     * @return contatore di riproduzioni
     */
    public int getPlayCount() {

        return playCount;
    }

    /**
     * Restituisce l'insieme dei tag della traccia.
     *
     * @return insieme di Tag associati alla traccia
     */
    public Set<Tag> getTags() {

        return Collections.unmodifiableSet(tags);
    }


    /**
     * Imposta il titolo della traccia.
     *
     * @param title nuovo titolo;
     */
    public void setTitle(String title) {
        validateTitle(title);
        this.title = title.trim();
    }

    /**
     * Imposta l'autore della traccia.
     *
     * @param author nuovo autore;
     */
    public void setAuthor(String author) {
        validateAuthor(author);
        this.author = author.trim();
    }

    /**
     * Imposta il genere musicale della traccia.
     *
     * @param genre nuovo genere;
     */
    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    /**
     * Imposta l'anno di pubblicazione della traccia.
     *
     * @param year nuovo anno;
     */
    public void setYear(int year) {
        validateYear(year);
        this.year = year;
    }

    /**
     * Imposta la durata della traccia in secondi.
     *
     * @param duration nuova durata;
     */
    public void setDuration(int duration) {
        validateDuration(duration);
        this.duration = duration;
    }

    /**
     * Imposta il percorso del file audio.
     *
     * @param filePath nuovo percorso;
     */
    private void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    /**
     * Incrementa di uno il contatore di riproduzioni della traccia.
     *
     */
    private void incrementPlayCount(){
    this.playCount++;
    }


    /**
     * Aggiunge un tag alla traccia.
     * Se il tag è già presente, l'operazione non ha alcun effetto.
     *
     * @param tag tag da aggiungere;
     */
    public void addTag(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Il tag non può essere null.");
        }
        tags.add(tag);
    }

    /**
     * Rimuove un tag dalla traccia.
     *
     * @param tag tag da rimuovere;
     */
    public void removeTag(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Il tag non può essere null.");
        }
        tags.remove(tag);
    }

    /**
     * Restituisce una rappresentazione leggibile della durata nel formato.
     *
     * @return stringa nel formato {@code mm:ss}
     */
    public String getDurationFormatted() {
        int minuti = duration / 60;
        int secondi = duration % 60;
        return String.format("%d:%02d", minuti, secondi);
    }


    /**
     * Verifica se la traccia ha il tag specificato.
     *
     * @param tag tag da verificare
     * @return true se la traccia possiede il tag
     */
    public boolean hasTag(Tag tag) {

        return tags.contains(tag);
    }


    /**
     * Due tracce sono considerate uguali se e solo se hanno lo stesso ID univoco.
     *
     * @param o oggetto da confrontare
     * @return true se gli ID coincidono
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song)) return false;
        Song song = (Song) o;
        return Objects.equals(id, song.id);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Restituisce una rappresentazione testuale della traccia con i metadati principali.
     *
     * @return stringa descrittiva della traccia
     */
    @Override
    public String toString() {
        return String.format("Song{id='%s', title='%s', author='%s', genre=%s, year=%d}",
            id, title, author, genre, year);
    }


    /**
     * Valida che il titolo non sia {@code null} né composto solo da spazi.
     *
     * @param title titolo da validare
     */
    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Il titolo della traccia non può essere vuoto.");
        }
    }

    /**
     * Valida che l'autore non sia {@code null} né composto solo da spazi.
     *
     * @param author autore da validare
     */
    private void validateAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("L'autore della traccia non può essere vuoto.");
        }
    }

    /**
     * Valida che l'anno sia un valore positivo e non superiore all'anno corrente + 1
     *
     *
     * @param year anno da validare
     */
    private void validateYear(int year) {
        int annoCorrente = java.time.Year.now().getValue();
        if (year <= 0 || year > annoCorrente + 1) {
            throw new IllegalArgumentException(
                "L'anno di pubblicazione non è valido: " + year);
        }
    }

    /**
     * Valida che la durata sia non negativa.
     *
     * @param duration durata da validare
     */
    private void validateDuration(int duration) {
        if (duration < 0) {
            throw new IllegalArgumentException(
                "La durata non può essere negativa: " + duration);
        }
    }
}
