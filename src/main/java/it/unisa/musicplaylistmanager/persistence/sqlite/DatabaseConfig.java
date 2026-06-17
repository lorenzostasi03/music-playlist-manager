package it.unisa.musicplaylistmanager.persistence.sqlite;
/**
 * * Contiene i parametri di configurazione utilizzati per la connessione al *
 * database SQLite dell'applicazione.
 */
public final class DatabaseConfig {
	/** * URL JDBC del database SQLite persistente. */
	public static final String DB_URL = "jdbc:sqlite:data/database.db";
	private DatabaseConfig() {
	}
}
