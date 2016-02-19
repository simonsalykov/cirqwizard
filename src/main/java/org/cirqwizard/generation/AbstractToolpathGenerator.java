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

package org.cirqwizard.generation;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.cirqwizard.gerber.appertures.CircularAperture;
import org.cirqwizard.gerber.appertures.macro.ApertureMacro;
import org.cirqwizard.gerber.appertures.macro.MacroCircle;
import org.cirqwizard.gerber.appertures.macro.MacroPrimitive;
import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Circle;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.gerber.InterpolatingShape;
import org.cirqwizard.settings.ApplicationConstants;
import org.cirqwizard.generation.toolpath.CircularToolpath;
import org.cirqwizard.generation.toolpath.CuttingToolpath;
import org.cirqwizard.generation.toolpath.LinearToolpath;
import org.cirqwizard.generation.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.List;

public class AbstractToolpathGenerator
{
    protected int inflation;
    protected List<GerberPrimitive> primitives;

    protected DoubleProperty progressProperty = new SimpleDoubleProperty();

    protected List<Circle> getKnownCircles(int inflation)
    {
        ArrayList<Circle> knownCircles = new ArrayList<>();
        for (GerberPrimitive primitive : primitives)
        {
            if (primitive.getAperture() instanceof CircularAperture)
            {
                CircularAperture aperture = (CircularAperture) primitive.getAperture();
                if (primitive instanceof Flash)
                {
                    Flash f = (Flash) primitive;
                    knownCircles.add(new Circle(f.getPoint(), aperture.getDiameter() / 2 + inflation));
                }
                else if (primitive instanceof InterpolatingShape && aperture.getDiameter() > 1 * ApplicationConstants.RESOLUTION)
                {
                    InterpolatingShape shape = (InterpolatingShape) primitive;
                    knownCircles.add(new Circle(shape.getFrom(), aperture.getDiameter() / 2 + inflation));
                    knownCircles.add(new Circle(shape.getTo(), aperture.getDiameter() / 2 + inflation));
                }
            }
            else if (primitive.getAperture() instanceof ApertureMacro && primitive instanceof Flash)
            {
                Flash f = (Flash) primitive;
                for (MacroPrimitive p : ((ApertureMacro) primitive.getAperture()).getPrimitives())
                {
                    if (p instanceof MacroCircle)
                    {
                        MacroCircle mc = (MacroCircle) p;
                        knownCircles.add(new Circle(f.getPoint().add(mc.getCenter()), mc.getDiameter() / 2 + inflation));
                    }
                }
            }
        }
        return knownCircles;
    }

    public DoubleProperty progressProperty()
    {
        return progressProperty;
    }


    protected List<Circle> translateKnownCircles(Point offset, double scale, List<Circle> knownCircles)
    {
        ArrayList<Circle> translatedCircles = new ArrayList<>();
        for (Circle circle : knownCircles)
        {
            Point p  = translateToWindowCoordinates(circle.getCenter(), offset, scale);
            translatedCircles.add(new Circle(p, (int)(scale * circle.getRadius())));
        }

        return translatedCircles;
    }

    protected Point translateWindowCoordiantes(Point point, Point windowOffset, double scale)
    {
        return new Point((int)((double)point.getX() / scale), (int)((double)point.getY() / scale)).add(windowOffset);
    }

    protected Point translateToWindowCoordinates(Point point, Point windowOffset, double scale)
    {
        return new Point((int)(scale * point.getX()), (int)(scale * point.getY())).subtract(windowOffset);
    }

    protected java.util.List<Toolpath> translateToolpaths(java.util.List<Toolpath> toolpaths, Point offset, double scale)
    {
        ArrayList<Toolpath> result = new ArrayList<>();
        for (Toolpath toolpath : toolpaths)
        {
            if (((CuttingToolpath)toolpath).getCurve().getFrom().equals(((CuttingToolpath)toolpath).getCurve().getTo()))
                continue;
            if (toolpath instanceof LinearToolpath)
            {
                LinearToolpath lt = (LinearToolpath) toolpath;
                Point start = translateWindowCoordiantes(lt.getCurve().getFrom(), offset, scale);
                Point end = translateWindowCoordiantes(lt.getCurve().getTo(), offset, scale);
                result.add(new LinearToolpath(((LinearToolpath) toolpath).getToolDiameter(), start, end));
            }
            else if (toolpath instanceof CircularToolpath)
            {
                CircularToolpath ct = (CircularToolpath) toolpath;
                Arc arc = (Arc) ct.getCurve();
                Point start = translateWindowCoordiantes(ct.getCurve().getFrom(), offset, scale);
                Point end = translateWindowCoordiantes(ct.getCurve().getTo(), offset, scale);
                Point center = translateWindowCoordiantes(arc.getCenter(), offset, scale);
                int radius = (int)((double)arc.getRadius() / scale);
                result.add(new CircularToolpath(ct.getToolDiameter(), start, end, center, radius, arc.isClockwise()));
            }
        }

        return result;
    }

}
