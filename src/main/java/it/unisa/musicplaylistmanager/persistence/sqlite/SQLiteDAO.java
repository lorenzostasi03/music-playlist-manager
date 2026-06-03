package it.unisa.musicplaylistmanager.persistence.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class SQLiteDAO {
    protected final String DB_URL;

    public SQLiteDAO(String DB_URL) {
        this.DB_URL = DB_URL;
    }

    /**
     * Crea una conessione con il database.
     * @return la connessione nel database.
     * @throws SQLException se non riesce a connettersi.
     */
    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
