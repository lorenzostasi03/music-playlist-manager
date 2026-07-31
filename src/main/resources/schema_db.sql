PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS song (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    author TEXT NOT NULL,
    genre TEXT NOT NULL,
    year INTEGER NOT NULL,
    duration INTEGER NOT NULL,
    file_path TEXT NOT NULL,
    play_count INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS playlist (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    play_count INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS playlist_criterion (
    playlist_id TEXT NOT NULL,
    criterion_type TEXT NOT NULL
        CHECK (criterion_type IN ('GENRE', 'YEAR', 'TAG')),
    criterion_value TEXT NOT NULL,

    PRIMARY KEY (
        playlist_id,
        criterion_type,
        criterion_value
    ),

    FOREIGN KEY (playlist_id) REFERENCES playlist(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS playlist_song (
    playlist_id TEXT NOT NULL,
    song_id TEXT NOT NULL,
    position INTEGER NOT NULL,

    PRIMARY KEY (playlist_id, song_id),

    FOREIGN KEY (playlist_id) REFERENCES playlist(id)
        ON DELETE CASCADE,

    FOREIGN KEY (song_id) REFERENCES song(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS song_tag (
    song_id TEXT NOT NULL,
    tag TEXT NOT NULL,

    PRIMARY KEY (song_id, tag),

    FOREIGN KEY (song_id) REFERENCES song(id)
        ON DELETE CASCADE
);
