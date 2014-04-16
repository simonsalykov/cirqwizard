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
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.toolpath.CuttingToolpath;
import org.cirqwizard.toolpath.LinearToolpath;
import org.cirqwizard.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AdditionalToolpathGenerator extends AbstractToolpathGenerator
{
    private static final int MIN_LENGTH = 250;

    private int width;
    private int height;
    private int passes;
    private int overlap;
    private int toolDiameter;
    private int threadCount;

    public AdditionalToolpathGenerator(int width, int height, int passes, int overlap, int toolDiameter, int threadCount, List<GerberPrimitive> primitives)
    {
        this.width = width;
        this.height = height;
        this.passes = passes;
        this.overlap = overlap;
        this.toolDiameter = toolDiameter;
        this.threadCount = threadCount;
        this.primitives = primitives;
        initRadii();
    }

    public List<Toolpath> generate()
    {
        long tt = System.currentTimeMillis();
        final Vector<Toolpath> segments = new Vector<>();

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        progressProperty.setValue(0);
        final double progressIncrement = 1.0 / primitives.size();

        for (final GerberPrimitive primitive : primitives)
        {
            pool.submit(new Callable<Object>()
            {
                @Override
                public Object call() throws Exception
                {
                    try
                    {
                        Platform.runLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                progressProperty.setValue(progressProperty.getValue() + progressIncrement);
                            }
                        });
                        if (!(primitive instanceof Flash))
                            return null;

                        Flash flash = (Flash) primitive;
                        ArrayList<GerberPrimitive> primitivesCopy = new ArrayList<>(primitives);
                        primitivesCopy.remove(flash);
                        int x = flash.getX() - flash.getAperture().getCircumRadius() * 2;
                        int y = flash.getY() - flash.getAperture().getCircumRadius() * 2;
                        x = Math.max(0, x);
                        y = Math.max(0, y);
                        Point windowOffset = new Point(x, y);
                        int windowWidth = Math.min(flash.getAperture().getCircumRadius() * 4, width - x);
                        int windowHeight = Math.min(flash.getAperture().getCircumRadius() * 4, height - y);
                        for (int i = 0; i < passes; i++)
                        {
                            RasterWindow window = new RasterWindow(new Point(x, y), windowWidth, windowHeight);
                            window.render(primitivesCopy, toolDiameter / 2);
                            int inflation = toolDiameter / 2 + toolDiameter * (100 - overlap) / 100 * (1 + i);
                            window.render(Arrays.asList((GerberPrimitive)flash), inflation);
                            SimpleEdgeDetector detector = new SimpleEdgeDetector(window.getBufferedImage());
                            window = null; // Helping GC to reclaim memory consumed by rendered image
                            detector.process();
                            if (detector.getOutput() != null)
                            {
                                List<Toolpath> toolpaths =
                                        new Tracer(detector.getOutput(), windowWidth, windowHeight, inflation, toolDiameter, translateKnownCircles(windowOffset)).process();
                                detector = null;  // Helping GC to reclaim memory consumed by processed image
                                for (Toolpath t : toolpaths)
                                {
                                    Point from = ((CuttingToolpath)t).getCurve().getFrom();
                                    Point to = ((CuttingToolpath)t).getCurve().getTo();
                                    if ((t instanceof LinearToolpath) && from.distanceTo(to) < MIN_LENGTH)
                                        continue;
                                    Point centerPoint = translateToWindowCoordinates(flash.getPoint(), windowOffset);
                                    int threshold = flash.getAperture().getCircumRadius() + (int)Math.sqrt(inflation * inflation * 2) + 10;
                                    if (from.distanceTo(centerPoint) < threshold && to.distanceTo(centerPoint) < threshold)
                                        segments.addAll(translateToolpaths(Arrays.asList(t), windowOffset));
                                }
                            }
                        }
                    }
                    catch (Throwable e)
                    {
                        LoggerFactory.logException("Exception caught while generating additional passes", e);
                    }
                    return null;
                }
            });
        }
        try
        {
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.DAYS);
        }
        catch (InterruptedException e) {}

        tt = System.currentTimeMillis() - tt;
        System.out.println("Addtional passes generation time: " + tt);

        return segments;
    }
}
