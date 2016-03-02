package org.cirqwizard.generation.outline;

import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.gerber.LinearShape;
import org.cirqwizard.gerber.appertures.Aperture;
import org.cirqwizard.gerber.appertures.CircularAperture;
import org.cirqwizard.layers.LayerElement;
import org.cirqwizard.layers.PanelBoard;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simon on 02.03.16.
 */
public class OutlineGenerator
{
    private static final int TOOL_DIAMETER = 1000;
    private static final int TAB_LENGTH = 2000;
    private static final int DRILL_DIAMETER = 600;
    private static final int HOLES_COUNT = 2;

    private PanelBoard board;

    public OutlineGenerator(PanelBoard board)
    {
        this.board = board;
    }

    public List<LayerElement> generateOutline()
    {
        Point p1 = new Point(-TOOL_DIAMETER / 2, -TOOL_DIAMETER / 2);
        Point p2 = new Point(-TOOL_DIAMETER / 2, board.getBoard().getHeight() + TOOL_DIAMETER / 2);
        Point p3 = new Point(board.getBoard().getWidth() + TOOL_DIAMETER / 2,
                board.getBoard().getHeight() + TOOL_DIAMETER / 2);
        Point p4 = new Point(board.getBoard().getWidth() + TOOL_DIAMETER / 2, -TOOL_DIAMETER / 2);
        ArrayList<LayerElement> result = new ArrayList<>();
        result.addAll(generateLines(p1, p2));
        result.addAll(generateLines(p2, p3));
        result.addAll(generateLines(p3, p4));
        result.addAll(generateLines(p4, p1));
        return result;
    }

    private List<GerberPrimitive> generateLines(Point from, Point to)
    {
        Point delta = to.subtract(from);
        Point midPoint = new Point(from.getX() + delta.getX() / 2, from.getY() + delta.getY() / 2);
        int adjustedTabLength = TAB_LENGTH + TOOL_DIAMETER / 2;
        Point tabLengthAdjustment = new Point((delta.getX() == 0 ? 0 : adjustedTabLength) * Integer.signum(delta.getX()),
                (delta.getY() == 0 ? 0 : adjustedTabLength) * Integer.signum(delta.getY()));
        ArrayList<GerberPrimitive> result = new ArrayList<>();
        Aperture aperture = new CircularAperture(TOOL_DIAMETER);
        Point midPoint1 = midPoint.subtract(tabLengthAdjustment);
        result.add(new LinearShape(from.getX(), from.getY(), midPoint1.getX(), midPoint1.getY(), aperture,
                GerberPrimitive.Polarity.DARK));
        Point midPoint2 = midPoint.add(tabLengthAdjustment);
        result.add(new LinearShape(midPoint2.getX(), midPoint2.getY(), to.getX(), to.getY(), aperture,
                GerberPrimitive.Polarity.DARK));
        return result;
    }

}
