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

package org.cirqwizard.layers;

import org.cirqwizard.appertures.RectangularAperture;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.toolpath.LinearToolpath;
import org.cirqwizard.toolpath.Toolpath;

import java.util.*;
import java.util.logging.Level;


public class SolderPasteLayer extends Layer
{
    private ArrayList<GerberPrimitive> elements = new ArrayList<GerberPrimitive>();
    private ArrayList<Toolpath> toolpaths = new ArrayList<Toolpath>();

    public void setElements(ArrayList<GerberPrimitive> elements)
    {
        this.elements = elements;
    }

    public ArrayList<GerberPrimitive> getElements()
    {
        return elements;
    }

    public ArrayList<Toolpath> getToolpaths()
    {
        return toolpaths;
    }

    @Override
    public void rotate(boolean clockwise)
    {
        for (GerberPrimitive p : elements)
            p.rotate(clockwise);
    }

    @Override
    public void move(Point point)
    {
        for (GerberPrimitive p : elements)
            p.move(point);
    }

    @Override
    public Point getMinPoint()
    {
        Point min = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (GerberPrimitive p : elements)
        {
            if (p.getMin().getX().lessThan(min.getX()))
                min = new Point(p.getMin().getX(), min.getY());
            if (p.getMin().getY().lessThan(min.getY()))
                min = new Point(min.getX(), p.getMin().getY());
        }
        return min;
    }

    public void generateToolpaths(RealNumber needleDiameter)
    {
        toolpaths = new ArrayList<Toolpath>();
        for (GerberPrimitive element : elements)
        {
            if (element instanceof Flash)
            {
                Flash flash = (Flash)element;
                if (flash.getAperture() instanceof RectangularAperture)
                {
                    RectangularAperture aperture = (RectangularAperture)flash.getAperture();
                    RealNumber[] dimensions = aperture.getDimensions();
                    boolean vertical = dimensions[1].greaterThan(dimensions[0]);
                    RealNumber needleRadius = needleDiameter.divide(2);

                    int passes = Math.max(1, (int) dimensions[vertical ? 0 : 1].divide(needleDiameter.multiply(2)).doubleValue());
                    for (int i = 0; i < passes; i++)
                    {
                        LinearToolpath toolpath;
                        if (vertical)
                        {
                            RealNumber x = flash.getX().subtract(dimensions[0].divide(2)).add(dimensions[0].divide(passes + 1).multiply(i + 1));
                            RealNumber y = flash.getY().subtract(dimensions[1].divide(2)).add(needleRadius.multiply(2));
                            toolpath = new LinearToolpath(needleDiameter, new Point(x, y), new Point(x, flash.getY().add(dimensions[1].divide(2)).subtract(needleRadius.multiply(2))));
                        }
                        else
                        {
                            RealNumber x = flash.getX().subtract(dimensions[0].divide(2)).add(needleRadius.multiply(2));
                            RealNumber y = flash.getY().subtract(dimensions[1].divide(2)).add(dimensions[1].divide(passes + 1).multiply(i + 1));
                            toolpath = new LinearToolpath(needleDiameter, new Point(x, y), new Point(flash.getX().add(dimensions[0].divide(2)).subtract(needleRadius.multiply(2)), y));
                        }
                        toolpaths.add(toolpath);
                    }
                }
                else
                    System.out.println("Circular apertures not supported at the moment");
            }
            else
            {
                LoggerFactory.getApplicationLogger().log(Level.WARNING, "Unexpected element on solder paste level: " + element);
            }
        }
    }

    @Override
    public void clearSelection()
    {
        if (toolpaths == null)
            return;
        for (Toolpath t : toolpaths)
            t.setSelected(false);

    }

}
