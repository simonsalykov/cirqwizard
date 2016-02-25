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

package org.cirqwizard.generation.toolpath;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import org.cirqwizard.geom.Curve;
import org.cirqwizard.geom.Line;
import org.cirqwizard.geom.Point;


public class LinearToolpath extends CuttingToolpath
{
    private Line line;

    public LinearToolpath(int toolDiameter, Point from, Point to)
    {
        super(toolDiameter);
        line = new Line(from, to);
    }

    @Override
    public Curve getCurve()
    {
        return line;
    }

    @Override
    public void render(GraphicsContext g)
    {
        g.setLineCap(StrokeLineCap.ROUND);
        g.setLineWidth(getToolDiameter());
        g.strokeLine(getCurve().getFrom().getX(), getCurve().getFrom().getY(),
                getCurve().getTo().getX(), getCurve().getTo().getY());
    }
}
