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
import org.cirqwizard.gerber.appertures.*;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.appertures.macro.*;

import java.awt.*;
import java.awt.geom.*;


public class Flash extends GerberPrimitive
{
    private Point point;

    public Flash(int x, int y, Aperture aperture, Polarity polarity)
    {
        super(polarity);
        point = new Point(x, y);
        this.aperture = aperture;
    }

    public Point getPoint()
    {
        return point;
    }

    public int getX()
    {
        return point.getX();
    }

    public int getY()
    {
        return point.getY();
    }

    @Override
    public void rotate(boolean clockwise)
    {
        if (clockwise)
            point = new Point(point.getY(), -point.getX());
        else
            point = new Point(-point.getY(), point.getX());
        aperture = aperture.rotate(clockwise);
    }

    @Override
    public void move(Point p)
    {
        point = point.add(p);
    }

    @Override
    public Point getMin()
    {
        return point.subtract(new Point(aperture.getWidth() / 2, aperture.getHeight() / 2));
    }

    @Override
    public Point getMax()
    {
        return point.add(new Point(aperture.getWidth() / 2, aperture.getHeight() / 2));
    }

    @Override
    public void render(Graphics2D g, double inflation)
    {
        if (getAperture() instanceof CircularAperture)
        {
            double d = Math.max(((CircularAperture)getAperture()).getDiameter() + inflation * 2, 0);
            double r = d / 2;
            g.fill(new Ellipse2D.Double(getX() - r, getY() - r, d, d));
        }
        else if (getAperture() instanceof RectangularAperture)
        {
            RectangularAperture aperture = (RectangularAperture)getAperture();
            double w = Math.max(aperture.getDimensions()[0] + inflation * 2, 0);
            double h = Math.max(aperture.getDimensions()[1] + inflation * 2, 0);
            g.fill(new Rectangle2D.Double(getX() - aperture.getDimensions()[0] / 2 - inflation,
                    getY() - aperture.getDimensions()[1] / 2 - inflation, w, h));
        }
        else if (getAperture() instanceof OctagonalAperture)
        {
            double edgeOffset = (Math.pow(2, 0.5) - 1) / 2 * (((OctagonalAperture)getAperture()).getDiameter() + inflation * 2);
            double centerOffset = 0.5 * (((OctagonalAperture)getAperture()).getDiameter() + inflation * 2);
            double flashX = getX();
            double flashY = getY();

            Path2D polygon = new GeneralPath();
            polygon.moveTo(centerOffset + flashX, edgeOffset + flashY);
            polygon.lineTo(edgeOffset + flashX, centerOffset + flashY);
            polygon.lineTo(-edgeOffset + flashX, centerOffset + flashY);
            polygon.lineTo(-centerOffset + flashX, edgeOffset + flashY);
            polygon.lineTo(-centerOffset + flashX, -edgeOffset + flashY);
            polygon.lineTo(-edgeOffset + flashX, -centerOffset + flashY);
            polygon.lineTo(edgeOffset + flashX, -centerOffset + flashY);
            polygon.lineTo(centerOffset + flashX, -edgeOffset + flashY);
            g.fill(polygon);
        }
        else if (getAperture() instanceof OvalAperture)
        {
            OvalAperture aperture = (OvalAperture)getAperture();
            double flashX = getX();
            double flashY = getY();
            double width = Math.max(aperture.getWidth() + inflation * 2, 0);
            double height = Math.max(aperture.getHeight() + inflation * 2, 0);
            double d = Math.min(width, height);
            double l = aperture.isHorizontal() ? width - height : height - width;
            double xOffset = aperture.isHorizontal() ? l / 2 : 0;
            double yOffset = aperture.isHorizontal() ? 0 : l / 2;
            double rectX = aperture.isHorizontal() ? flashX - l / 2 : flashX - width / 2;
            double rectY = aperture.isHorizontal() ? flashY - height / 2 : flashY - l / 2;
            double rectWidth =  aperture.isHorizontal() ? l : width;
            double rectHeight =  aperture.isHorizontal() ? height : l;

            g.fill(new Ellipse2D.Double(flashX - xOffset - d / 2, flashY + yOffset - d / 2, d, d));
            g.fill(new Ellipse2D.Double(flashX + xOffset - d / 2, flashY - yOffset - d / 2, d, d));
            g.fill(new Rectangle2D.Double(rectX, rectY, rectWidth, rectHeight));
        }
        else if (getAperture() instanceof ApertureMacro)
        {
            ApertureMacro macro = (ApertureMacro) getAperture();
            for (MacroPrimitive p : macro.getPrimitives())
            {
                if (p instanceof MacroCenterLine)
                {
                    MacroCenterLine centerLine = (MacroCenterLine) p;
                    org.cirqwizard.geom.Point from = centerLine.getFrom().add(getPoint());
                    org.cirqwizard.geom.Point to = centerLine.getTo().add(getPoint());
                    g.setStroke(new BasicStroke(centerLine.getHeight(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
                    g.draw(new Line2D.Float(from.getX(), from.getY(), to.getX(), to.getY()));
                }
                else if (p instanceof MacroVectorLine)
                {
                    MacroVectorLine vectorLine = (MacroVectorLine) p;
                    org.cirqwizard.geom.Point from = vectorLine.getTranslatedStart().add(getPoint());
                    org.cirqwizard.geom.Point to = vectorLine.getTranslatedEnd().add(getPoint());
                    g.setStroke(new BasicStroke(vectorLine.getWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
                    g.draw(new Line2D.Float(from.getX(), from.getY(), to.getX(), to.getY()));
                }
                else if (p instanceof MacroCircle)
                {
                    MacroCircle circle = (MacroCircle) p;
                    double d = circle.getDiameter();
                    double r = d / 2;
                    org.cirqwizard.geom.Point point = circle.getCenter().add(getPoint());
                    g.fill(new Ellipse2D.Double(point.getX() - r, point.getY() - r, d, d));
                }
                else if (p instanceof MacroOutline)
                {
                    MacroOutline outline = (MacroOutline) p;
                    double x = getX();
                    double y = getY();

                    Path2D polygon = new GeneralPath();
                    org.cirqwizard.geom.Point point = outline.getTranslatedPoints().get(0);
                    polygon.moveTo(point.getX() + x, point.getY() + y);
                    for (int i = 1; i < outline.getTranslatedPoints().size(); i++)
                    {
                        point = outline.getTranslatedPoints().get(i);
                        polygon.lineTo(point.getX()  + x, point.getY() + y);
                    }
                    g.fill(polygon);
                }
                else if (p instanceof MacroPolygon)
                {
                    MacroPolygon poly = (MacroPolygon) p;
                    double x = getX();
                    double y = getY();

                    Path2D polygon = new GeneralPath();
                    org.cirqwizard.geom.Point point = poly.getPoints().get(0);
                    polygon.moveTo(point.getX() + x, point.getY() + y);
                    for (int i = 1; i < poly.getPoints().size(); i++)
                    {
                        point = poly.getPoints().get(i);
                        polygon.lineTo(point.getX()  + x, point.getY() + y);
                    }
                    g.fill(polygon);
                }
            }
        }
    }

    @Override
    public void render(GraphicsContext g)
    {
        if (getAperture() instanceof CircularAperture)
        {
            double d = ((CircularAperture)getAperture()).getDiameter();
            double r = d / 2;
            g.fillOval(getX() - r, getY() - r, d, d);
        }
        else if (getAperture() instanceof RectangularAperture)
        {
            RectangularAperture aperture = (RectangularAperture)getAperture();
            g.fillRect(getX() - aperture.getDimensions()[0] / 2,
                    getY() - aperture.getDimensions()[1] / 2,
                    aperture.getDimensions()[0],
                    aperture.getDimensions()[1]);
        }
        else if (getAperture() instanceof OctagonalAperture)
        {
            double edgeOffset = (Math.pow(2, 0.5) - 1) / 2 * ((OctagonalAperture)getAperture()).getDiameter();
            double centerOffset =  0.5 * ((OctagonalAperture)getAperture()).getDiameter();
            double x = getX();
            double y = getY();

            g.beginPath();
            g.moveTo(centerOffset + x, edgeOffset + y);
            g.lineTo(edgeOffset + x, centerOffset + y);
            g.lineTo(-edgeOffset + x, centerOffset + y);
            g.lineTo(-centerOffset + x, edgeOffset + y);
            g.lineTo(-centerOffset + x, -edgeOffset + y);
            g.lineTo(-edgeOffset + x, -centerOffset + y);
            g.lineTo(edgeOffset + x, -centerOffset + y);
            g.lineTo(centerOffset + x, -edgeOffset + y);
            g.closePath();
            g.fill();
        }
        else if (getAperture() instanceof OvalAperture)
        {
            OvalAperture aperture = (OvalAperture)getAperture();
            double flashX = getX();
            double flashY = getY();
            double width = aperture.getWidth();
            double height = aperture.getHeight();
            double d = Math.min(width, height);
            double l = aperture.isHorizontal() ? width - height : height - width;
            double xOffset = aperture.isHorizontal() ? l / 2 : 0;
            double yOffset = aperture.isHorizontal() ? 0 : l / 2;
            g.fillOval(flashX + xOffset - d / 2, flashY + yOffset - d / 2, d, d);
            g.fillOval(flashX - xOffset - d / 2, flashY - yOffset - d / 2, d, d);

            double rectX = aperture.isHorizontal() ? flashX - l / 2 : flashX - width / 2;
            double rectY = aperture.isHorizontal() ? flashY - height / 2 : flashY - l / 2;
            double rectWidth =  aperture.isHorizontal() ? l : width;
            double rectHeight =  aperture.isHorizontal() ? height : l;
            g.fillRect(rectX, rectY, rectWidth, rectHeight);
        }
        else if (getAperture() instanceof ApertureMacro)
        {
            ApertureMacro macro = (ApertureMacro) getAperture();
            for (MacroPrimitive p : macro.getPrimitives())
            {
                if (p instanceof MacroCenterLine)
                {
                    MacroCenterLine centerLine = (MacroCenterLine) p;
                    Point from = centerLine.getFrom().add(getPoint());
                    Point to = centerLine.getTo().add(getPoint());
                    g.setLineCap(StrokeLineCap.BUTT);
                    g.setLineWidth(centerLine.getHeight());
                    g.strokeLine(from.getX(), from.getY(), to.getX(), to.getY());
                }
                else if (p instanceof MacroVectorLine)
                {
                    MacroVectorLine vectorLine = (MacroVectorLine) p;
                    Point from = vectorLine.getTranslatedStart().add(getPoint());
                    Point to = vectorLine.getTranslatedEnd().add(getPoint());
                    g.setLineCap(StrokeLineCap.BUTT);
                    g.setLineWidth(vectorLine.getWidth());
                    g.strokeLine(from.getX(), from.getY(), to.getX(), to.getY());
                }
                else if (p instanceof MacroCircle)
                {
                    MacroCircle circle = (MacroCircle) p;
                    double d = circle.getDiameter();
                    double r = d / 2;
                    Point point = circle.getCenter().add(getPoint());
                    g.fillOval(point.getX() - r, point.getY() - r, d, d);

                }
                else if (p instanceof MacroOutline)
                {
                    MacroOutline outline = (MacroOutline) p;
                    double x = getX();
                    double y = getY();

                    g.beginPath();
                    Point point = outline.getTranslatedPoints().get(0);
                    g.moveTo(point.getX() + x, point.getY() + y);
                    outline.getTranslatedPoints().forEach(tp -> g.lineTo(tp.getX() + x, tp.getY() + y));
                    g.closePath();
                    g.fill();
                }
                else if (p instanceof MacroPolygon)
                {
                    MacroPolygon polygon = (MacroPolygon) p;
                    double x = getX();
                    double y = getY();

                    g.beginPath();
                    Point point = polygon.getPoints().get(0);
                    g.moveTo(point.getX() + x, point.getY() + y);
                    polygon.getPoints().forEach(tp -> g.lineTo(tp.getX() + x, tp.getY() + y));
                    g.closePath();
                    g.fill();
                }
            }
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}
