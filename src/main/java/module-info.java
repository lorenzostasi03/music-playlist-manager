module it.unisa.musicplaylistmanager {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.media;
    requires transitive javafx.graphics;

	opens it.unisa.musicplaylistmanager.controller to javafx.fxml;
	opens it.unisa.musicplaylistmanager.controller.playlist to javafx.fxml;
	opens it.unisa.musicplaylistmanager.controller.song to javafx.fxml;

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
}
