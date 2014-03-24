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

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.cirqwizard.appertures.CircularAperture;
import org.cirqwizard.appertures.macro.ApertureMacro;
import org.cirqwizard.appertures.macro.MacroCircle;
import org.cirqwizard.appertures.macro.MacroPrimitive;
import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.toolpath.CircularToolpath;
import org.cirqwizard.toolpath.CuttingToolpath;
import org.cirqwizard.toolpath.LinearToolpath;
import org.cirqwizard.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ToolpathGenerator
{
    private final static int WINDOW_SIZE = 5000;
    private final static int WINDOWS_OVERLAP = 5;
    private final static int OOM_RETRY_DELAY = 1000;

    private int width;
    private int height;
    private int inflation;
    private int toolDiameter;
    private int threadCount;
    private List<GerberPrimitive> primitives;

    private ArrayList<Flash> circularFlashes = new ArrayList<>();
    private ArrayList<Integer> radii = new ArrayList<>();

    private DoubleProperty progressProperty = new SimpleDoubleProperty();

    public ToolpathGenerator(int width, int height, int inflation, int toolDiameter, List<GerberPrimitive> primitives, int threadCount)
    {
        this.width = width;
        this.height = height;
        this.inflation = inflation;
        this.toolDiameter = toolDiameter;
        this.primitives = primitives;
        this.threadCount = threadCount;

        for (GerberPrimitive primitive : primitives)
        {
            if (primitive.getAperture() instanceof CircularAperture)
            {
                CircularAperture aperture = (CircularAperture) primitive.getAperture();
                if (primitive instanceof Flash)
                    circularFlashes.add((Flash) primitive);
                else if (!radii.contains(aperture.getDiameter() / 2))
                    radii.add(aperture.getDiameter() / 2);
            }
            else if (primitive.getAperture() instanceof ApertureMacro)
            {
                for (MacroPrimitive p : ((ApertureMacro) primitive.getAperture()).getPrimitives())
                {
                    if (p instanceof MacroCircle)
                        if (!radii.contains(((MacroCircle) p).getDiameter() / 2))
                            radii.add(((MacroCircle) p).getDiameter() / 2);
                }
            }

        }
    }

    public DoubleProperty progressProperty()
    {
        return progressProperty;
    }

    public List<Toolpath> generate()
    {
        final Vector<Toolpath> segments = new Vector<>();

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        for (int x = 0; x < width; x += WINDOW_SIZE)
            for (int y = 0; y < height; y += WINDOW_SIZE)
                pool.submit(new WindowGeneratorThread(x > WINDOWS_OVERLAP ? x - WINDOWS_OVERLAP : x, y > WINDOWS_OVERLAP ? y - WINDOWS_OVERLAP : y, segments));

        try
        {
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.DAYS);
        }
        catch (InterruptedException e)
        {
        }

        return segments;
    }

    private class WindowGeneratorThread implements Runnable
    {
        private int x;
        private int y;
        private Vector<Toolpath> segments;

        private WindowGeneratorThread(int x, int y, Vector<Toolpath> segments)
        {
            this.x = x;
            this.y = y;
            this.segments = segments;
        }

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
                        progressProperty.set(((double) y * WINDOW_SIZE + (double) x * height) / ((double) width * height));
                    }
                });

                Point offset = new Point(x, y);
                ArrayList<Flash> translatedFlashes = new ArrayList<>();
                for (Flash flash : circularFlashes)
                {
                    Point p  = translateToWindowCoordinates(flash.getPoint(), offset);
                    translatedFlashes.add(new Flash(p.getX(), p.getY(), new CircularAperture(((CircularAperture)flash.getAperture()).getDiameter() / 2)));
                }

                int windowWidth = Math.min(WINDOW_SIZE + 2 * WINDOWS_OVERLAP, width - x);
                int windowHeight = Math.min(WINDOW_SIZE + 2 * WINDOWS_OVERLAP, height - y);
                RasterWindow window = new RasterWindow(new Point(x, y), windowWidth, windowHeight);
                window.render(primitives, inflation);
                SimpleEdgeDetector detector = new SimpleEdgeDetector(window.getBufferedImage());
                window = null; // Helping GC to reclaim memory consumed by rendered image
                detector.process();
                if (detector.getOutput() != null)
                {
                    java.util.List<Toolpath> toolpaths =
                            new Tracer(detector.getOutput(), windowWidth, windowHeight, toolDiameter, radii, translatedFlashes).process();
                    detector = null;  // Helping GC to reclaim memory consumed by processed image
                    segments.addAll(translateToolpaths(toolpaths, offset));
                }
            }
            catch (OutOfMemoryError e)
            {
                LoggerFactory.getApplicationLogger().log(Level.WARNING, "Out of memory caught while generating tool paths. Retrying", e);
                try
                {
                    Thread.sleep((int)((1.0 + Math.random()) * OOM_RETRY_DELAY));
                }
                catch (InterruptedException e1)  {}

                run();
            }
            catch (Throwable e)
            {
                LoggerFactory.logException("Error while generating tool paths", e);
            }
        }
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


}
