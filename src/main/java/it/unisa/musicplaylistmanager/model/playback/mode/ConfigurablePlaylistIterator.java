package it.unisa.musicplaylistmanager.model.playback.mode;

import it.unisa.musicplaylistmanager.model.entity.Playlist;
import it.unisa.musicplaylistmanager.model.entity.Song;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Iteratore configurabile per la riproduzione di una playlist.
 *
 * <p>
 * L'iteratore lavora sempre sul contenuto aggiornato della playlist e mantiene
 * lo stato della sessione di riproduzione, memorizzando il brano corrente,
 * l'ultimo indice conosciuto e l'insieme dei brani già riprodotti.
 * </p>
 *
 * <p>
 * L'ordine di avanzamento è determinato dalla strategia configurata, che può
 * essere sostituita anche durante la riproduzione.
 * </p>
 */
public class ConfigurablePlaylistIterator implements PlaylistIterator {

	private final Playlist playlist;

	private int lastKnownIndex;
	private Song currentSong;
	private final Set<UUID> playedSongIds;

	private PlaylistIteratorStrategy strategy;

	/**
	 * Crea un iteratore associato alla playlist e alla strategia specificate.
	 *
	 * <p>
	 * L'iteratore viene inizializzato prima del primo brano della playlist:
	 * l'indice corrente è impostato a {@code -1}, non è presente alcun brano
	 * corrente e nessun brano risulta ancora riprodotto.
	 * </p>
	 *
	 * @param playlist
	 *            playlist sulla quale eseguire l'iterazione
	 * @param strategy
	 *            strategia utilizzata per determinare il brano successivo
	 * @throws IllegalArgumentException
	 *             se la playlist o la strategia sono {@code null}
	 */
	public ConfigurablePlaylistIterator(Playlist playlist, PlaylistIteratorStrategy strategy) {

		if (playlist == null) {
			throw new IllegalArgumentException("Playlist cannot be null.");
		}

		if (strategy == null) {
			throw new IllegalArgumentException("Playlist iterator strategy cannot be null.");
		}

		this.playlist = playlist;
		this.strategy = strategy;
		this.lastKnownIndex = -1;
		this.currentSong = null;
		this.playedSongIds = new HashSet<>();
	}

	/**
	 * Restituisce il prossimo brano determinato dalla strategia corrente.
	 *
	 * <p>
	 * Prima di calcolare il prossimo indice, il metodo recupera il contenuto
	 * aggiornato della playlist e determina la posizione attuale del brano
	 * corrente. In questo modo l'iteratore può adattarsi a eventuali modifiche
	 * apportate alla playlist durante la riproduzione.
	 * </p>
	 *
	 * <p>
	 * Quando viene individuato un nuovo brano, il metodo aggiorna il brano
	 * corrente, l'ultimo indice conosciuto e l'insieme degli identificativi dei
	 * brani già riprodotti.
	 * </p>
	 *
	 * @return il prossimo brano da riprodurre, oppure {@code null} se la playlist è
	 *         vuota o se la strategia non individua un indice valido
	 */
	@Override
	public Song next() {
		List<Song> songs = playlist.getSongs();

		if (songs.isEmpty()) {
			return null;
		}

		int currentIndex = resolveCurrentIndex(songs);
		int nextIndex = strategy.nextIndex(currentIndex, songs, playedSongIds);

		if (nextIndex < 0 || nextIndex >= songs.size()) {
			return null;
		}

		currentSong = songs.get(nextIndex);
		lastKnownIndex = nextIndex;
		playedSongIds.add(currentSong.getId());

		return currentSong;
	}

	/**
	 * Sostituisce la strategia utilizzata per determinare il prossimo brano.
	 *
	 * <p>
	 * La modifica non azzera lo stato dell'iteratore: il brano corrente, l'ultimo
	 * indice conosciuto e i brani già riprodotti vengono mantenuti.
	 * </p>
	 *
	 * @param strategy
	 *            nuova strategia di iterazione
	 * @throws IllegalArgumentException
	 *             se la strategia è {@code null}
	 */
	@Override
	public void setStrategy(PlaylistIteratorStrategy strategy) {
		if (strategy == null) {
			throw new IllegalArgumentException("Playlist iterator strategy cannot be null.");
		}

		this.strategy = strategy;
	}

	/**
	 * Restituisce l'ultimo indice conosciuto del brano corrente.
	 *
	 * <p>
	 * Prima dell'avvio della riproduzione il valore restituito è {@code -1}.
	 * </p>
	 *
	 * @return indice dell'ultimo brano selezionato, oppure {@code -1} se non è
	 *         stato ancora selezionato alcun brano
	 */
	@Override
	public int getCurrentIndex() {
		return lastKnownIndex;
	}

	/**
	 * Restituisce il brano attualmente selezionato dall'iteratore.
	 *
	 * @return brano corrente, oppure {@code null} se l'iterazione non è ancora
	 *         iniziata
	 */
	@Override
	public Song getCurrentSong() {
		return currentSong;
	}

	/**
	 * Determina la posizione corrente del brano nella versione aggiornata della
	 * playlist.
	 *
	 * <p>
	 * Se non è ancora stato selezionato alcun brano, viene restituito {@code -1}.
	 * Se il brano corrente è ancora presente nella playlist, il metodo ne ricerca
	 * la posizione tramite identificativo e aggiorna {@code lastKnownIndex}.
	 * </p>
	 *
	 * <p>
	 * Se il brano corrente è stato rimosso dalla playlist, viene restituita la
	 * posizione immediatamente precedente all'ultimo indice conosciuto. Questo
	 * permette alle strategie sequenziali di continuare dal brano che ha preso il
	 * posto di quello rimosso.
	 * </p>
	 *
	 * @param songs
	 *            contenuto aggiornato della playlist
	 * @return indice corrente del brano, {@code -1} se l'iterazione non è iniziata,
	 *         oppure un indice corretto in seguito alla rimozione del brano
	 *         corrente
	 */
	private int resolveCurrentIndex(List<Song> songs) {
		if (currentSong == null) {
			return -1;
		}

		for (int i = 0; i < songs.size(); i++) {
			if (songs.get(i).getId().equals(currentSong.getId())) {
				lastKnownIndex = i;
				return i;
			}
		}

		return Math.max(-1, lastKnownIndex - 1);
	}
}
