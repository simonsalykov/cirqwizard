package org.cirqwizard.generation;

import com.vividsolutions.jts.geom.*;
import javafx.beans.property.BooleanProperty;
import org.cirqwizard.generation.optimizer.Chain;
import org.cirqwizard.generation.toolpath.LinearToolpath;
import org.cirqwizard.generation.toolpath.Toolpath;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.GerberPrimitive;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by simon on 24.05.17.
 */
public class VectorToolPathGenerator extends AbstractToolpathGenerator
{
    private static final PrecisionModel precisionModel = new PrecisionModel(PrecisionModel.FIXED);
    public static final GeometryFactory factory = new GeometryFactory(precisionModel);

    private int width;
    private int height;
    private int toolDiameter;
    private BooleanProperty cancelledProperty;

    public void init(int width, int height, int inflation, int toolDiameter, List<GerberPrimitive> primitives,
                     BooleanProperty cancelledProperty)
    {
        this.width = width;
        this.height = height;
        this.inflation = inflation;
        this.toolDiameter = toolDiameter;
        this.primitives = primitives;
        this.cancelledProperty = cancelledProperty;
    }

    private Chain processCoordinates(Coordinate[] coordinates)
    {
        List<Toolpath> toolpaths = new ArrayList<>();
        for (int i = 0; i < coordinates.length - 1; i++)
        {
            Point from = new Point((int) coordinates[i].x, (int) coordinates[i].y);
            Point to= new Point((int) coordinates[i + 1].x, (int) coordinates[i + 1].y);
            toolpaths.add(new LinearToolpath(toolDiameter, from, to));
        }
        return new Chain(toolpaths);
    }

    private void processPolygon(List<Chain> chains, Polygon polygon)
    {
        chains.add(processCoordinates(polygon.getExteriorRing().getCoordinates()));
        for (int i = 0; i < polygon.getNumInteriorRing(); i++)
            chains.add(processCoordinates(polygon.getInteriorRingN(i).getCoordinates()));
    }

    public List<Chain> generate()
    {
        List<Chain> chains = new ArrayList<>();
        Polygon[] polygons = primitives.stream().map(p -> p.createPolygon(inflation)).filter(Objects::nonNull).toArray(Polygon[]::new);
        Geometry union = factory.createGeometryCollection(polygons).buffer(0);
        if (union instanceof Polygon)
            processPolygon(chains, (Polygon) union);
        else
        {
            MultiPolygon g = (MultiPolygon) union;
            for (int j = 0; j < g.getNumGeometries(); j++)
                processPolygon(chains, (Polygon) g.getGeometryN(j));
        }
        return chains;
    }

}
