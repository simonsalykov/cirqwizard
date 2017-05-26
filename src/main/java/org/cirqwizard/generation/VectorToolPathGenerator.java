package org.cirqwizard.generation;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import javafx.beans.property.BooleanProperty;
import org.cirqwizard.generation.optimizer.Chain;
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


    public List<Chain> generate()
    {
        Geometry resultingGeometry = createLayerGeometry(inflation);

        List<Chain> chains = new ArrayList<>();
        addChains(resultingGeometry, chains, toolDiameter);
        for (int i = 0; i < additionalPasses; i++)
        {
            int offset = toolDiameter * (100 - additionalPassesOverlap) / 100;
            resultingGeometry = resultingGeometry.buffer(offset);
            addChains(resultingGeometry, chains, toolDiameter);
        }
        return chains;
    }


}
