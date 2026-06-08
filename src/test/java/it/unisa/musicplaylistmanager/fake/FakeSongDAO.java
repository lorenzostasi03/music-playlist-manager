package it.unisa.musicplaylistmanager.fake;

import it.unisa.musicplaylistmanager.model.entity.Song;
import it.unisa.musicplaylistmanager.persistence.dao.SongDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FakeSongDAO implements SongDAO {

	private final List<Song> songs = new ArrayList<>();

	@Override
	public void save(Song song) {
		songs.add(song);
	}

	@Override
	public void update(Song song) {
		delete(song.getId());
		songs.add(song);
	}

	@Override
	public void delete(UUID songId) {
		songs.removeIf(s -> s.getId().equals(songId));
	}

	@Override
	public List<Song> getSongs() {
		return new ArrayList<>(songs);
	}
}
