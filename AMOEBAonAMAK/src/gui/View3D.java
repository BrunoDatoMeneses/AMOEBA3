package gui;

import agents.context.Context;
import agents.context.localModel.LocalModelMillerRegression;
import experiments.nDimensionsLaunchers.F_N_Manager;
import experiments.nDimensionsLaunchers.PARAMS;
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
import kernel.AMOEBA;
import kernel.StudiedSystem;
import org.jzy3d.chart.AWTChart;
import org.jzy3d.chart.Chart;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.javafx.JavaFXChartFactory;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import utils.TRACE_LEVEL;

import java.util.ArrayList;
import java.util.Arrays;


public class View3D {

    public String title;

    public AMOEBA amoeba;

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

    private float spaceSize;

    float increment = 0.5f;

    AWTChart chart1;
    ImageView imageView1;

    AWTChart chart2;
    ImageView imageView2;

    public View3D(StudiedSystem ss , AMOEBA amb) {

        studiedSystem =ss;
        amoeba = amb;

        spaceSize = (float)(2*((F_N_Manager)studiedSystem).spaceSize);

        pane = new BorderPane();
        paneLeft = new BorderPane();
        paneRight = new BorderPane();

        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                // Jzy3d
                JavaFXChartFactory factory1 = new JavaFXChartFactory();
                JavaFXChartFactory factory2 = new JavaFXChartFactory();
                //AWTChart chart  = getSurfaceChart(factory, "offscreen");
                chart1  = getScatterPlotChart(factory1, "offscreen");
                chart2  = getScatterPlotChart(factory2, "offscreen");


                imageView1 = factory1.bindImageView(chart1);
                imageView2 = factory2.bindImageView(chart2);
                imageView1.setFitHeight(100);
                imageView1.setFitWidth(100);

                // JavaFX

                pane.setLeft(paneLeft);
                pane.setRight(paneRight);

                paneLeft.prefHeightProperty().bind(pane.heightProperty());
                paneRight.prefHeightProperty().bind(pane.heightProperty());
                paneLeft.setPrefWidth(paneLeft.getHeight());
                paneRight.setPrefWidth(paneRight.getHeight());

                paneLeft.setCenter(imageView1);
                paneRight.setCenter(imageView2);


                imageView1.fitWidthProperty().bind(paneLeft.widthProperty());
                imageView1.fitHeightProperty().bind(paneLeft.heightProperty());

                imageView2.fitWidthProperty().bind(paneRight.widthProperty());
                imageView2.fitHeightProperty().bind(paneRight.heightProperty());

            }
        });




    }

    public void updateContextChart(){

        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {

                // Jzy3d
                JavaFXChartFactory factory1 = new JavaFXChartFactory();
                //AWTChart chart  = getSurfaceChart(factory, "offscreen");
                chart1  = getScatterPlotChart(factory1, "offscreen");
                imageView1 = factory1.bindImageView(chart1);
                // JavaFX
                pane.setLeft(paneLeft);

                paneLeft.prefHeightProperty().bind(pane.heightProperty());
                paneLeft.setPrefWidth(paneLeft.getHeight());
                paneLeft.setCenter(imageView1);
                imageView1.fitWidthProperty().bind(paneLeft.widthProperty());
                imageView1.fitHeightProperty().bind(paneLeft.heightProperty());

                // Jzy3d
                JavaFXChartFactory factory2 = new JavaFXChartFactory();
                //AWTChart chart  = getSurfaceChart(factory, "offscreen");
                chart2  = getScatterPlotChartFromContexts(factory2, "offscreen");
                imageView2 = factory2.bindImageView(chart2);
                //paneRight.prefHeightProperty().bind(pane.heightProperty());
                paneRight.prefHeightProperty().bind(pane.heightProperty());
                paneRight.setPrefWidth(paneRight.getHeight());
                paneRight.setCenter(imageView2);
                imageView2.fitWidthProperty().bind(paneRight.widthProperty());
                imageView2.fitHeightProperty().bind(paneRight.heightProperty());

            }
        });


    }

    private AWTChart getSurfaceChart(JavaFXChartFactory factory, String toolkit) {
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
        Range range = new Range(-spaceSize, spaceSize);
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




    private AWTChart getScatterPlotChart(JavaFXChartFactory factory, String toolkit) {


        float xStart = -spaceSize;
        float xEnd = spaceSize;
        float yStart = -spaceSize;
        float yEnd = spaceSize;
        float x=xStart;
        float y=yStart;
        float z;

        int nbPoints = (int)(Math.pow(1 + (2*spaceSize/increment),2) );
        Coord3d[] points = new Coord3d[nbPoints];
        Color[]   colors = new Color[nbPoints];

        int i = 0;
        while(x<=xEnd){
            y = yStart;
            while(y<=yEnd){
                z = (float)model(x,y);

                points[i] = new Coord3d(x, y, z);
                colors[i] = getColor((float)PARAMS.minPrediction,(float)PARAMS.maxPrediction, z);


                i++;
                y += increment;
            }

            x+= increment;
        }


        Scatter scatter = new Scatter(points, colors);
        Quality quality = Quality.Fastest;
        AWTChart chart = (AWTChart) factory.newChart(quality, toolkit);
        //chart.getAxeLayout().setMainColor(Color.WHITE);
        //chart.getView().setBackgroundColor(Color.BLACK);
        chart.getScene().add(scatter);

        return chart;
    }

    public AWTChart getScatterPlotChartFromContexts(JavaFXChartFactory factory, String toolkit) {



        ArrayList<Coord3d> pointAAjouter = new ArrayList<>();
        for (Context ctxt : amoeba.getContexts()){

            float xStart = (float)ctxt.getRanges().get(amoeba.getPercepts().get(0)).getStart();
            float xEnd = (float)ctxt.getRanges().get(amoeba.getPercepts().get(0)).getEnd();
            float yStart = (float)ctxt.getRanges().get(amoeba.getPercepts().get(1)).getStart();
            float yEnd = (float)ctxt.getRanges().get(amoeba.getPercepts().get(1)).getEnd();

            float x=xStart;
            float y=yStart;
            float z;

            while(x<=xEnd){
                y = yStart;
                while(y<=yEnd){
                    double[] perception = new double[2];
                    perception[0]=x;
                    perception[1]=y;
                    z = (float) ((LocalModelMillerRegression)ctxt.getLocalModel()).getPropositionFrom2DPerceptions(perception);

                    pointAAjouter.add(new Coord3d(x, y, z));


                    y += increment;
                }

                x+= increment;
            }

        }

        Coord3d[] points = new Coord3d[pointAAjouter.size()];
        Color[]   colors = new Color[pointAAjouter.size()];

        int i =0;
        for( Coord3d coord : pointAAjouter){
            points[i] = coord;
            colors[i] = getColor((float)PARAMS.minPrediction,(float)PARAMS.maxPrediction, coord.z);
            i++;
        }

        Scatter scatter = new Scatter(points, colors);
        Quality quality = Quality.Fastest;
        AWTChart chart = (AWTChart) factory.newChart(quality, toolkit);
        //chart.getAxeLayout().setMainColor(Color.WHITE);
        //chart.getView().setBackgroundColor(Color.BLACK);
        chart.getScene().add(scatter);


        amoeba.getEnvironment().trace(TRACE_LEVEL.CYCLE, new ArrayList<String>(Arrays.asList("UPDATE CONTEXTS 3D MODEL VIEW")));

        return chart;
    }


    double model(double x, double y){
        Double[] request = new Double[2];
        request[0]=x;
        request[1]=y;
        return ((F_N_Manager)(studiedSystem)).model(request);
    }

    private Color getColor(float min, float max, float prediction) {

        float r = 0.0f;
        float g = 0.0f;
        float b = 0.0f;


        float range = max-min;
        float middle = (max+min)/2;
        if(prediction < middle){
            r = (prediction-min)/(range/2);
            b = (middle-prediction)/(range/2);
        }else{
            g = (prediction-middle)/(range/2);
            r = 1.0f;
        }





        r = Math.min(r, 1.0f);
        g = Math.min(g, 1.0f);
        b = Math.min(b, 1.0f);
        r = Math.max(r, 0.0f);
        g = Math.max(g, 0.0f);
        b = Math.max(b, 0.0f);
        return new Color(r, g, b, 0.75f);
    }
}

/* WALID

    GridPane gridpane = new GridPane();

    ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);

                ColumnConstraints column2 = new ColumnConstraints();
                column2.setPercentWidth(50);
                gridpane.getColumnConstraints().addAll(column1, column2); // each get 50% of width

                RowConstraints row0 = new RowConstraints();
                row0.setPercentHeight(100);
                gridpane.getRowConstraints().add(row0);

                gridpane.add(new Button(), 0, 0); // column=1 row=0
                gridpane.add(new Label("hdbvfkj"), 1, 0);  // column=2 row=0
                gridpane.setGridLinesVisible(true);*/