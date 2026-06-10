package it.unisa.musicplaylistmanager.model.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test piccoli sulle strategie dell'iteratore.
 *
 * Servono a controllare solo il calcolo dell'indice successivo, senza coinvolgere
 * il player audio.
 */
class PlaylistIteratorStrategyTest {

	/**
	 * La strategia sequenziale deve andare avanti di uno.
	 */
	@Test
	void sequentialStrategyShouldReturnNextIndex() {
		SequentialIteratorStrategy strategy = new SequentialIteratorStrategy();

		assertEquals(1, strategy.nextIndex(0, 3));
	}

	/**
	 * La strategia sequenziale deve segnalare la fine dopo l'ultima traccia.
	 */
	@Test
	void sequentialStrategyShouldReturnMinusOneAtEnd() {
		SequentialIteratorStrategy strategy = new SequentialIteratorStrategy();

		assertEquals(-1, strategy.nextIndex(2, 3));
	}

	/**
	 * La strategia loop playlist deve tornare a zero dopo l'ultima traccia.
	 */
	@Test
	void loopStrategyShouldReturnZeroAtEnd() {
		LoopIteratorStrategy strategy = new LoopIteratorStrategy();

		assertEquals(0, strategy.nextIndex(2, 3));
	}

	/**
	 * La strategia loop su playlist vuota non deve proporre nessun indice.
	 */
	@Test
	void loopStrategyShouldReturnMinusOneForEmptyPlaylist() {
		LoopIteratorStrategy strategy = new LoopIteratorStrategy();

		assertEquals(-1, strategy.nextIndex(0, 0));
	}
}
