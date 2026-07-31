package it.unisa.musicplaylistmanager.util;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;

/**
 * Fornisce operazioni per leggere i metadati dei file audio.
 */
public final class AudioMetadataReader {

	private AudioMetadataReader() {
	}

	/**
	 * Legge la durata di un file audio.
	 *
	 * @param file
	 *            file audio da analizzare
	 * @return durata del file espressa in secondi
	 * @throws IOException
	 *             se il file non è valido oppure i suoi metadati non possono essere
	 *             letti
	 */
	public static int readDurationInSeconds(File file) throws IOException {
		if (file == null || !file.isFile()) {
			throw new IOException("Il file audio selezionato non è valido.");
		}

		try {
			AudioFile audioFile = AudioFileIO.read(file);
			int duration = audioFile.getAudioHeader().getTrackLength();

			if (duration <= 0) {
				throw new IOException("La durata del file audio non è valida.");
			}

			return duration;
		} catch (CannotReadException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
			throw new IOException("Impossibile leggere la durata del file audio.", e);
		}
	}
}
