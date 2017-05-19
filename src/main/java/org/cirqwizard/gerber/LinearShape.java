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

package org.cirqwizard.gerber;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import org.cirqwizard.gerber.appertures.Aperture;
import org.cirqwizard.geom.Line;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.appertures.CircularAperture;

import java.awt.*;
import java.awt.geom.Line2D;


public class LinearShape extends InterpolatingShape
{
    private Line line;

    public LinearShape(int fromX, int fromY, int toX, int toY, Aperture aperture, Polarity polarity)
    {
        super(polarity);
        Point from = new Point(fromX, fromY);
        Point to = new Point(toX, toY);
        line = new Line(from, to);
        this.aperture = aperture;
    }

    public Point getFrom()
    {
        return line.getFrom();
    }

    public Point getTo()
    {
        return line.getTo();
    }

    @Override
    public void rotate(boolean clockwise)
    {
        line.setFrom(line.getFrom().rotateRelativeToOrigin(clockwise));
        line.setTo(line.getTo().rotateRelativeToOrigin(clockwise));

        if (aperture != null)
            aperture = aperture.rotate(clockwise);
    }

    @Override
    public void move(Point point)
    {
        line.setFrom(line.getFrom().add(point));
        line.setTo(line.getTo().add(point));
    }

    @Override
    public Point getMin()
    {
        Point p = new Point(Math.min(line.getFrom().getX(), line.getTo().getX()),
                Math.min(line.getFrom().getY(), line.getTo().getY()));
        if (aperture != null)
            p = p.subtract(new Point(aperture.getWidth() / 2, aperture.getHeight() / 2));
        return p;
    }

    @Override
    public Point getMax()
    {
        Point p = new Point(Math.max(line.getFrom().getX(), line.getTo().getX()),
                Math.max(line.getFrom().getY(), line.getTo().getY()));
        if (aperture != null)
            p = p.add(new Point(aperture.getWidth() / 2, aperture.getHeight() / 2));
        return p;
    }

    @Override
    public void render(Graphics2D g, double inflation)
    {
        int cap = getAperture() instanceof CircularAperture ? BasicStroke.CAP_ROUND : BasicStroke.CAP_SQUARE;
        double width = Math.max(getAperture().getWidth() + inflation * 2, 0);
        g.setStroke(new BasicStroke((float) width, cap, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(getFrom().getX(), getFrom().getY(),
                getTo().getX(), getTo().getY()));

    }

    @Override
    public void render(GraphicsContext g)
    {
        g.setLineCap(getAperture() instanceof CircularAperture ? StrokeLineCap.ROUND : StrokeLineCap.SQUARE);
        g.setLineWidth(getAperture().getWidth());
        g.strokeLine(getFrom().getX(), getFrom().getY(), getTo().getX(), getTo().getY());

    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        LinearShape clone = (LinearShape) super.clone();
        clone.line = new Line(new Point(line.getFrom().getX(), line.getFrom().getY()),
                new Point(line.getTo().getX(), line.getTo().getY()));
        return clone;
    }
}
