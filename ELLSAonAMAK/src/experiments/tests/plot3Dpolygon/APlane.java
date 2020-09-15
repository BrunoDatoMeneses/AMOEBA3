package experiments.tests.plot3Dpolygon;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jzy3d.chart.AWTChart;
import org.jzy3d.colors.Color;
import org.jzy3d.javafx.JavaFXChartFactory;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.*;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import java.util.ArrayList;
import java.util.List;

public class APlane extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        HistogramBar myBar = new HistogramBar();


        Shape ctxt1 = createPlaneShape(new Color(1,0,0,0.75f), 0, 1, 0, 1, 0, 1,1,2);
        //Shape ctxt3 = createPlaneShape(new Color(0,0,1,0.75f), 0.5f, 1.5f, 0.5f, 1.5f, 0, 1,0,0);
        //Shape ctxt2 = createPlaneShape(new Color(0,1,0,0.75f), 2, 4, 2, 3, 0, 1,0,0);

        myBar.add(ctxt1);
        /*myBar.add(ctxt2);
        myBar.add(ctxt3);*/

        stage.setTitle(APlane.class.getSimpleName());
        JavaFXChartFactory factory = new JavaFXChartFactory();
        Quality quality = Quality.Advanced;
        quality.setSmoothPolygon(true);
        //quality.setAnimated(true);
        // let factory bind mouse and keyboard controllers to JavaFX node
        AWTChart chart = (AWTChart) factory.newChart(quality, "offscreen");
        chart.getScene().getGraph().add(myBar);
        ImageView imageView = factory.bindImageView(chart);

        // JavaFX
        StackPane pane = new StackPane();
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
        pane.getChildren().add(imageView);

        factory.addSceneSizeChangedListener(chart, scene);

        stage.setWidth(500);
        stage.setHeight(500);





        }

    private Shape createPlaneShape(Color contextColor, float xStart, float xEnd, float yStart, float yEnd, float xSySPrediction, float xSyEPrediction, float xEySPrediction, float xEyEPrediction) {
        List<Polygon> faces = new ArrayList<Polygon>();
        Quad face1 = new Quad();
        face1.add(new Point(
                new Coord3d(xStart, yStart, xSySPrediction)));
        face1.add(new Point(
                new Coord3d(xEnd, yStart, xEySPrediction)));
        face1.add(new Point(
                new Coord3d(xEnd, yEnd, xEyEPrediction)));
        face1.add(new Point(
                new Coord3d(xStart, yEnd, xSyEPrediction)));
        face1.setColor(contextColor);
        faces.add(face1);


        return new Shape(faces);
    }


}
