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

import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Circle;
import org.cirqwizard.geom.Curve;
import org.cirqwizard.geom.Line;
import org.cirqwizard.toolpath.CircularToolpath;
import org.cirqwizard.toolpath.LinearToolpath;
import org.cirqwizard.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.List;


public class Tracer
{
    private byte[] windowData;

    private int width;
    private int height;
    private int toolDiameter;
    private List<Circle> knownCircles;

    public Tracer(byte[] windowData, int width, int height, int toolDiameter, List<Circle> knownCircles)
    {
        this.windowData = windowData;
        this.width = width;
        this.height = height;
        this.toolDiameter = toolDiameter;
        this.knownCircles = knownCircles;
    }

    public List<Toolpath> process()
    {
        ArrayList<Toolpath> result = new ArrayList<>();
        for (int y = height - 1; y >= 0; y--)
        {
            for (int x = width - 1; x >= 0; x--)
            {
                int index = x + y * width;
                if (windowData[index] != 0)
                {
                    List<Curve> curves = new Vectorizer(windowData, width, height, knownCircles, x, y).trace();
                    for (Curve curve : curves)
                        result.add(getToolpath(curve));
                }
            }
        }

        return result;
    }

    private Toolpath getToolpath(Curve curve)
    {
        if (curve instanceof Line)
            return new LinearToolpath(toolDiameter, curve.getFrom(), curve.getTo());
        else if (curve instanceof Arc)
        {
            Arc arc = (Arc) curve;
            return new CircularToolpath(toolDiameter, arc.getFrom(), arc.getTo(), arc.getCenter(), arc.getRadius(), arc.isClockwise());
        }

        throw new IllegalArgumentException("Unexpected curve supplied: " + curve);
    }


}


