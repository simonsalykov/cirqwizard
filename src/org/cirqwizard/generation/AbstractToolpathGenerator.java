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
import org.cirqwizard.appertures.CircularAperture;
import org.cirqwizard.appertures.macro.ApertureMacro;
import org.cirqwizard.appertures.macro.MacroCircle;
import org.cirqwizard.appertures.macro.MacroPrimitive;
import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Circle;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.gerber.InterpolatingShape;
import org.cirqwizard.settings.Settings;
import org.cirqwizard.toolpath.CircularToolpath;
import org.cirqwizard.toolpath.CuttingToolpath;
import org.cirqwizard.toolpath.LinearToolpath;
import org.cirqwizard.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.List;

public class AbstractToolpathGenerator
{
    protected int inflation;
    protected List<GerberPrimitive> primitives;

    protected ArrayList<Circle> knownCircles = new ArrayList<>();

    protected DoubleProperty progressProperty = new SimpleDoubleProperty();

    protected void initRadii()
    {
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
                else if (primitive instanceof InterpolatingShape && aperture.getDiameter() > 1 * Settings.RESOLUTION)
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

    }

    public DoubleProperty progressProperty()
    {
        return progressProperty;
    }


    protected List<Circle> translateKnownCircles(Point offset)
    {
        ArrayList<Circle> translatedCircles = new ArrayList<>();
        for (Circle circle : knownCircles)
        {
            Point p  = translateToWindowCoordinates(circle.getCenter(), offset);
            translatedCircles.add(new Circle(p, circle.getRadius()));
        }

        return translatedCircles;
    }

    protected Point translateWindowCoordiantes(Point point, Point windowOffset)
    {
        return new Point(point.getX(), point.getY()).add(windowOffset);
    }

    protected Point translateToWindowCoordinates(Point point, Point windowOffset)
    {
        return new Point(point.getX(), point.getY()).subtract(windowOffset);
    }

    protected java.util.List<Toolpath> translateToolpaths(java.util.List<Toolpath> toolpaths, Point offset)
    {
        ArrayList<Toolpath> result = new ArrayList<>();
        for (Toolpath toolpath : toolpaths)
        {
            if (((CuttingToolpath)toolpath).getCurve().getFrom().equals(((CuttingToolpath)toolpath).getCurve().getTo()))
                continue;
            if (toolpath instanceof LinearToolpath)
            {
                LinearToolpath lt = (LinearToolpath) toolpath;
                Point start = translateWindowCoordiantes(lt.getCurve().getFrom(), offset);
                Point end = translateWindowCoordiantes(lt.getCurve().getTo(), offset);
                result.add(new LinearToolpath(((LinearToolpath) toolpath).getToolDiameter(), start, end));
            }
            else if (toolpath instanceof CircularToolpath)
            {
                CircularToolpath ct = (CircularToolpath) toolpath;
                Arc arc = (Arc) ct.getCurve();
                Point start = translateWindowCoordiantes(ct.getCurve().getFrom(), offset);
                Point end = translateWindowCoordiantes(ct.getCurve().getTo(), offset);
                Point center = translateWindowCoordiantes(arc.getCenter(), offset);
                int radius = arc.getRadius();
                result.add(new CircularToolpath(ct.getToolDiameter(), start, end, center, radius, arc.isClockwise()));
            }
        }

        return result;
    }

}
