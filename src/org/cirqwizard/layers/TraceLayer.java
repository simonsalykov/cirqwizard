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

import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.optimizer.Environment;
import org.cirqwizard.optimizer.Generation;
import org.cirqwizard.optimizer.Optimizer;
import org.cirqwizard.optimizer.Phenotype;
import org.cirqwizard.render.Raster;
import org.cirqwizard.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.List;


public class TraceLayer extends Layer
{
    private List<GerberPrimitive> elements = new ArrayList<GerberPrimitive>();
    private ArrayList<Toolpath> toolpaths = new ArrayList<Toolpath>();

    public void setElements(ArrayList<GerberPrimitive> elements)
    {
        this.elements = elements;
    }

    public List<GerberPrimitive> getElements()
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

    public void generateToolpaths(Raster raster)
    {
        for (GerberPrimitive p : elements)
            raster.addPrimitive(p);
        toolpaths = new ArrayList<>();
        toolpaths.addAll(raster.trace());
        Environment environment = new Environment(toolpaths, 1000.0 / 60, 200.0 / 60, 5.0, 2.0);
        new Optimizer(toolpaths, environment).optimize();
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
