package it.unisa.musicplaylistmanager.persistence.sqlite;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Si occupa dell'inizializzazione del database SQLite.
 *
 * <p>
 * Crea la directory destinata al database, apre la connessione e applica le
 * istruzioni SQL contenute nel file {@code schema_db.sql}.
 * </p>
 */
public final class DatabaseInitializer {
	private DatabaseInitializer() {
	}

	/**
	 * Inizializza il database utilizzando l'URL JDBC configurato in
	 * {@link DatabaseConfig}.
	 *
	 * @throws RuntimeException
	 *             se si verifica un errore durante l'inizializzazione
	 */
	public static void initialize() {
		initialize(DatabaseConfig.DB_URL);
	}

	/**
	 * Inizializza il database corrispondente all'URL JDBC specificato.
	 *
	 * @param dbUrl
	 *            URL JDBC del database da inizializzare
	 * @throws RuntimeException
	 *             se non è possibile creare la directory, aprire la connessione,
	 *             leggere lo schema oppure eseguire le istruzioni SQL
	 */
	public static void initialize(String dbUrl) {
		try {
			Files.createDirectories(Path.of("data"));

			try (Connection conn = DriverManager.getConnection(dbUrl);
					InputStream is = DatabaseInitializer.class.getResourceAsStream("/schema_db.sql")) {

				if (is == null) {
					throw new IllegalStateException("schema_db.sql non trovato nelle resources.");
				}

				String schema = new String(is.readAllBytes(), StandardCharsets.UTF_8);

				try (Statement stmt = conn.createStatement()) {
					for (String sql : schema.split(";")) {
						sql = sql.trim();

						if (!sql.isEmpty()) {
							stmt.execute(sql);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Errore durante l'inizializzazione del database.", e);
		}
	}
}
