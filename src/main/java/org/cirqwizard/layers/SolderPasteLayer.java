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

import org.cirqwizard.gerber.appertures.OvalAperture;
import org.cirqwizard.gerber.appertures.RectangularAperture;
import org.cirqwizard.geom.Line;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.gerber.LinearShape;
import org.cirqwizard.gerber.Region;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.math.MathUtil;
import org.cirqwizard.generation.toolpath.LinearToolpath;
import org.cirqwizard.generation.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.logging.Level;


public class SolderPasteLayer extends Layer
{
    private ArrayList<GerberPrimitive> elements = new ArrayList<GerberPrimitive>();
    private ArrayList<Toolpath> toolpaths = new ArrayList<Toolpath>();

    public void setElements(ArrayList<GerberPrimitive> elements)
    {
        this.elements = elements;
    }

    public ArrayList<GerberPrimitive> getElements()
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

    private void fillRectangle(ArrayList<Toolpath> toolpaths, Point from, Point to, int width, int needleDiameter)
    {
        // Shortening tool path for needle radius
        double angle = new Line(from, to).angleToX();
        from = from.add(new Point((int) (Math.cos(angle) * needleDiameter / 2), (int) (Math.sin(angle) * needleDiameter / 2)));
        to = to.subtract(new Point((int) (Math.cos(angle) * needleDiameter / 2), (int) (Math.sin(angle) * needleDiameter / 2)));

        angle = MathUtil.bindAngle(angle - Math.PI / 2);
        int passes = Math.max(1, Math.abs(width) / (needleDiameter + needleDiameter / 2));
        for (int i = 0; i < passes; i++)
        {
            // Offsetting
            int offset = width / (passes + 1) * (i + 1);
            Point offsetVector = new Point((int)(Math.cos(angle) * offset), (int)(Math.sin(angle) * offset));
            toolpaths.add(new LinearToolpath(needleDiameter, from.add(offsetVector), to.add(offsetVector)));
        }
    }

    public void generateToolpaths(int needleDiameter)
    {
        toolpaths = new ArrayList<>();
        for (GerberPrimitive element : elements)
        {
            if (element instanceof Flash)
            {
                Flash flash = (Flash)element;
                if (flash.getAperture() instanceof RectangularAperture)
                {
                    RectangularAperture aperture = (RectangularAperture)flash.getAperture();
                    int[] dimensions = aperture.getDimensions();
                    boolean vertical = dimensions[1] > dimensions[0];

                    int passes = Math.max(1, dimensions[vertical ? 0 : 1] / (needleDiameter * 2));
                    for (int i = 0; i < passes; i++)
                    {
                        LinearToolpath toolpath;
                        if (vertical)
                        {
                            int x = (int)(flash.getX() - dimensions[0] / 2 + (double)dimensions[0] / (passes + 1) * (i + 1));
                            int y = flash.getY() - dimensions[1] / 2 + needleDiameter;
                            toolpath = new LinearToolpath(needleDiameter, new Point(x, y), new Point(x, flash.getY() + (dimensions[1] / 2) - needleDiameter));
                        }
                        else
                        {
                            int x = flash.getX() - dimensions[0] / 2 + needleDiameter;
                            int y = (int)(flash.getY() - dimensions[1] / 2 + (double)dimensions[1] / (passes + 1) * (i + 1));
                            toolpath = new LinearToolpath(needleDiameter, new Point(x, y), new Point(flash.getX() + (dimensions[0] / 2) - needleDiameter, y));
                        }
                        toolpaths.add(toolpath);
                    }
                }
                else if (flash.getAperture() instanceof OvalAperture)
                {
                    OvalAperture aperture = (OvalAperture) flash.getAperture();
                    Point from, to;
                    int width;
                    if (aperture.isHorizontal())
                    {
                        from = new Point(flash.getX() - aperture.getWidth() / 2 + (aperture.getHeight() / 2 - needleDiameter / 2), flash.getY() + aperture.getHeight() / 2);
                        to = new Point(flash.getX() + aperture.getWidth() / 2 - (aperture.getHeight() / 2 - needleDiameter / 2), flash.getY() + aperture.getHeight() / 2);
                        width = aperture.getHeight();
                    }
                    else
                    {
                        from = new Point(flash.getX() - aperture.getWidth() / 2, flash.getY() - aperture.getHeight() / 2 + (aperture.getWidth() / 2 - needleDiameter / 2));
                        to = new Point(flash.getX() - aperture.getWidth() / 2, flash.getY() + aperture.getHeight() / 2 - (aperture.getWidth() / 2 - needleDiameter / 2));
                        width = aperture.getWidth();
                    }
                    fillRectangle(toolpaths, from, to, width, needleDiameter);
                }
                else
                    System.out.println("Circular apertures not supported at the moment: " + flash.getAperture());
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

                fillRectangle(toolpaths, longestSide.getFrom(), longestSide.getTo(), (int) largestWidth, needleDiameter);
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
                fillRectangle(toolpaths, l.getFrom().add(offset), l.getTo().add(offset), line.getAperture().getWidth(), needleDiameter);
            }
            else
            {
                LoggerFactory.getApplicationLogger().log(Level.WARNING, "Unexpected element on solder paste level: " + element);
            }
        }
    }

    private double calculatePerpendicular(Point from, Point to, Point p)
    {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();

        return (dx * (from.getY() - p.getY()) - dy * (from.getX() - p.getX())) / Math.sqrt(dx * dx + dy * dy);
    }

}
