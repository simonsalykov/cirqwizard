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

import org.cirqwizard.generation.toolpath.CircularToolpath;
import org.cirqwizard.generation.toolpath.LinearToolpath;
import org.cirqwizard.generation.toolpath.Toolpath;
import org.cirqwizard.gerber.CircularShape;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.gerber.LinearShape;

import java.util.ArrayList;
import java.util.List;

public class MillingToolpathGenerator
{
    private List<GerberPrimitive> primitives;

    public MillingToolpathGenerator(List<GerberPrimitive> primitives)
    {
        this.primitives = primitives;
    }

    public List<Toolpath> generate()
    {
        List<Toolpath> toolpaths = new ArrayList<>();
        for (GerberPrimitive element : primitives)
        {
            if (element instanceof LinearShape)
            {
                LinearShape shape = (LinearShape) element;
                toolpaths.add(new LinearToolpath(element.getAperture().getWidth(), shape.getFrom(), shape.getTo()));
            }
            else if (element instanceof CircularShape)
            {
                CircularShape shape = (CircularShape) element;
                toolpaths.add(new CircularToolpath(element.getAperture().getWidth(), shape.getFrom(), shape.getTo(), shape.getArc().getCenter(), shape.getArc().getRadius(),
                        shape.getArc().isClockwise()));
            }
        }
        return toolpaths;
    }

}
