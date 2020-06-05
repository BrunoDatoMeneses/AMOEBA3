package experiments.tests.plot3Dpolygon;

import experiments.tests.JZY3D_Test;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jzy3d.chart.AWTChart;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.colors.Color;
import org.jzy3d.javafx.JavaFXChartFactory;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.*;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import java.util.ArrayList;
import java.util.List;

public class ACUbe extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        HistogramBar myBar = new HistogramBar();


        Shape ctxt1 = createContextShape(new Color(1,0,0,0.75f), 0, 1, 0, 1, 0, 1);
        Shape ctxt3 = createContextShape(new Color(0,0,1,0.75f), 0.5f, 1.5f, 0.5f, 1.5f, 0.5f, 1.5f);
        Shape ctxt2 = createContextShape(new Color(0,1,0,0.75f), 2, 4, 2, 3, 2, 3);

        myBar.add(ctxt1);
        myBar.add(ctxt2);
        myBar.add(ctxt3);

        stage.setTitle(ACUbe.class.getSimpleName());
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

    private Shape createContextShape(Color contextColor, float xStart, float xEnd, float yStart, float yEnd, float zStart, float zEnd) {
        List<Polygon> faces = new ArrayList<Polygon>();
        Quad face1 = new Quad();
        face1.add(new Point(
                new Coord3d(xStart, yStart, zStart)));
        face1.add(new Point(
                new Coord3d(xEnd, yStart, zStart)));
        face1.add(new Point(
                new Coord3d(xEnd, yEnd, zStart)));
        face1.add(new Point(
                new Coord3d(xStart, yEnd, zStart)));
        face1.setColor(contextColor);
        faces.add(face1);

        Quad face2 = new Quad();
        face2.add(new Point(
                new Coord3d(xStart, yStart, zEnd)));
        face2.add(new Point(
                new Coord3d(xEnd, yStart, zEnd)));
        face2.add(new Point(
                new Coord3d(xEnd, yEnd, zEnd)));
        face2.add(new Point(
                new Coord3d(xStart, yEnd, zEnd)));
        face2.setColor(contextColor);
        faces.add(face2);

        Quad face3 = new Quad();
        face3.add(new Point(
                new Coord3d(xStart,yStart,zStart)));
        face3.add(new Point(
                new Coord3d(xStart,yEnd,zStart)));
        face3.add(new Point(
                new Coord3d(xStart,yEnd,zEnd)));
        face3.add(new Point(
                new Coord3d(xStart,yStart,zEnd)));
        face3.setColor(contextColor);
        faces.add(face3);

        Quad face4 = new Quad();
        face4.add(new Point(
                new Coord3d(xEnd,yStart,zStart)));
        face4.add(new Point(
                new Coord3d(xEnd,yEnd,zStart)));
        face4.add(new Point(
                new Coord3d(xEnd,yEnd,zEnd)));
        face4.add(new Point(
                new Coord3d(xEnd,yStart,zEnd)));
        face4.setColor(contextColor);
        faces.add(face4);

        Quad face5 = new Quad();
        face5.add(new Point(
                new Coord3d(xStart,yStart,zStart)));
        face5.add(new Point(
                new Coord3d(xEnd,yStart,zStart)));
        face5.add(new Point(
                new Coord3d(xEnd,yStart,zEnd)));
        face5.add(new Point(
                new Coord3d(xStart,yStart,zEnd)));
        face5.setColor(contextColor);
        faces.add(face5);

        Quad face6 = new Quad();
        face6.add(new Point(
                new Coord3d(xStart,yEnd,zStart)));
        face6.add(new Point(
                new Coord3d(xEnd,yEnd,zStart)));
        face6.add(new Point(
                new Coord3d(xEnd,yEnd,zEnd)));
        face6.add(new Point(
                new Coord3d(xStart,yEnd,zEnd)));
        face6.setColor(contextColor);
        faces.add(face6);
        return new Shape(faces);
    }


}
