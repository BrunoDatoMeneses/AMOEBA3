package gui;

import agents.context.Context;
import agents.context.localModel.LocalModelMillerRegression;
import agents.percept.Percept;
import experiments.UI_PARAMS;
import experiments.nDimensionsLaunchers.F_N_Manager;
import experiments.tests.plot3Dpolygon.ACUbe;
import gui.utils.ContextColor;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import kernel.AMOEBA;
import kernel.StudiedSystem;
import org.jzy3d.chart.AWTChart;
import org.jzy3d.colors.Color;
import org.jzy3d.javafx.JavaFXChartFactory;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.*;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import utils.TRACE_LEVEL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class View3DContexts {

    public String title;

    public AMOEBA amoeba;

    private BorderPane pane;





    public BorderPane getPane() {
        return pane;
    }




    float increment = 0.5f;

    AWTChart chart1;
    ImageView imageView1;



    public View3DContexts(AMOEBA amb) {


        amoeba = amb;



        pane = new BorderPane();

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
                imageView1.setFitHeight(100);
                imageView1.setFitWidth(100);

                // JavaFX

                pane.setCenter(imageView1);

                imageView1.fitWidthProperty().bind(pane.widthProperty());
                imageView1.fitHeightProperty().bind(pane.heightProperty());

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
                chart1  = get3DContextWithsPolygonsPlotChart(factory1, "offscreen");
                imageView1 = factory1.bindImageView(chart1);

                // JavaFX

                pane.setCenter(imageView1);
                factory1.addSceneSizeChangedListener(chart1, pane.getScene());
                imageView1.fitWidthProperty().bind(pane.widthProperty());
                imageView1.fitHeightProperty().bind(pane.heightProperty());



            }
        });


    }





    private AWTChart getScatterPlotChart(JavaFXChartFactory factory, String toolkit) {

        float spaceSize = 10;
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
                z = x+y;

                points[i] = new Coord3d(x, y, z);
                colors[i] = getColor((float)UI_PARAMS.minPrediction,(float)UI_PARAMS.maxPrediction, z);


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




    public AWTChart get3DContextWithsPolygonsPlotChart(JavaFXChartFactory factory, String toolkit){


        HistogramBar myBar = new HistogramBar();

        Percept pct1 =  amoeba.getDimensionSelector3D().d1();
        Percept pct2 =  amoeba.getDimensionSelector3D().d2();
        Percept pct3 =  amoeba.getDimensionSelector3D().d3();

        for (Context ctxt : amoeba.getContexts()){

            float xStart = (float)ctxt.getRanges().get(pct1).getStart();
            float xEnd = (float)ctxt.getRanges().get(pct1).getEnd();
            float yStart = (float)ctxt.getRanges().get(pct2).getStart();
            float yEnd = (float)ctxt.getRanges().get(pct2).getEnd();
            float zStart = (float)ctxt.getRanges().get(pct3).getStart();
            float zEnd = (float)ctxt.getRanges().get(pct3).getEnd();

            //addCubePoints(pointAAjouter, predictions, correspondingContexts, ctxt, xStart, xEnd, yStart, yEnd, zStart, zEnd);

            Color ctxtColor;
            if(UI_PARAMS.contextColorByCoef){
                ctxtColor = getColorFromCoefs(ctxt);
            }else{
                ctxtColor = getColor((float)UI_PARAMS.minPrediction,(float)UI_PARAMS.maxPrediction, (float) (ctxt.lastPrediction.doubleValue()) );
            }

            Shape ctxtShape = createContextShape(ctxtColor, xStart, xEnd, yStart, yEnd, zStart, zEnd);
            myBar.add(ctxtShape);

        }



        //stage.setTitle(ACUbe.class.getSimpleName());
        //JavaFXChartFactory factory = new JavaFXChartFactory();
        Quality quality = Quality.Advanced;
        quality.setSmoothPolygon(true);
        //quality.setAnimated(true);
        // let factory bind mouse and keyboard controllers to JavaFX node
        AWTChart chart = (AWTChart) factory.newChart(quality, toolkit);
        chart.getScene().getGraph().add(myBar);


        return  chart;

    }




    public AWTChart getScatterPlotChartFromContexts(JavaFXChartFactory factory, String toolkit) {



        ArrayList<Coord3d> pointAAjouter = new ArrayList<>();
        ArrayList<Double> predictions = new ArrayList<>();
        ArrayList<Context> correspondingContexts = new ArrayList<>();

        Percept pct1 =  amoeba.getDimensionSelector3D().d1();
        Percept pct2 =  amoeba.getDimensionSelector3D().d2();
        Percept pct3 =  amoeba.getDimensionSelector3D().d3();

        for (Context ctxt : amoeba.getContexts()){




            float xStart = (float)ctxt.getRanges().get(pct1).getStart();
            float xEnd = (float)ctxt.getRanges().get(pct1).getEnd();
            float yStart = (float)ctxt.getRanges().get(pct2).getStart();
            float yEnd = (float)ctxt.getRanges().get(pct2).getEnd();
            float zStart = (float)ctxt.getRanges().get(pct3).getStart();
            float zEnd = (float)ctxt.getRanges().get(pct3).getEnd();

            //addCubePoints(pointAAjouter, predictions, correspondingContexts, ctxt, xStart, xEnd, yStart, yEnd, zStart, zEnd);

            pointsCubeXStart(pointAAjouter, predictions, correspondingContexts, ctxt, xStart, yStart, yEnd, zStart, zEnd);
            pointsCubeXEnd(pointAAjouter, predictions, correspondingContexts, ctxt, xEnd, yStart, yEnd, zStart, zEnd);
            pointsCubeYStart(pointAAjouter, predictions, correspondingContexts, ctxt, yStart, xStart, xEnd, zStart, zEnd);
            pointsCubeYEnd(pointAAjouter, predictions, correspondingContexts, ctxt, yEnd,  xStart, xEnd, zStart, zEnd);
            pointsCubeZStart(pointAAjouter, predictions, correspondingContexts, ctxt, zStart, xStart, xEnd, yStart, yEnd);
            pointsCubeZEnd(pointAAjouter, predictions, correspondingContexts, ctxt, zEnd, xStart, xEnd, yStart, yEnd);


        }

        Coord3d[] points = new Coord3d[pointAAjouter.size()];
        Color[]   colors = new Color[pointAAjouter.size()];

        int i =0;
        for( Coord3d coord : pointAAjouter){
            points[i] = coord;
            if(UI_PARAMS.contextColorByCoef){
                colors[i] = getColorFromCoefs(correspondingContexts.get(i));
            }else{
                colors[i] = getColor((float)UI_PARAMS.minPrediction,(float)UI_PARAMS.maxPrediction, (float) (predictions.get(i).doubleValue()) );
            }

            i++;
        }

        Scatter scatter = new Scatter(points, colors);
        Quality quality = Quality.Fastest;
        AWTChart chart = (AWTChart) factory.newChart(quality, toolkit);
        //chart.getAxeLayout().setMainColor(Color.WHITE);
        //chart.getView().setBackgroundColor(Color.BLACK);
        chart.getScene().add(scatter);


        amoeba.getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("UPDATE CONTEXTS 3D MODEL VIEW")));

        return chart;
    }

    private void addCubePoints(ArrayList<Coord3d> pointAAjouter, ArrayList<Double> predictions, ArrayList<Context> correspondingContexts, Context ctxt, float xStart, float xEnd, float yStart, float yEnd, float zStart, float zEnd) {
        float x=xStart;
        float y;
        float z;


        while(x<=xEnd){
            y = yStart;
            while(y<=yEnd){
                z = zStart;
                while(z<=zEnd){
                    int pct1Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d1());
                    int pct2Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d2());
                    int pct3Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d3());

                    double[] perception = new double[3];
                    for(int i=0;i<perception.length;i++){
                        if(i == pct1Index){
                            perception[i] = x;
                        }else if(i == pct2Index){
                            perception[i] = y;
                        }else if(i == pct3Index){
                            perception[i] = z;
                        }else{
                            perception[i] = 0.0;
                        }
                    }

                    pointAAjouter.add(new Coord3d(x, y, z));
                    predictions.add(ctxt.lastPrediction);
                    correspondingContexts.add(ctxt);

                    z += increment*3;
                }
                y += increment*3;
            }
            x+= increment*3;
        }
    }

    private void pointsCubeXStart(ArrayList<Coord3d> pointAAjouter, ArrayList<Double> predictions, ArrayList<Context> correspondingContexts, Context ctxt, float xStart, float yStart, float yEnd, float zStart, float zEnd) {
        float x;
        float y;
        float z;
        x=xStart;
        y = yStart;
        while(y<=yEnd){
            z = zStart;
            while(z<=zEnd){
                int pct1Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d1());
                int pct2Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d2());
                int pct3Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d3());

                double[] perception = new double[3];
                for(int i=0;i<perception.length;i++){
                    if(i == pct1Index){
                        perception[i] = x;
                    }else if(i == pct2Index){
                        perception[i] = y;
                    }else if(i == pct3Index){
                        perception[i] = z;
                    }else{
                        perception[i] = 0.0;
                    }
                }

                pointAAjouter.add(new Coord3d(x, y, z));
                predictions.add(ctxt.lastPrediction);
                correspondingContexts.add(ctxt);

                z += increment*3;
            }
            y += increment*3;
        }
    }

    private void pointsCubeXEnd(ArrayList<Coord3d> pointAAjouter, ArrayList<Double> predictions, ArrayList<Context> correspondingContexts, Context ctxt, float xEnd, float yStart, float yEnd, float zStart, float zEnd) {
        float x;
        float y;
        float z;
        x=xEnd;
        y = yStart;
        while(y<=yEnd){
            z = zStart;
            while(z<=zEnd){
                int pct1Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d1());
                int pct2Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d2());
                int pct3Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d3());

                double[] perception = new double[3];
                for(int i=0;i<perception.length;i++){
                    if(i == pct1Index){
                        perception[i] = x;
                    }else if(i == pct2Index){
                        perception[i] = y;
                    }else if(i == pct3Index){
                        perception[i] = z;
                    }else{
                        perception[i] = 0.0;
                    }
                }

                pointAAjouter.add(new Coord3d(x, y, z));
                predictions.add(ctxt.lastPrediction);
                correspondingContexts.add(ctxt);

                z += increment*3;
            }
            y += increment*3;
        }
    }

    private void pointsCubeYStart(ArrayList<Coord3d> pointAAjouter, ArrayList<Double> predictions, ArrayList<Context> correspondingContexts, Context ctxt, float yStart, float xStart, float xEnd, float zStart, float zEnd) {
        float x;
        float y;
        float z;
        y=yStart;
        x = xStart;
        while(x<=xEnd){
            z = zStart;
            while(z<=zEnd){
                int pct1Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d1());
                int pct2Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d2());
                int pct3Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d3());

                double[] perception = new double[3];
                for(int i=0;i<perception.length;i++){
                    if(i == pct1Index){
                        perception[i] = x;
                    }else if(i == pct2Index){
                        perception[i] = y;
                    }else if(i == pct3Index){
                        perception[i] = z;
                    }else{
                        perception[i] = 0.0;
                    }
                }

                pointAAjouter.add(new Coord3d(x, y, z));
                predictions.add(ctxt.lastPrediction);
                correspondingContexts.add(ctxt);

                z += increment*3;
            }
            x += increment*3;
        }
    }

    private void pointsCubeYEnd(ArrayList<Coord3d> pointAAjouter, ArrayList<Double> predictions, ArrayList<Context> correspondingContexts, Context ctxt, float yEnd, float xStart, float xEnd, float zStart, float zEnd) {
        float x;
        float y;
        float z;
        y=yEnd;
        x = xStart;
        while(x<=xEnd){
            z = zStart;
            while(z<=zEnd){
                int pct1Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d1());
                int pct2Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d2());
                int pct3Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d3());

                double[] perception = new double[3];
                for(int i=0;i<perception.length;i++){
                    if(i == pct1Index){
                        perception[i] = x;
                    }else if(i == pct2Index){
                        perception[i] = y;
                    }else if(i == pct3Index){
                        perception[i] = z;
                    }else{
                        perception[i] = 0.0;
                    }
                }

                pointAAjouter.add(new Coord3d(x, y, z));
                predictions.add(ctxt.lastPrediction);
                correspondingContexts.add(ctxt);

                z += increment*3;
            }
            x += increment*3;
        }
    }

    private void pointsCubeZStart(ArrayList<Coord3d> pointAAjouter, ArrayList<Double> predictions, ArrayList<Context> correspondingContexts, Context ctxt, float zStart, float xStart, float xEnd, float yStart, float yEnd) {
        float x;
        float y;
        float z;
        z=zStart;
        x = xStart;
        while(x<=xEnd){
            y = yStart;
            while(y<=yEnd){
                int pct1Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d1());
                int pct2Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d2());
                int pct3Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d3());

                double[] perception = new double[3];
                for(int i=0;i<perception.length;i++){
                    if(i == pct1Index){
                        perception[i] = x;
                    }else if(i == pct2Index){
                        perception[i] = y;
                    }else if(i == pct3Index){
                        perception[i] = z;
                    }else{
                        perception[i] = 0.0;
                    }
                }

                pointAAjouter.add(new Coord3d(x, y, z));
                predictions.add(ctxt.lastPrediction);
                correspondingContexts.add(ctxt);

                y += increment*3;
            }
            x += increment*3;
        }
    }

    private void pointsCubeZEnd(ArrayList<Coord3d> pointAAjouter, ArrayList<Double> predictions, ArrayList<Context> correspondingContexts, Context ctxt, float zEnd, float xStart, float xEnd, float yStart, float yEnd) {
        float x;
        float y;
        float z;
        z=zEnd;
        x = xStart;
        while(x<=xEnd){
            y = yStart;
            while(y<=yEnd){
                int pct1Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d1());
                int pct2Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d2());
                int pct3Index =  amoeba.getPercepts().indexOf(amoeba.getDimensionSelector3D().d3());

                double[] perception = new double[3];
                for(int i=0;i<perception.length;i++){
                    if(i == pct1Index){
                        perception[i] = x;
                    }else if(i == pct2Index){
                        perception[i] = y;
                    }else if(i == pct3Index){
                        perception[i] = z;
                    }else{
                        perception[i] = 0.0;
                    }
                }

                pointAAjouter.add(new Coord3d(x, y, z));
                predictions.add(ctxt.lastPrediction);
                correspondingContexts.add(ctxt);

                y += increment*3;
            }
            x += increment*3;
        }
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


    private Color getColorFromCoefs(Context ctxt){
        Double[] c = ContextColor.colorFromCoefs(ctxt.getFunction().getCoef());
        return new Color((float)c[0].doubleValue(), (float)c[1].doubleValue(), (float)c[2].doubleValue(), 0.75f);

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
