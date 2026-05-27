package it.unisa.musicplaylistmanager.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class App extends Application {

  @Override
  public void start(Stage stage) {
    Label label = new Label("Music Playlist Manager");
    Scene scene = new Scene(label, 800, 600);

    stage.setTitle("Music Playlist Manager");
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
