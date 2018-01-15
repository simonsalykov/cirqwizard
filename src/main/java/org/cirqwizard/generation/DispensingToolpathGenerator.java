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

import org.cirqwizard.generation.toolpath.LinearToolpath;
import org.cirqwizard.generation.toolpath.Toolpath;
import org.cirqwizard.geom.Line;
import org.cirqwizard.geom.Point;
import org.cirqwizard.geom.Polygon;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.gerber.LinearShape;
import org.cirqwizard.gerber.Region;
import org.cirqwizard.gerber.appertures.CircularAperture;
import org.cirqwizard.gerber.appertures.OvalAperture;
import org.cirqwizard.gerber.appertures.RectangularAperture;
import org.cirqwizard.gerber.appertures.macro.ApertureMacro;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.math.MathUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class DispensingToolpathGenerator
{
    private List<GerberPrimitive> elements;

    public DispensingToolpathGenerator(List<GerberPrimitive> primitives)
    {
        this.elements = primitives;
    }

    public List<Toolpath> generate(int needleDiameter)
    {
        List<Toolpath> toolpaths = new ArrayList<>();
        for (GerberPrimitive element : elements)
        {
            if (element instanceof Flash)
            {
                Flash flash = (Flash)element;
                if (flash.getAperture() instanceof RectangularAperture)
                {
                    RectangularAperture aperture = (RectangularAperture)flash.getAperture();
                    int[] dimensions = aperture.getDimensions();
                    fillRectangle(toolpaths, flash.getPoint(), dimensions[0], dimensions[1], needleDiameter);
                }
                else if (flash.getAperture() instanceof OvalAperture)
                {
                    OvalAperture aperture = (OvalAperture) flash.getAperture();
                    Point from, to;
                    int width;
                    if (aperture.isHorizontal())
                    {
                        from = new Point(flash.getX() - aperture.getWidth() / 2 + (aperture.getHeight() / 2 - needleDiameter), flash.getY() + aperture.getHeight() / 2);
                        to = new Point(flash.getX() + aperture.getWidth() / 2 - (aperture.getHeight() / 2 - needleDiameter), flash.getY() + aperture.getHeight() / 2);
                        width = aperture.getHeight();
                    }
                    else
                    {
                        from = new Point(flash.getX() - aperture.getWidth() / 2, flash.getY() - aperture.getHeight() / 2 + (aperture.getWidth() / 2 - needleDiameter));
                        to = new Point(flash.getX() - aperture.getWidth() / 2, flash.getY() + aperture.getHeight() / 2 - (aperture.getWidth() / 2 - needleDiameter));
                        width = aperture.getWidth();
                    }
                    fillRectangleByAngle(toolpaths, from, to, width, needleDiameter);
                }
                else if (flash.getAperture() instanceof CircularAperture)
                {
                    CircularAperture aperture = (CircularAperture) flash.getAperture();
                    int width = aperture.getRectWidth();
                    fillRectangle(toolpaths, flash.getPoint(), width, width, needleDiameter);
                }
                else if(flash.getAperture() instanceof ApertureMacro)
                {
                    ApertureMacro aperture = (ApertureMacro) flash.getAperture();
                    Polygon polygon =  aperture.getMinInsideRectangular();

                    if (polygon != null)
                    {
                        polygon = polygon.transform(flash.getPoint());
                        Line longestEdge = polygon.getLongestEdge();

                        // need to move the needle by the angle, calculate offsets
                        double angle = longestEdge.angleToX();
                        angle = MathUtil.bindAngle(angle - Math.PI / 2);
                        double cos = Math.cos(angle);
                        double sin = Math.sin(angle);

                        Point needleOffset = new Point((int) (cos * needleDiameter), (int) (sin * needleDiameter));

                        // going through the polygon and measure it's size
                        Point estimateOffset = new Point((int) (cos * 50), (int) (sin * 50));
                        Line estLine = longestEdge.clone();
                        int width = 0;

                        while (polygon.lineBelongsToPolygon(estLine))
                        {
                            width = (int) longestEdge.getFrom().distanceTo(estLine.getFrom());

                            estLine.setFrom(estLine.getFrom().add(estimateOffset));
                            estLine.setTo(estLine.getTo().add(estimateOffset));
                        }

                        int passes = Math.max(1, Math.abs(width) / (2 * needleDiameter));
                        for (int i = 0; i < passes; i++)
                        {
                            // Offsetting
                            int offset = width / (passes + 1) * (i + 1);
                            Point offsetVector2 = new Point((int)(cos * offset), (int)(sin * offset));
                            Line offsetLine = new Line(longestEdge.getFrom().add(offsetVector2), longestEdge.getTo().add(offsetVector2));

                            if (passes > 1)
                            {
                                offsetLine = adjustLineSizeForOutline(polygon, offsetLine, needleDiameter, needleOffset);
                            }
                            else
                            {
                                // if the pass only one we need to fill it anyway
                                if (offsetLine.length() > 2 * needleDiameter)
                                    offsetLine = offsetLine.offsetFrom(needleDiameter).offsetTo(needleDiameter);
                            }

                            if (offsetLine != null)
                            {
                                LinearToolpath toolpath = new LinearToolpath(needleDiameter, offsetLine.getFrom(), offsetLine.getTo());
                                toolpaths.add(toolpath);
                            }
                        }
                    }
                    else
                    {
                        System.out.println("Rect area wasn't build for macro aperture, ignoring it.");
                    }
                }
                else
                    System.out.println("The given aperture is not supported at the moment: " + flash.getAperture());
            }
            else if (element instanceof Region)
            {
                Region region = (Region) element;
                LinearShape longestSide = null;
                for (GerberPrimitive p : region.getSegments())
                {
                    if (p instanceof LinearShape)
                    {
                        if (longestSide == null ||
                                longestSide.getFrom().distanceTo(longestSide.getTo()) < ((LinearShape) p).getFrom().distanceTo(((LinearShape) p).getTo()))
                            longestSide = (LinearShape) p;
                    }
                }

                if (longestSide == null)
                    continue;

                double largestWidth = 0;
                for (GerberPrimitive p : region.getSegments())
                {
                    if (p instanceof LinearShape)
                    {
                        double d = calculatePerpendicular(longestSide.getFrom(), longestSide.getTo(), ((LinearShape) p).getFrom());
                        if (Math.abs(d) > Math.abs(largestWidth))
                            largestWidth = d;
                        d = calculatePerpendicular(longestSide.getFrom(), longestSide.getTo(), ((LinearShape) p).getTo());
                        if (Math.abs(d) > Math.abs(largestWidth))
                            largestWidth = d;
                    }
                }

                fillRectangleByAngle(toolpaths, longestSide.getFrom(), longestSide.getTo(), (int) largestWidth, needleDiameter);
            }
            else if (element instanceof LinearShape)
            {
                LinearShape line = (LinearShape) element;
                Line l = new Line(line.getFrom(), line.getTo());
                double angle = l.angleToX();
                if (line.getAperture() instanceof RectangularAperture)
                {
                    Point appertureOffset = new Point((int)(line.getAperture().getWidth() / 2 * Math.cos(angle)),
                            (int)(line.getAperture().getWidth() / 2 * Math.sin(angle)));
                    l.setTo(l.getTo().add(appertureOffset));
                    appertureOffset = new Point(-appertureOffset.getX(), -appertureOffset.getY());
                    l.setFrom(l.getFrom().add(appertureOffset));
                }
                angle += Math.PI / 2;
                Point offset = new Point((int)(line.getAperture().getWidth() / 2 * Math.cos(angle)),
                        (int)(line.getAperture().getWidth() / 2 * Math.sin(angle)));
                fillRectangleByAngle(toolpaths, l.getFrom().add(offset), l.getTo().add(offset), line.getAperture().getWidth(), needleDiameter);
            }
            else
            {
                LoggerFactory.getApplicationLogger().log(Level.WARNING, "Unexpected element on solder paste level: " + element);
            }
        }
        return toolpaths;
    }

    private Line adjustLineSizeForOutline(Polygon polygon, Line line, int needleDiameter, Point needleOffset)
    {
        Point firstPoint = line.getFrom();
        Point secondPoint = line.getTo();
        int precision = 100; // reduce by 1%

        int stepX = (secondPoint.getX() - firstPoint.getX()) / precision;
        int stepY = (secondPoint.getY() - firstPoint.getY()) / precision;
        Point stepPoint = new Point(stepX, stepY);

        int iterations = 0;
        while((!polygon.pointBelongsToPolygon(firstPoint) ||
              !polygon.pointBelongsToPolygon(firstPoint.add(needleOffset)) ||
              !polygon.pointBelongsToPolygon(firstPoint.subtract(needleOffset))) &&
              iterations < precision)
        {
            firstPoint = firstPoint.add(stepPoint);
            ++iterations;
        }

        if (iterations >= precision)
            return null;

        iterations = 0;

        while((!polygon.pointBelongsToPolygon(secondPoint) ||
              !polygon.pointBelongsToPolygon(secondPoint.add(needleOffset)) ||
              !polygon.pointBelongsToPolygon(secondPoint.subtract(needleOffset))) &&
              iterations < precision)
        {
            secondPoint = secondPoint.subtract(stepPoint);
            ++iterations;
        }

        if (iterations >= precision)
            return null;

        // here substract needle
        Line result = new Line(firstPoint, secondPoint);
        if (result.length() < needleDiameter * 2)
            return result;

        return result.offsetFrom(needleDiameter).offsetTo(needleDiameter);
    }

    private void fillRectangle(List<Toolpath> toolpaths, Point center, int width, int height, int needleDiameter)
    {
        boolean vertical = width < height;
        int passes = Math.max(1, (vertical ? width : height) / (needleDiameter * 2));
        for (int i = 0; i < passes; i++)
        {
            LinearToolpath toolpath;
            if (vertical)
            {
                int x = (int)(center.getX() - width / 2 + (double)width / (passes + 1) * (i + 1));
                int y = center.getY() - height / 2 + needleDiameter;
                toolpath = new LinearToolpath(needleDiameter, new Point(x, y), new Point(x, center.getY() + (height / 2) - needleDiameter));
            }
            else
            {
                int x = center.getX() - width / 2 + needleDiameter;
                int y = (int)(center.getY() - height / 2 + (double)height / (passes + 1) * (i + 1));
                toolpath = new LinearToolpath(needleDiameter, new Point(x, y), new Point(center.getX() + (width / 2) - needleDiameter, y));
            }
            toolpaths.add(toolpath);
        }
    }

    private void fillRectangleByAngle(List<Toolpath> toolpaths, Point from, Point to, int width, int needleDiameter)
    {
        // Shortening tool path for needle radius
        double angle = new Line(from, to).angleToX();
        from = from.add(new Point((int) (Math.cos(angle) * needleDiameter), (int) (Math.sin(angle) * needleDiameter)));
        to = to.subtract(new Point((int) (Math.cos(angle) * needleDiameter), (int) (Math.sin(angle) * needleDiameter)));

        angle = MathUtil.bindAngle(angle - Math.PI / 2);
        int passes = Math.max(1, Math.abs(width) / (needleDiameter + needleDiameter));
        for (int i = 0; i < passes; i++)
        {
            // Offsetting
            int offset = width / (passes + 1) * (i + 1);
            Point offsetVector = new Point((int)(Math.cos(angle) * offset), (int)(Math.sin(angle) * offset));
            toolpaths.add(new LinearToolpath(needleDiameter, from.add(offsetVector), to.add(offsetVector)));
        }
    }

    private double calculatePerpendicular(Point from, Point to, Point p)
    {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();

        return (dx * (from.getY() - p.getY()) - dy * (from.getX() - p.getX())) / Math.sqrt(dx * dx + dy * dy);
    }
}
