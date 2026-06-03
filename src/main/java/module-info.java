module it.unisa.musicplaylistmanager {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens it.unisa.musicplaylistmanager.controller to javafx.fxml;

    exports it.unisa.musicplaylistmanager.app;

    exports it.unisa.musicplaylistmanager.controller to javafx.fxml;
    exports it.unisa.musicplaylistmanager.controller.playlist to javafx.fxml;
    exports it.unisa.musicplaylistmanager.controller.song to javafx.fxml;

    exports it.unisa.musicplaylistmanager.model.entity;
    exports it.unisa.musicplaylistmanager.model.library;
    exports it.unisa.musicplaylistmanager.model.playback;

    exports it.unisa.musicplaylistmanager.persistence.dao;
    exports it.unisa.musicplaylistmanager.persistence.sqlite;

    exports it.unisa.musicplaylistmanager.util;

    exports it.unisa.musicplaylistmanager.exceptions;
}
