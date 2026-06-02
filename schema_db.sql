PRAGMA foreign_keys = ON;

CREATE TABLE song (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    author TEXT NOT NULL,
    genre TEXT NOT NULL,
    year INTEGER NOT NULL,
    duration INTEGER NOT NULL,
    file_path TEXT NOT NULL,
    play_count INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE playlist (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    play_count INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE playlist_song (
    playlist_id TEXT NOT NULL,
    song_id TEXT NOT NULL,

    PRIMARY KEY (playlist_id, song_id),

    FOREIGN KEY (playlist_id) REFERENCES playlist(id)
        ON DELETE CASCADE,

    FOREIGN KEY (song_id) REFERENCES song(id)
        ON DELETE CASCADE
);
