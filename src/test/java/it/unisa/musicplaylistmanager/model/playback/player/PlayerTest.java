package it.unisa.musicplaylistmanager.model.playback.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.model.playback.events.EventType;
import it.unisa.musicplaylistmanager.model.playback.mode.PlaybackMode;
import it.unisa.musicplaylistmanager.model.playback.playable.Playable;
import org.junit.jupiter.api.Test;

class PlayerTest {

	@Test
	void newPlayerShouldBeStopped() {
		Player player = new Player();

		assertEquals(PlayerState.STOPPED, player.getState());
		assertNull(player.getCurrentPlayable());
	}

	@Test
	void playShouldStartPlayableAndSetStateToPlaying() {
		Player player = new Player();
		FakePlayable playable = new FakePlayable();

		player.play(playable);

		assertTrue(playable.playCalled);
		assertEquals(PlayerState.PLAYING, player.getState());
		assertSame(playable, player.getCurrentPlayable());
	}

	@Test
	void playShouldRejectNullPlayable() {
		Player player = new Player();

		assertThrows(IllegalArgumentException.class, () -> player.play(null));
	}

	@Test
	void pauseShouldPauseCurrentPlayableAndSetStateToPaused() {
		Player player = new Player();
		FakePlayable playable = new FakePlayable();

		player.play(playable);
		player.pause();

		assertTrue(playable.pauseCalled);
		assertEquals(PlayerState.PAUSED, player.getState());
	}

	@Test
	void resumeShouldResumeCurrentPlayableAndSetStateToPlaying() {
		Player player = new Player();
		FakePlayable playable = new FakePlayable();

		player.play(playable);
		player.pause();
		player.resume();

		assertTrue(playable.resumeCalled);
		assertEquals(PlayerState.PLAYING, player.getState());
	}

	@Test
	void stopShouldStopCurrentPlayableAndClearCurrentPlayable() {
		Player player = new Player();
		FakePlayable playable = new FakePlayable();

		player.play(playable);
		player.stop();

		assertTrue(playable.stopCalled);
		assertEquals(PlayerState.STOPPED, player.getState());
		assertNull(player.getCurrentPlayable());
	}

	@Test
	void playerShouldBecomeStoppedWhenPlayableCompletedEventIsReceived() {
		Player player = new Player();
		FakePlayable playable = new FakePlayable();

		player.play(playable);
		playable.getEvents().notifyListeners(EventType.PLAYABLE_COMPLETED);

		assertFalse(playable.stopCalled);
		assertEquals(PlayerState.STOPPED, player.getState());
		assertNull(player.getCurrentPlayable());
	}

	@Test
	void playerShouldPlayNextQueuedPlayableWhenCurrentPlayableCompleted() {
		Player player = new Player();
		FakePlayable firstPlayable = new FakePlayable();
		FakePlayable secondPlayable = new FakePlayable();

		player.play(firstPlayable);
		player.enqueue(secondPlayable);

		firstPlayable.getEvents().notifyListeners(EventType.PLAYABLE_COMPLETED);

		assertFalse(firstPlayable.stopCalled);
		assertTrue(secondPlayable.playCalled);
		assertEquals(PlayerState.PLAYING, player.getState());
		assertSame(secondPlayable, player.getCurrentPlayable());
	}

	private static class FakePlayable extends Playable {

		private boolean playCalled;
		private boolean pauseCalled;
		private boolean resumeCalled;
		private boolean stopCalled;

		@Override
		public void play() {
			playCalled = true;
		}

		@Override
		public void pause() {
			pauseCalled = true;
		}

		@Override
		public void resume() {
			resumeCalled = true;
		}

		@Override
		public void stop() {
			stopCalled = true;
		}

		@Override
		public Song getCurrentSong() {
			return null;
		}

		@Override
		public String getTitle() {
			return "Fake playable";
		}

		@Override
		public boolean skipToNextSong() {
			return false;
		}

		@Override
		public void setPlaybackMode(PlaybackMode mode) {
		}

		@Override
		public PlaybackMode getPlaybackMode() {
			return PlaybackMode.SEQUENTIAL;
		}

		@Override
		protected void updatePlayCount() {
			return;
		}

		@Override
		public void update(EventType eventType) {
		}
	}
}
