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

import org.cirqwizard.appertures.CircularAperture;
import org.cirqwizard.appertures.OctagonalAperture;
import org.cirqwizard.appertures.OvalAperture;
import org.cirqwizard.appertures.RectangularAperture;
import org.cirqwizard.appertures.macro.*;
import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.*;
import org.cirqwizard.math.RealNumber;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class RasterWindow
{
    private BufferedImage window;
    private Point windowLowerLeftCorner;

    public RasterWindow(Point windowLowerLeftCorner, int width, int height)
    {
        this.windowLowerLeftCorner = windowLowerLeftCorner;
        this.window = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
    }

    public void render(java.util.List<GerberPrimitive> primitives, int inflation)
    {
        Graphics2D g = window.createGraphics();
        g.setBackground(Color.BLACK);
        g.clearRect(0, 0, window.getWidth(), window.getHeight());
        g = window.createGraphics();
        g.transform(AffineTransform.getTranslateInstance(-windowLowerLeftCorner.getX(), -windowLowerLeftCorner.getY()));

        for (GerberPrimitive primitive : primitives)
            renderPrimitive(g, primitive, inflation);
    }

    private void renderPrimitive(Graphics2D g, GerberPrimitive primitive, double inflation)
    {
        if (!(primitive instanceof Region) && !primitive.getAperture().isVisible())
            return;

        g.setColor(Color.WHITE);
        if (primitive instanceof LinearShape)
        {
            LinearShape linearShape = (LinearShape) primitive;
            int cap = linearShape.getAperture() instanceof CircularAperture ? BasicStroke.CAP_ROUND : BasicStroke.CAP_SQUARE;
            g.setStroke(new BasicStroke((float) ((linearShape.getAperture().getWidth(0) + inflation * 2)), cap, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(linearShape.getFrom().getX(), linearShape.getFrom().getY(),
                    linearShape.getTo().getX(), linearShape.getTo().getY()));
        }
        else if (primitive instanceof CircularShape)
        {
            CircularShape circularShape = (CircularShape) primitive;
            int cap = circularShape.getAperture() instanceof CircularAperture ? BasicStroke.CAP_ROUND : BasicStroke.CAP_SQUARE;
            g.setStroke(new BasicStroke((float) ((circularShape.getAperture().getWidth(0) + inflation * 2)), cap, BasicStroke.JOIN_ROUND));
            g.draw(new Arc2D.Double(circularShape.getArc().getCenter().getX() - circularShape.getArc().getRadius(),
                    circularShape.getArc().getCenter().getY() - circularShape.getArc().getRadius(),
                    circularShape.getArc().getRadius() * 2, circularShape.getArc().getRadius() * 2,
                    -Math.toDegrees(circularShape.getArc().getStart()),
                    Math.toDegrees(circularShape.getArc().getAngle()) * (circularShape.getArc().isClockwise() ? 1 : -1), Arc2D.OPEN));
        }
        else if (primitive instanceof Region)
        {
            Region region = (Region) primitive;

            Path2D polygon = new GeneralPath();

            g.setStroke(new BasicStroke((float) inflation * 2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
            Point p = ((InterpolatingShape) region.getSegments().get(0)).getFrom();
            polygon.moveTo(p.getX(), p.getY());
            for (GerberPrimitive segment : region.getSegments())
            {
                if (segment instanceof LinearShape)
                    polygon.lineTo(((LinearShape)segment).getTo().getX(), ((LinearShape)segment).getTo().getY());
                else if (segment instanceof CircularShape)
                {
                    Arc arc = ((CircularShape) segment).getArc();
                    polygon.append(new Arc2D.Double(arc.getCenter().getX() - arc.getRadius(),
                            arc.getCenter().getY() - arc.getRadius(),
                            arc.getRadius() * 2, arc.getRadius() * 2,
                            -Math.toDegrees(arc.getStart()),
                            Math.toDegrees(arc.getAngle()) * (arc.isClockwise() ? 1 : -1), Arc2D.OPEN),
                            true);
                }
            }
            g.fill(polygon);
        }
        else if (primitive instanceof Flash)
        {
            Flash flash = (Flash) primitive;
            if (flash.getAperture() instanceof CircularAperture)
            {
                double d = ((CircularAperture)flash.getAperture()).getDiameter() + inflation * 2;
                double r = d / 2;
                g.fill(new Ellipse2D.Double(flash.getX() - r, flash.getY() - r, d, d));
            }
            else if (flash.getAperture() instanceof RectangularAperture)
            {
                RectangularAperture aperture = (RectangularAperture)flash.getAperture();
                g.fill(new Rectangle2D.Double(flash.getX() - aperture.getDimensions()[0] / 2 - inflation,
                        flash.getY() - aperture.getDimensions()[1] / 2 - inflation,
                        aperture.getDimensions()[0] + inflation * 2,
                        aperture.getDimensions()[1] + inflation * 2));
            }
            else if (flash.getAperture() instanceof OctagonalAperture)
            {
                double edgeOffset = (Math.pow(2, 0.5) - 1) / 2 * (((OctagonalAperture)flash.getAperture()).getDiameter() + inflation * 2);
                double centerOffset = 0.5 * (((OctagonalAperture)flash.getAperture()).getDiameter() + inflation * 2);
                double flashX = flash.getX();
                double flashY = flash.getY();

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
            else if (flash.getAperture() instanceof OvalAperture)
            {
                OvalAperture aperture = (OvalAperture)flash.getAperture();
                double flashX = flash.getX();
                double flashY = flash.getY();
                double width = aperture.getWidth() + inflation * 2;
                double height = aperture.getHeight() + inflation * 2;
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
            else if (flash.getAperture() instanceof ApertureMacro)
            {
                ApertureMacro macro = (ApertureMacro) flash.getAperture();
                for (MacroPrimitive p : macro.getPrimitives())
                {
                    if (p instanceof MacroCenterLine)
                    {
                        MacroCenterLine centerLine = (MacroCenterLine) p;
                        org.cirqwizard.geom.Point from = centerLine.getFrom().add(flash.getPoint());
                        org.cirqwizard.geom.Point to = centerLine.getTo().add(flash.getPoint());
                        g.setStroke(new BasicStroke(centerLine.getHeight(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
                        g.draw(new Line2D.Float(from.getX(), from.getY(), to.getX(), to.getY()));
                    }
                    else if (p instanceof MacroVectorLine)
                    {
                        MacroVectorLine vectorLine = (MacroVectorLine) p;
                        org.cirqwizard.geom.Point from = vectorLine.getTranslatedStart().add(flash.getPoint());
                        org.cirqwizard.geom.Point to = vectorLine.getTranslatedEnd().add(flash.getPoint());
                        g.setStroke(new BasicStroke(vectorLine.getWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
                        g.draw(new Line2D.Float(from.getX(), from.getY(), to.getX(), to.getY()));
                    }
                    else if (p instanceof MacroCircle)
                    {
                        MacroCircle circle = (MacroCircle) p;
                        double d = circle.getDiameter();
                        double r = d / 2;
                        org.cirqwizard.geom.Point point = circle.getCenter().add(flash.getPoint());
                        g.fill(new Ellipse2D.Double(point.getX() - r, point.getY() - r, d, d));
                    }
                    else if (p instanceof MacroOutline)
                    {
                        MacroOutline outline = (MacroOutline) p;
                        double x = flash.getX();
                        double y = flash.getY();

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
                }
            }
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RasterWindow that = (RasterWindow) o;

        return windowLowerLeftCorner.equals(that.windowLowerLeftCorner) && window.getWidth() == that.window.getWidth() && window.getHeight() == that.window.getHeight();
    }

    @Override
    public int hashCode()
    {
        return windowLowerLeftCorner != null ? windowLowerLeftCorner.hashCode() : 0;
    }

    public BufferedImage getBufferedImage()
    {
        return window;
    }

    public void save(String file)
    {
        System.out.println("windowLeftCorner: " + windowLowerLeftCorner);
        try
        {
            ImageIO.write(window, "png", new File(file));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
