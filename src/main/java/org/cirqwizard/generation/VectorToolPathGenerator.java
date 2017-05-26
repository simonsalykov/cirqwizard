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

/**
 * Created by simon on 24.05.17.
 */
public class VectorToolPathGenerator extends AbstractToolpathGenerator
{
    private static final PrecisionModel precisionModel = new PrecisionModel(PrecisionModel.FIXED);
    public static final GeometryFactory factory = new GeometryFactory(precisionModel);

    private int toolDiameter;
    private BooleanProperty cancelledProperty;
    private int additionalPasses;
    private int additionalPassesOverlap;

    public VectorToolPathGenerator(int inflation, int toolDiameter, List<GerberPrimitive> primitives, BooleanProperty cancelledProperty,
                     int additionalPasses, int additionalPassesOverlap)
    {
        this.inflation = inflation;
        this.toolDiameter = toolDiameter;
        this.primitives = primitives;
        this.cancelledProperty = cancelledProperty;
        this.additionalPasses = additionalPasses;
        this.additionalPassesOverlap = additionalPassesOverlap;
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

    private Geometry processGeometries(GerberPrimitive.Polarity polarity, Geometry resultingGeometry, List<Geometry> geometriesList)
    {
        Geometry[] geometries = new Geometry[geometriesList.size()];
        geometriesList.toArray(geometries);
        Geometry union = factory.createGeometryCollection(geometries).buffer(0);
        if (polarity == GerberPrimitive.Polarity.DARK)
        {
            if (resultingGeometry == null)
                return union;
            else
                return factory.createGeometryCollection(new Geometry[]{resultingGeometry, union}).buffer(0);
        }
        else
            return resultingGeometry.difference(union);
    }

    public List<Chain> generate()
    {
        GerberPrimitive.Polarity currentPolarity = primitives.get(0).getPolarity();
        List<Geometry> currentGeometryCollection = new ArrayList<>();
        Geometry resultingGeometry = null;
        for (GerberPrimitive p : primitives)
        {
            if (p.getPolarity() != currentPolarity)
            {
                resultingGeometry = processGeometries(currentPolarity, resultingGeometry, currentGeometryCollection);
                currentGeometryCollection = new ArrayList<>();
                currentPolarity = p.getPolarity();
            }
            currentGeometryCollection.add(p.createPolygon(inflation));
        }
        resultingGeometry = processGeometries(currentPolarity, resultingGeometry, currentGeometryCollection);

        List<Chain> chains = new ArrayList<>();

        addChains(resultingGeometry, chains);
        for (int i = 0; i < additionalPasses; i++)
        {
            int offset = toolDiameter * (100 - additionalPassesOverlap) / 100;
            resultingGeometry = resultingGeometry.buffer(offset);
            addChains(resultingGeometry, chains);
        }
        return chains;
    }

    private void addChains(Geometry resultingGeometry, List<Chain> chains)
    {
        if (resultingGeometry instanceof Polygon)
            processPolygon(chains, (Polygon) resultingGeometry);
        else
        {
            MultiPolygon g = (MultiPolygon) resultingGeometry;
            for (int j = 0; j < g.getNumGeometries(); j++)
                processPolygon(chains, (Polygon) g.getGeometryN(j));
        }
    }

}
