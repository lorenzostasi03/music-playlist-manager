package it.unisa.musicplaylistmanager.persistence.sqlite;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public final class DatabaseInitializer {

	private DatabaseInitializer() {
	}

	public static void initialize() {
		initialize(DatabaseConfig.DB_URL);
	}

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
