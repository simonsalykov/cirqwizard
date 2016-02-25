package org.cirqwizard.generation;

import org.cirqwizard.generation.toolpath.CircularToolpath;
import org.cirqwizard.generation.toolpath.LinearToolpath;
import org.cirqwizard.generation.toolpath.Toolpath;
import org.cirqwizard.gerber.CircularShape;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.gerber.LinearShape;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simon on 25.02.16.
 */
public class MillingToolpathGenerator
{
    private List<GerberPrimitive> primitives;

    public MillingToolpathGenerator(List<GerberPrimitive> primitives)
    {
        this.primitives = primitives;
    }

    public List<Toolpath> generate()
    {
        List<Toolpath> toolpaths = new ArrayList<>();
        for (GerberPrimitive element : primitives)
        {
            if (element instanceof LinearShape)
            {
                LinearShape shape = (LinearShape) element;
                toolpaths.add(new LinearToolpath(element.getAperture().getWidth(), shape.getFrom(), shape.getTo()));
            }
            else if (element instanceof CircularShape)
            {
                CircularShape shape = (CircularShape) element;
                toolpaths.add(new CircularToolpath(element.getAperture().getWidth(), shape.getFrom(), shape.getTo(), shape.getArc().getCenter(), shape.getArc().getRadius(),
                        shape.getArc().isClockwise()));
            }
        }
        return toolpaths;
    }

}
