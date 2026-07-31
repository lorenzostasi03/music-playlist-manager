package it.unisa.musicplaylistmanager.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AudioMetadataReaderTest {

	@TempDir
	Path tempDir;

	@Test
	void leggeDurataDaFileWavValido() throws IOException {
		Path audioPath = tempDir.resolve("audio.wav");
		createSilentWav(audioPath, 2);

		int duration = AudioMetadataReader.readDurationInSeconds(audioPath.toFile());

		assertEquals(2, duration);
	}

	@Test
	void fileNullLanciaIOException() {
		assertThrows(IOException.class, () -> AudioMetadataReader.readDurationInSeconds(null));
	}

	@Test
	void fileInesistenteLanciaIOException() {
		Path missingFile = tempDir.resolve("missing.wav");

		assertThrows(IOException.class, () -> AudioMetadataReader.readDurationInSeconds(missingFile.toFile()));
	}

	@Test
	void fileNonAudioLanciaIOException() throws IOException {
		Path invalidAudio = tempDir.resolve("invalid.wav");
		Files.writeString(invalidAudio, "Questo non è un file audio.");

		assertThrows(IOException.class, () -> AudioMetadataReader.readDurationInSeconds(invalidAudio.toFile()));
	}

	private void createSilentWav(Path path, int durationSeconds) throws IOException {
		float sampleRate = 8_000;
		AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);

		int frameCount = Math.round(sampleRate * durationSeconds);
		byte[] audioData = new byte[frameCount * format.getFrameSize()];

		try (ByteArrayInputStream byteStream = new ByteArrayInputStream(audioData);
				AudioInputStream audioStream = new AudioInputStream(byteStream, format, frameCount)) {

			AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, path.toFile());
		}
	}
}
