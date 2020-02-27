package gui;

import experiments.nDimensionsLaunchers.F_N_Manager;
import experiments.tests.JZY3D_Test;
import fr.irit.smac.amak.ui.VuiExplorer;
import fr.irit.smac.amak.ui.drawables.Drawable;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import kernel.StudiedSystem;
import org.jzy3d.chart.AWTChart;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.javafx.JavaFXChartFactory;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class View3D {

    public String title;

    private BorderPane pane;
    private BorderPane paneLeft;
    private BorderPane paneRight;

    private StudiedSystem studiedSystem;

    public BorderPane getPaneLeft() {
        return paneLeft;
    }

    public BorderPane getPane() {
        return pane;
    }

    public BorderPane getPaneRight() {
        return paneRight;
    }

    public View3D(StudiedSystem ss) {

        studiedSystem =ss;

        pane = new BorderPane();
        paneLeft = new BorderPane();
        paneRight = new BorderPane();

        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                // Jzy3d
                JavaFXChartFactory factory = new JavaFXChartFactory();
                AWTChart chart  = getDemoChart(factory, "offscreen");
                ImageView imageView = factory.bindImageView(chart);

                // JavaFX
                pane.setLeft(paneLeft);
                pane.setRight(paneRight);
                paneLeft.setCenter(imageView);
                Button TODO = new Button("TODO");
                paneRight.setCenter(TODO);

            }
        });




    }

    private AWTChart getDemoChart(JavaFXChartFactory factory, String toolkit) {
        // -------------------------------
        // Define a function to plot
        Mapper mapper = new Mapper() {
            @Override
            public double f(double x, double y) {

                Double[] request = new Double[2];
                request[0]=x;
                request[1]=y;
                return ((F_N_Manager)(studiedSystem)).model(request);
            }
        };

        // Define range and precision for the function to plot
        Range range = new Range(-100, 100);
        int steps = 50;

        // Create the object to represent the function over the given range.
        final Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(range, steps, range, steps), mapper);
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
        surface.setFaceDisplayed(true); // draws surface polygons content
        surface.setWireframeDisplayed(true); // draw surface polygons border
        surface.setWireframeColor(Color.BLACK); // set polygon border in black




        // -------------------------------
        // Create a chart
        Quality quality = Quality.Fastest;
        //quality.setSmoothPolygon(true);
        //quality.setAnimated(true);

        // let factory bind mouse and keyboard controllers to JavaFX node
        AWTChart chart = (AWTChart) factory.newChart(quality, toolkit);
        chart.getScene().getGraph().add(surface);
        return chart;
    }


}
