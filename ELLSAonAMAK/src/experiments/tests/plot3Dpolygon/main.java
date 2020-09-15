package experiments.tests.plot3Dpolygon;

import experiments.tests.JZY3D_Test;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jzy3d.chart.AWTChart;
import org.jzy3d.chart.Chart;
import org.jzy3d.javafx.JavaFXChartFactory;

import java.util.Arrays;

public class main extends Application {

    public static void main(String[] args) {Application.launch(args);}






    @Override
    public void start(Stage primaryStage) throws Exception {
        final HistogramScene histogramScene = new HistogramScene();
        for (int i = 0; i < 150; i += 50)
            for (int j = 0; j < 150; j += 50)
                histogramScene.addCylinder(i, j, Math.random());
// Create a chart for this scene
        AWTChart chart = new AWTChart() {
            public HistogramScene initializeScene() {
                return histogramScene;
            }

        };

        primaryStage.setTitle(main.class.getSimpleName());


        // JavaFX
        StackPane pane = new StackPane();
        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.show();
        JavaFXChartFactory factory = new JavaFXChartFactory();
        ImageView imageView = factory.bindImageView(chart);
        pane.getChildren().add(imageView);


        primaryStage.setWidth(500);
        primaryStage.setHeight(500);
    }
}
