package org.cirqwizard.generation;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import javafx.beans.property.BooleanProperty;
import org.cirqwizard.generation.optimizer.Chain;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.layers.PanelBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by simon on 26.05.17.
 */
public class VectorRuboutGenerator extends AbstractToolpathGenerator
{
    private List<PanelBoard> boards;
    private int toolDiameter;
    private BooleanProperty cancelledProperty;
    private int initialOffset;

    public VectorRuboutGenerator(List<PanelBoard> boards, List<GerberPrimitive> primitives, int toolDiameter,
                                 BooleanProperty cancelledProperty, int initialOffset)
    {
        this.boards = boards;
        this.primitives = primitives;
        this.toolDiameter = toolDiameter;
        this.cancelledProperty = cancelledProperty;
        this.initialOffset = initialOffset;
    }

    public List<Chain> generate()
    {
        Geometry layerGeometry = createLayerGeometry(initialOffset);
        List<Polygon> boardOutlines = boards.stream().map(b -> VectorToolPathGenerator.factory.createPolygon(new Coordinate[] {
                new Coordinate(b.getX(), b.getY()),
                new Coordinate(b.getX(), b.getY() + b.getBoard().getHeight()),
                new Coordinate(b.getX() + b.getBoard().getWidth(), b.getY() + b.getBoard().getHeight()),
                new Coordinate(b.getX() + b.getBoard().getWidth() , b.getY()),
                new Coordinate(b.getX(), b.getY())})).collect(Collectors.toList());
        Polygon[] b = new Polygon[boardOutlines.size()];
        boardOutlines.toArray(b);
        Geometry panelOutline = VectorToolPathGenerator.factory.createGeometryCollection(b).buffer(0);

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
