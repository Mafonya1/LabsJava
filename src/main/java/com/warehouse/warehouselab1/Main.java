//создание интерфейса
package com.warehouse.warehouselab1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("graphics.fxml")); //Для создания интерфейса из файла fxml применяется метод FXMLLoader.load(). Чтобы получить определение интерфейса из файла, используется метод getClass().getResource("Main.fxml").

        stage.setScene(new Scene(root));

        stage.show();
    }
}