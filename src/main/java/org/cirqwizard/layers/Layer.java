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

import java.util.List;

public class Layer
{
    private List<? extends LayerElement> elements;

    public Layer()
    {
    }

    public Layer(List<? extends LayerElement> elements)
    {
        this.elements = elements;
    }

    public List<? extends LayerElement> getElements()
    {
        return elements;
    }

    public void setElements(List<? extends LayerElement> elements)
    {
        this.elements = elements;
    }

    public Point getMinPoint()
    {
        int minX = elements.stream().mapToInt(p -> p.getMin().getX()).min().getAsInt();
        int minY = elements.stream().mapToInt(p -> p.getMin().getY()).min().getAsInt();
        return new Point(minX, minY);
    }

    public Point getMaxPoint()
    {
        int maxX = elements.stream().mapToInt(p -> p.getMax().getX()).max().getAsInt();
        int maxY = elements.stream().mapToInt(p -> p.getMax().getY()).max().getAsInt();
        return new Point(maxX, maxY);
    }

    public void move(Point p)
    {
        elements.stream().forEach(e -> e.move(p));
    }

    public void rotate(boolean clockwise)
    {
        elements.stream().forEach(e -> e.rotate(clockwise));
    }

}
