package it.unisa.musicplaylistmanager.exceptions;

public class DuplicatedPlaylistException extends RuntimeException {
	public DuplicatedPlaylistException(String message) {
		super(message);
	}
}
