package experiments.tests.plot3Dpolygon;


import org.jzy3d.chart.ChartScene;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRBG;
import org.jzy3d.colors.colormaps.ColorMapWhiteRed;
import org.jzy3d.maths.Coord3d;

public class HistogramScene extends ChartScene {
    public HistogramScene(){
        super(true);

        ColorMapWhiteRed map = new ColorMapWhiteRed();
        map.setDirection(false);
        mapper = new ColorMapper(map, 0, 1);
    }

    public void addCylinder(double x, double y, double height){
        if(height>1 || height<0)
            throw new IllegalArgumentException("height is supposed to be a ratio");
        Color color = Color.RED; //mapper.getColor(new Coord3d(0,0,height));
        color.a = 0.55f;

        Cylinder bar = new Cylinder();
        bar.setData(new Coord3d(x, y, 0), (float)height, 7f, 15, 00, color);
        bar.setWireframeDisplayed(true);
        bar.setWireframeColor(Color.BLACK);
        graph.add(bar);
    }
    private ColorMapper mapper;
}
