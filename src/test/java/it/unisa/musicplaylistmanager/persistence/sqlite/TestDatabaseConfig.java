package it.unisa.musicplaylistmanager.persistence.sqlite;

public final class TestDatabaseConfig {

	public static final String DB_PATH = "data/test.db";
	public static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

	private TestDatabaseConfig() {
	}
}
