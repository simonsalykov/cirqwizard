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
import org.cirqwizard.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.List;


public class TraceLayer extends Layer
{
    private List<GerberPrimitive> elements = new ArrayList<>();
    private List<Toolpath> toolpaths = new ArrayList<>();

    public void setElements(ArrayList<GerberPrimitive> elements)
    {
        this.elements = elements;
    }

    public List<GerberPrimitive> getElements()
    {
        return elements;
    }

    public List<Toolpath> getToolpaths()
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
        int minX = elements.stream().mapToInt(p -> p.getMin().getX()).min().getAsInt();
        int minY = elements.stream().mapToInt(p -> p.getMin().getY()).min().getAsInt();
        return new Point(minX, minY);
    }

    @Override
    public Point getMaxPoint()
    {
        int maxX = elements.stream().mapToInt(p -> p.getMax().getX()).max().getAsInt();
        int maxY = elements.stream().mapToInt(p -> p.getMax().getY()).max().getAsInt();
        return new Point(maxX, maxY);
    }

    public void setToolpaths(List<Toolpath> toolpaths)
    {
        this.toolpaths = toolpaths;
    }

}
