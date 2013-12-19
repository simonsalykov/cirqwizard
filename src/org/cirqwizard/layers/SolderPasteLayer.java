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
            if (p.getMin().getX() < min.getX())
                min = new Point(p.getMin().getX(), min.getY());
            if (p.getMin().getY() < min.getY())
                min = new Point(min.getX(), p.getMin().getY());
        }
        return min;
    }

    public void generateToolpaths(int needleDiameter)
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
                    int[] dimensions = aperture.getDimensions();
                    boolean vertical = dimensions[1] > dimensions[0];

                    int passes = Math.max(1, dimensions[vertical ? 0 : 1] / (needleDiameter * 2));
                    for (int i = 0; i < passes; i++)
                    {
                        LinearToolpath toolpath;
                        if (vertical)
                        {
                            int x = (int)(flash.getX() - dimensions[0] / 2 + (double)dimensions[0] / (passes + 1) * (i + 1));
                            int y = flash.getY() - dimensions[1] / 2 + needleDiameter;
                            toolpath = new LinearToolpath(needleDiameter, new Point(x, y), new Point(x, flash.getY() + (dimensions[1] / 2) - needleDiameter));
                        }
                        else
                        {
                            int x = flash.getX() - dimensions[0] / 2 + needleDiameter;
                            int y = (int)(flash.getY() - dimensions[1] / 2 + (double)dimensions[1] / (passes + 1) * (i + 1));
                            toolpath = new LinearToolpath(needleDiameter, new Point(x, y), new Point(flash.getX() + (dimensions[0] / 2) - needleDiameter, y));
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
