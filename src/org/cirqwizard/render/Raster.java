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

package org.cirqwizard.render;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.cirqwizard.appertures.CircularAperture;
import org.cirqwizard.appertures.OctagonalAperture;
import org.cirqwizard.appertures.PolygonalAperture;
import org.cirqwizard.appertures.RectangularAperture;
import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Point;
import org.cirqwizard.geom.PolygonUtils;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.gerber.LinearShape;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.toolpath.CircularToolpath;
import org.cirqwizard.toolpath.CuttingToolpath;
import org.cirqwizard.toolpath.LinearToolpath;
import org.cirqwizard.toolpath.Toolpath;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Raster
{
    private int width;
    private int height;
    private int inflation;
    private int toolDiameter;

    private ArrayList<GerberPrimitive> primitives = new ArrayList<GerberPrimitive>();
    private ArrayList<Integer> radii = new ArrayList<>();
    private ArrayList<Flash> circularFlashes = new ArrayList<>();

    private DoubleProperty generationProgress = new SimpleDoubleProperty();


    public Raster(int width, int height, int inflation, int toolDiameter)
    {
        this.width = width;
        this.height = height;
        this.inflation = inflation;
        this.toolDiameter = toolDiameter;
    }

    public void addPrimitive(GerberPrimitive primitive)
    {
        primitives.add(primitive);
        if (primitive.getAperture() instanceof CircularAperture)
        {
            CircularAperture aperture = (CircularAperture) primitive.getAperture();
            if (primitive instanceof Flash)
                circularFlashes.add((Flash) primitive);
            else if (!radii.contains(aperture.getDiameter() / 2))
                radii.add(aperture.getDiameter() / 2);
        }
    }

    public java.util.List<Toolpath> trace()
    {
        final int windowSize = 5000;
        final int windowsOverlap = 5;

        final Vector<Toolpath> segments = new Vector<>();

        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int x = 0; x < width; x += windowSize)
        {
            for (int y = 0; y < height; y += windowSize)
            {
                final int _x = x > windowsOverlap ? x - windowsOverlap : x;
                final int _y = y > windowsOverlap ? y - windowsOverlap : y;
                pool.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Platform.runLater(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    generationProgress.set(((double)_y * windowSize + (double)_x * height) / ((double)width * height));
                                }
                            });

                            Point offset = new Point(_x, _y);
                            ArrayList<Flash> translatedFlashes = new ArrayList<>();
                            for (Flash flash : circularFlashes)
                            {
                                Point p  = translateToWindowCoordinates(flash.getPoint(), offset);
                                translatedFlashes.add(new Flash(p.getX(), p.getY(), new CircularAperture(((CircularAperture)flash.getAperture()).getDiameter() / 2)));
                            }

                            int windowWidth = Math.min(windowSize + 2 * windowsOverlap, width - _x);
                            int windowHeight = Math.min(windowSize + 2 * windowsOverlap, height - _y);
                            RasterWindow window = renderWindow(new PointI(_x, _y), windowWidth, windowHeight);
                            SimpleEdgeDetector detector = new SimpleEdgeDetector(window.getBufferedImage());
                            window = null; // Helping GC to reclaim memory consumed by rendered image
                            detector.process();
                            if (detector.getOutput() != null)
                            {
                                java.util.List<Toolpath> toolpaths = new Tracer(Raster.this, detector.getOutput(), windowWidth, windowHeight, toolDiameter, translatedFlashes).process();
                                detector = null;  // Helping GC to reclaim memory consumed by processed image
                                segments.addAll(translateToolpaths(toolpaths, offset));
                            }
                        }
                        catch (Throwable e)
                        {
                            LoggerFactory.logException("Error while generating tool paths", e);
                        }
                    }
                });
            }
        }

        try
        {
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.DAYS);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return mergeToolpaths(segments);
    }

    private java.util.List<Toolpath> translateToolpaths(java.util.List<Toolpath> toolpaths, Point offset)
    {
        ArrayList<Toolpath> result = new ArrayList<>();
        for (Toolpath toolpath : toolpaths)
        {
            if (((CuttingToolpath)toolpath).getCurve().getFrom().equals(((CuttingToolpath)toolpath).getCurve().getTo()))
                continue;
            if (toolpath instanceof LinearToolpath)
            {
                LinearToolpath lt = (LinearToolpath) toolpath;
                Point start = translateWindowCoordiantes(lt.getCurve().getFrom(), offset);
                Point end = translateWindowCoordiantes(lt.getCurve().getTo(), offset);
                result.add(new LinearToolpath(((LinearToolpath) toolpath).getToolDiameter(), start, end));
            }
            else if (toolpath instanceof CircularToolpath)
            {
                CircularToolpath ct = (CircularToolpath) toolpath;
                Arc arc = (Arc) ct.getCurve();
                Point start = translateWindowCoordiantes(ct.getCurve().getFrom(), offset);
                Point end = translateWindowCoordiantes(ct.getCurve().getTo(), offset);
                Point center = translateWindowCoordiantes(arc.getCenter(), offset);
                int radius = arc.getRadius();
                result.add(new CircularToolpath(ct.getToolDiameter(), start, end, center, radius, arc.isClockwise()));
            }
        }

        return result;
    }

    private Point translateWindowCoordiantes(Point point, Point windowOffset)
    {
        return new Point(point.getX(), point.getY()).add(windowOffset);
    }

    private Point translateToWindowCoordinates(Point point, Point windowOffset)
    {
        return new Point(point.getX(), point.getY()).subtract(windowOffset);
    }

    private java.util.List<Toolpath> mergeToolpaths(java.util.List<Toolpath> toolpaths)
    {
        System.out.println("merge: " + toolpaths.size());

        long t = System.currentTimeMillis();
        toolpaths = new ToolpathMerger(toolpaths).merge();
        t = System.currentTimeMillis() - t;

        System.out.println("merged: " + toolpaths.size() + "(" + t + ")");

        return toolpaths;
    }

    public DoubleProperty generationProgressProperty()
    {
        return generationProgress;
    }

    public java.util.List<Integer> getRadii()
    {
        return radii;
    }

    private RasterWindow renderWindow(PointI lowerLeftCorner, int width, int height)
    {
        int[] cmap = {0x00000000, 0x00ffffff, 0x0000ff00, 0xffffffff};
        BufferedImage window = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

        Graphics2D g = window.createGraphics();
        g.setBackground(Color.BLACK);
        g.clearRect(0, 0, width, height);
        for (GerberPrimitive primitive : primitives)
            renderPrimitive(window.createGraphics(), primitive, inflation, lowerLeftCorner);

        return new RasterWindow(window, lowerLeftCorner);
    }

    private void renderPrimitive(Graphics2D g, GerberPrimitive primitive, double inflation, PointI lowerLeftCorner)
    {
        if (!primitive.getAperture().isVisible())
            return;

        g.transform(AffineTransform.getTranslateInstance(-lowerLeftCorner.x, -lowerLeftCorner.y));
        g.setColor(Color.WHITE);
        if (primitive instanceof LinearShape)
        {
            LinearShape linearShape = (LinearShape) primitive;
            int cap = linearShape.getAperture() instanceof CircularAperture ? BasicStroke.CAP_ROUND : BasicStroke.CAP_SQUARE;
            g.setStroke(new BasicStroke((float) ((linearShape.getAperture().getWidth(0) + inflation * 2)), cap, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(linearShape.getFrom().getX(), linearShape.getFrom().getY(),
                    linearShape.getTo().getX(), linearShape.getTo().getY()));
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
            else if (flash.getAperture() instanceof PolygonalAperture)
            {
                PolygonalAperture aperture = (PolygonalAperture)flash.getAperture();
                ArrayList<org.cirqwizard.geom.Point> points = aperture.getPoints();
                double flashX = flash.getX();
                double flashY = flash.getY();
                Path2D polygon = new GeneralPath();

                points = PolygonUtils.expandPolygon(new ArrayList<>(points.subList(0, points.size() - 1)), inflation);
                polygon.moveTo(points.get(0).getX() + flashX, points.get(0).getY() + flashY);
                for (int i = 1; i < points.size(); i++)
                    polygon.lineTo(points.get(i).getX() + flashX, points.get(i).getY() + flashY);

                g.fill(polygon);
            }
        }
    }

}
