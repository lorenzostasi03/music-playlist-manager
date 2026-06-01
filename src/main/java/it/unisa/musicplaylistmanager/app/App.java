package it.unisa.musicplaylistmanager.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

  @Override
  public void start(Stage stage) {
      Scene scene = null;
      String path = "/views/MainView.fxml";
      try {
          scene = new Scene(
              FXMLLoader.load(getClass().getResource(path))
          );
      } catch (IOException e) {
          System.err.println("File non trovato: " + path);
      }

      stage.setTitle("Music Playlist Manager");
      stage.setScene(scene);
      stage.setResizable(false);
      stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
