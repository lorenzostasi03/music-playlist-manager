package it.unisa.musicplaylistmanager.persistence.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class SQLiteDAO {
    protected static final String DB_URL = "jdbc:sqlite:test.db";

    /**
     * Crea una conessione con il database.
     * @return la connessione nel database.
     * @throws SQLException se non riesce a connettersi.
     */
    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
