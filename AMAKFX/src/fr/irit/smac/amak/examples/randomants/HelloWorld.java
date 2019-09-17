package fr.irit.smac.amak.examples.randomants;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class HelloWorld extends Application {

    @Override public void start(Stage stage) {
        Text text = new Text(10, 40, "Hello World!");
        text.setFont(new Font(40));
        Scene scene = new Scene(new Group(text));

        stage.setTitle("Welcome to JavaFX!"); 
        stage.setScene(scene); 
        stage.sizeToScene(); 
        
        

        Stage stage2 = new Stage();
        stage2.setScene(new Scene(new Group(new Button("my second window"))));
        stage2.show();

        stage.show(); 
    }
    
    

    public static void main(String[] args) {
        Application.launch(args);
    }
}

