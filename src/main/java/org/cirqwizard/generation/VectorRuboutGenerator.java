package org.cirqwizard.generation;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import javafx.beans.property.BooleanProperty;
import org.cirqwizard.generation.optimizer.Chain;
import org.cirqwizard.gerber.GerberPrimitive;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simon on 26.05.17.
 */
public class VectorRuboutGenerator extends AbstractToolpathGenerator
{
    private int panelWidth;
    private int panelHeight;
    private int toolDiameter;
    private BooleanProperty cancelledProperty;
    private int initialOffset;

    public VectorRuboutGenerator(int panelWidth, int panelHeight, List<GerberPrimitive> primitives, int toolDiameter,
                                 BooleanProperty cancelledProperty, int initialOffset)
    {
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        this.primitives = primitives;
        this.toolDiameter = toolDiameter;
        this.cancelledProperty = cancelledProperty;
        this.initialOffset = initialOffset;
    }

    public List<Chain> generate()
    {
        Geometry layerGeometry = createLayerGeometry(initialOffset);
        Polygon panelOutline = VectorToolPathGenerator.factory.createPolygon(new Coordinate[] {
                new Coordinate(0, 0),
                new Coordinate(0, panelHeight),
                new Coordinate(panelWidth, panelHeight),
                new Coordinate(panelWidth, 0),
                new Coordinate(0, 0)});
        Geometry p = panelOutline.difference(layerGeometry).buffer(-toolDiameter / 2);
        List<Chain> chains = new ArrayList<>();
        while (!p.isEmpty())
        {
            addChains(p, chains, toolDiameter);
            p = p.buffer(-toolDiameter / 2);
        }
        return chains;
    }

}
