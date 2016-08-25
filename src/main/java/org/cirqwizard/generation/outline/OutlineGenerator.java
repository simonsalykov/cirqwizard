/*
This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 3 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.cirqwizard.generation.outline;

import org.cirqwizard.generation.toolpath.DrillPoint;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.gerber.LinearShape;
import org.cirqwizard.gerber.appertures.Aperture;
import org.cirqwizard.gerber.appertures.CircularAperture;
import org.cirqwizard.layers.Board;
import org.cirqwizard.layers.Layer;
import org.cirqwizard.layers.LayerElement;
import org.cirqwizard.layers.PanelBoard;
import org.cirqwizard.settings.ContourMillingSettings;
import org.cirqwizard.settings.SettingsFactory;

import java.util.ArrayList;
import java.util.List;

public class OutlineGenerator
{
    private PanelBoard board;

    public OutlineGenerator(PanelBoard board)
    {
        this.board = board;
    }

    public void generate()
    {
        ContourMillingSettings settings = SettingsFactory.getContourMillingSettings();
        Point[] points = getExtremePoints(settings.getGenerationToolDiameter().getValue()  / 2);
        ArrayList<LayerElement> contourShapes = new ArrayList<>();
        contourShapes.addAll(generateLines(points[0], points[1]));
        contourShapes.addAll(generateLines(points[1], points[2]));
        contourShapes.addAll(generateLines(points[2], points[3]));
        contourShapes.addAll(generateLines(points[3], points[0]));
        if (board.getBoard().getLayer(Board.LayerType.MILLING) == null)
            board.getBoard().setLayer(Board.LayerType.MILLING, new Layer());
        board.getBoard().getLayer(Board.LayerType.MILLING).setElements(contourShapes);

        points = getExtremePoints(settings.getGenerationDrillDiameter().getValue()  / 2);
        ArrayList<LayerElement> drillPoints = new ArrayList<>();
        if (board.getBoard().getLayer(Board.LayerType.DRILLING) == null)
            board.getBoard().setLayer(Board.LayerType.DRILLING, new Layer());
        else
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
        ContourMillingSettings settings = SettingsFactory.getContourMillingSettings();
        int adjustedTabLength = (settings.getGenerationDrillDiameter().getValue() *
                settings.getGenerationHolesCount().getValue() + settings.getGenerationHolesSpacing().getValue() *
                (settings.getGenerationHolesCount().getValue() + 1) +
                settings.getGenerationToolDiameter().getValue()) / 2;
        ArrayList<GerberPrimitive> result = new ArrayList<>();
        Aperture aperture = new CircularAperture(settings.getGenerationToolDiameter().getValue());
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
        ContourMillingSettings settings = SettingsFactory.getContourMillingSettings();
        int offset = -((settings.getGenerationDrillDiameter().getValue() + settings.getGenerationHolesSpacing().getValue()) *
                (settings.getGenerationHolesCount().getValue() - 1)) / 2;
        ArrayList<LayerElement> points = new ArrayList<>();
        for (int i = 0; i < settings.getGenerationHolesCount().getValue(); i++)
        {
            points.add(new DrillPoint(getOffsetMidpoint(from, to, offset), settings.getGenerationDrillDiameter().getValue()));
            offset += settings.getGenerationDrillDiameter().getValue() + settings.getGenerationHolesSpacing().getValue();
        }
        return points;
    }

}
