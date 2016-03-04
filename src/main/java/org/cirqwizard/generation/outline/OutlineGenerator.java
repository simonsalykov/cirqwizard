package org.cirqwizard.generation.outline;

import org.cirqwizard.generation.toolpath.DrillPoint;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.gerber.LinearShape;
import org.cirqwizard.gerber.appertures.Aperture;
import org.cirqwizard.gerber.appertures.CircularAperture;
import org.cirqwizard.layers.Board;
import org.cirqwizard.layers.LayerElement;
import org.cirqwizard.layers.PanelBoard;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simon on 02.03.16.
 */
public class OutlineGenerator
{
    public static final int TOOL_DIAMETER = 1000;
    private static final int DRILL_DIAMETER = 600;
    private static final int HOLES_COUNT = 4;
    private static final int HOLES_SPACING = 250;

    private PanelBoard board;

    public OutlineGenerator(PanelBoard board)
    {
        this.board = board;
    }

    public void generate()
    {
        Point[] points = getExtremePoints(TOOL_DIAMETER  / 2);
        ArrayList<LayerElement> contourShapes = new ArrayList<>();
        contourShapes.addAll(generateLines(points[0], points[1]));
        contourShapes.addAll(generateLines(points[1], points[2]));
        contourShapes.addAll(generateLines(points[2], points[3]));
        contourShapes.addAll(generateLines(points[3], points[0]));
        board.getBoard().getLayer(Board.LayerType.MILLING).setElements(contourShapes);

        points = getExtremePoints(DRILL_DIAMETER  / 2);
        ArrayList<LayerElement> drillPoints = new ArrayList<>();
        drillPoints.addAll(board.getBoard().getLayer(Board.LayerType.DRILLING).getElements());
        drillPoints.addAll(generateDrillHoles(points[0], points[1]));
        drillPoints.addAll(generateDrillHoles(points[1], points[2]));
        drillPoints.addAll(generateDrillHoles(points[2], points[3]));
        drillPoints.addAll(generateDrillHoles(points[3], points[0]));
        board.getBoard().getLayer(Board.LayerType.DRILLING).setElements(drillPoints);
    }

    private Point[] getExtremePoints(int offset)
    {
        return new Point[] {
                new Point(-offset, -offset),
                new Point(-offset, board.getBoard().getHeight() + offset),
                new Point(board.getBoard().getWidth() + offset, board.getBoard().getHeight() + offset),
                new Point(board.getBoard().getWidth() + offset, -offset)
        };
    }

    private Point getOffsetMidpoint(Point from, Point to, int offset)
    {
        Point delta = to.subtract(from);
        Point midPoint = new Point(from.getX() + delta.getX() / 2, from.getY() + delta.getY() / 2);
        Point tabLengthAdjustment = new Point((delta.getX() == 0 ? 0 : offset) * Integer.signum(delta.getX()),
                (delta.getY() == 0 ? 0 : offset) * Integer.signum(delta.getY()));
        return midPoint.add(tabLengthAdjustment);
    }

    private List<GerberPrimitive> generateLines(Point from, Point to)
    {
        int adjustedTabLength = (DRILL_DIAMETER * HOLES_COUNT + HOLES_SPACING * (HOLES_COUNT + 1) + TOOL_DIAMETER) / 2;
        ArrayList<GerberPrimitive> result = new ArrayList<>();
        Aperture aperture = new CircularAperture(TOOL_DIAMETER);
        Point midPoint1 = getOffsetMidpoint(from, to, -adjustedTabLength);
        result.add(new LinearShape(from.getX(), from.getY(), midPoint1.getX(), midPoint1.getY(), aperture,
                GerberPrimitive.Polarity.DARK));
        Point midPoint2 = getOffsetMidpoint(from, to, adjustedTabLength);
        result.add(new LinearShape(midPoint2.getX(), midPoint2.getY(), to.getX(), to.getY(), aperture,
                GerberPrimitive.Polarity.DARK));
        return result;
    }

    private List<? extends LayerElement> generateDrillHoles(Point from, Point to)
    {
        int offset = -((DRILL_DIAMETER + HOLES_SPACING) * (HOLES_COUNT - 1)) / 2;
        ArrayList<LayerElement> points = new ArrayList<>();
        for (int i = 0; i < HOLES_COUNT; i++)
        {
            points.add(new DrillPoint(getOffsetMidpoint(from, to, offset), DRILL_DIAMETER));
            offset += DRILL_DIAMETER + HOLES_SPACING;
        }
        return points;
    }

}
