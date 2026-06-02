package it.unisa.musicplaylistmanager.exceptions;

public class DuplicatedSongException extends RuntimeException {
    public DuplicatedSongException(String message) {
        super(message);
    }
}
