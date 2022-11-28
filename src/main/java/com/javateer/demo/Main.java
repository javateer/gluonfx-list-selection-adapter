package com.javateer.demo;

import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.visual.Swatch;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    private final AppManager appManager = AppManager.initialize(this::postInit);

    @Override
    public void init() {

        AppViewManager.registerViewsAndDrawer();
    }

    @Override
    public void start(Stage stage) {

        appManager.start(stage);
    }

    private void postInit(Scene scene) {

        Swatch.BLUE.assignTo(scene);
        //scene.getStylesheets().add(Main.class.getResource("styles.css").toExternalForm());
        ((Stage) scene.getWindow()).getIcons().add(new Image(Main.class.getResourceAsStream("/icon.png")));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
