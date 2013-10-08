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

import org.cirqwizard.appertures.CircularAperture;
import org.cirqwizard.appertures.OctagonalAperture;
import org.cirqwizard.appertures.PolygonalAperture;
import org.cirqwizard.appertures.RectangularAperture;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.gerber.LinearShape;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.toolpath.Toolpath;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import org.cirqwizard.geom.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;


public class Raster
{
    private static final int PREVIEW_RESOLUTION_FACTOR = 10;

    private static final int MIN_PERIMETER = 40;    // Minimum feature to be considered should measure 0.1mm x 0.1mm or similar

    private BufferedImage preview;
    private int width;
    private int height;
    private int resolution;
    private int previewResolution;
    private double inflation;
    private ArrayList<GerberPrimitive> primitives = new ArrayList<GerberPrimitive>();
    private ArrayList<RealNumber> radii = new ArrayList<RealNumber>();
    private PointI lastCleared;

    private static final int WINDOWS_CACHE_SIZE = 1;
    private LinkedList<RasterWindow> windows = new LinkedList<RasterWindow>();

    private DoubleProperty generationProgress = new SimpleDoubleProperty();
    private DoubleProperty traceProgress = new SimpleDoubleProperty();

    private int[] dummyArray = new int[1];

    public Raster(double width, double height, int resolution, double inflation)
    {
        int[] cmap = {0x00000000, 0x00ffffff, 0x0000ff00, 0xffffffff};
        IndexColorModel icm = new IndexColorModel(2, 3, cmap, 0, false, 3, DataBuffer.TYPE_BYTE);
        previewResolution = resolution / PREVIEW_RESOLUTION_FACTOR;
        preview = new BufferedImage((int)(width * previewResolution), (int)(height * previewResolution), BufferedImage.TYPE_BYTE_BINARY, icm);
        this.width = (int)(width * resolution);
        this.height = (int)(height * resolution);
        this.inflation = inflation;
        this.resolution = resolution;
    }

    public void addPrimitive(GerberPrimitive primitive)
    {
        primitives.add(primitive);
        if (primitive.getAperture() instanceof CircularAperture)
        {
            CircularAperture aperture = (CircularAperture) primitive.getAperture();
            if (!radii.contains(aperture.getDiameter().divide(2).multiply(resolution)))
                radii.add(aperture.getDiameter().divide(2).multiply(resolution));
        }
    }

    public java.util.List<Toolpath> trace()
    {
        render();
        final ArrayList<Toolpath> segments = new ArrayList<Toolpath>();
        PointI[] p;
        lastCleared = new PointI(0, 0);
        generationProgress.setValue(0);
        while ((p = findNonEmptyPixel()) != null)
        {
            generationProgress.setValue((double) p[0].y / preview.getHeight());
            int perimeter = fill(p[0].x, p[0].y, 0);
            if (perimeter >= MIN_PERIMETER)
                segments.addAll(new Tracer(Raster.this, p[1].x - 1, p[1].y, getPixel(p[1].x, p[1].y), traceProgress, perimeter * 10).trace(new RealNumber(inflation * 2)));
        }
        return segments;
    }

    public DoubleProperty generationProgressProperty()
    {
        return generationProgress;
    }

    public DoubleProperty traceProgressProperty()
    {
        return traceProgress;
    }

    public int getPixel(int x, int y)
    {
        return getPixel(x, y, RenderingHint.SQUARE);
    }

    public int getPixel(int x, int y, RenderingHint hint)
    {
        PointI p = new PointI(x, y);
        RasterWindow window = null;
        for (RasterWindow w : windows)
        {
            if (w.contains(p))
            {
                window = w;
                break;
            }
        }
        if (window != null)
        {
            windows.remove(window);
            windows.addFirst(window);
            return window.getPixel(p);
        }

        int windowWidth = 1000;
        int windowHeight = 1000;
        switch (hint)
        {
            case COLUMN:
                windowWidth = 100;
                windowHeight = 20000;
            break;
            case ROW:
                windowWidth = 20000;
                windowHeight = 100;
            break;
        }
        PointI windowCorner = new PointI(x - windowWidth / 2, y - windowHeight / 2);
        if (windowCorner.x + windowWidth > width)
            windowCorner.x = width - windowWidth + 1;
        if (windowCorner.y + windowHeight > height)
            windowCorner.y = height - windowHeight + 1;
        windowCorner.x = Math.max(0, windowCorner.x);
        windowCorner.y = Math.max(0, windowCorner.y);
        RasterWindow w = renderWindow(windowCorner, windowWidth, windowHeight);
        windows.addFirst(w);
        while (windows.size() > WINDOWS_CACHE_SIZE)
            windows.removeLast();

        return w.getPixel(p);
    }

    public void setPixel(int x, int y, int value)
    {
        dummyArray[0] = value;
        preview.getRaster().setPixel(x, y, dummyArray);
    }

    public java.util.List<RealNumber> getRadii()
    {
        return radii;
    }

    public int getResolution()
    {
        return resolution;
    }

    private RasterWindow renderWindow(PointI lowerLeftCorner, int width, int height)
    {
        int[] cmap = {0x00000000, 0x00ffffff, 0x0000ff00, 0xffffffff};
        IndexColorModel icm = new IndexColorModel(2, 3, cmap, 0, false, 3, DataBuffer.TYPE_BYTE);
        BufferedImage window = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY, icm);

        Graphics2D g = window.createGraphics();
        g.setBackground(Color.GREEN);
        g.clearRect(0, 0, width, height);
        for (GerberPrimitive primitive : primitives)
            renderPrimitive(window.createGraphics(), primitive, inflation, false, lowerLeftCorner);

        return new RasterWindow(window, lowerLeftCorner);
    }

    private void render()
    {
        Graphics2D g = preview.createGraphics();
        g.setBackground(Color.GREEN);
        g.clearRect(0, 0, preview.getWidth(), preview.getHeight());
        for (GerberPrimitive primitive : primitives)
            renderPrimitive(preview.createGraphics(), primitive, inflation, true, null);
        fill(0, 0, 0);
    }

    private void renderPrimitive(Graphics2D g, GerberPrimitive primitive, double inflation, boolean renderPreview, PointI lowerLeftCorner)
    {
        if (!primitive.getAperture().isVisible())
            return;

        if (renderPreview)
        {
            g.transform(AffineTransform.getScaleInstance(previewResolution, previewResolution));
        }
        else
        {
            g.transform(AffineTransform.getTranslateInstance(-lowerLeftCorner.x, -lowerLeftCorner.y));
            g.transform(AffineTransform.getScaleInstance(resolution, resolution));
        }
        g.setColor(Color.WHITE);
        if (primitive instanceof LinearShape)
        {
            LinearShape linearShape = (LinearShape) primitive;
            int cap = linearShape.getAperture() instanceof CircularAperture ? BasicStroke.CAP_ROUND : BasicStroke.CAP_SQUARE;
            g.setStroke(new BasicStroke((float) ((linearShape.getAperture().getWidth(new RealNumber(0)).doubleValue() + inflation * 2)), cap, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(linearShape.getFrom().getX().doubleValue(), linearShape.getFrom().getY().doubleValue(),
                    linearShape.getTo().getX().doubleValue(), linearShape.getTo().getY().doubleValue()));
        }
        else if (primitive instanceof Flash)
        {
            Flash flash = (Flash) primitive;
            if (flash.getAperture() instanceof CircularAperture)
            {
                double d = ((CircularAperture)flash.getAperture()).getDiameter().doubleValue() + inflation * 2;
                double r = d / 2;
                g.fill(new Ellipse2D.Double(flash.getX().doubleValue() - r, flash.getY().doubleValue() - r, d, d));
            }
            else if (flash.getAperture() instanceof RectangularAperture)
            {
                RectangularAperture aperture = (RectangularAperture)flash.getAperture();
                g.fill(new Rectangle2D.Double(flash.getX().doubleValue() - aperture.getDimensions()[0].doubleValue() / 2 - inflation,
                        flash.getY().doubleValue() - aperture.getDimensions()[1].doubleValue() / 2 - inflation,
                        aperture.getDimensions()[0].doubleValue() + inflation * 2,
                        aperture.getDimensions()[1].doubleValue() + inflation * 2));
            }
            else if (flash.getAperture() instanceof OctagonalAperture)
            {
                double edgeOffset = (((OctagonalAperture)flash.getAperture()).getDiameter().doubleValue() + inflation * 2) * (Math.pow(2, 0.5) - 1) / 2;
                double centerOffset = (((OctagonalAperture)flash.getAperture()).getDiameter().doubleValue() + inflation * 2) * 0.5;
                double flashX = flash.getX().doubleValue();
                double flashY = flash.getY().doubleValue();

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
                double flashX = flash.getX().doubleValue();
                double flashY = flash.getY().doubleValue();
                Path2D polygon = new GeneralPath();

                points = expandPolygon(new ArrayList<Point>(points.subList(0, points.size() - 1)), inflation);
                polygon.moveTo(points.get(0).getX().doubleValue() + flashX, points.get(0).getY().doubleValue() + flashY);
                for (int i = 1; i < points.size(); i++)
                    polygon.lineTo(points.get(i).getX().doubleValue() + flashX, points.get(i).getY().doubleValue() + flashY);

                g.fill(polygon);
            }
        }
    }

    private Point vecUnit(Point v)
    {
        double len = Math.sqrt(v.getX().doubleValue() * v.getX().doubleValue() + v.getY().doubleValue() * v.getY().doubleValue());
        return new Point(v.getX().divide(new RealNumber(len)), v.getY().divide(new RealNumber(len)));
    }

    private Point vecMul(Point v, double s)
    {
        return new Point(new RealNumber(v.getX().doubleValue() * s), new RealNumber(v.getY().doubleValue() * s));
    }

    private double vecDot(Point v1, Point v2)
    {
        return v1.getX().doubleValue() * v2.getX().doubleValue() + v1.getY().doubleValue() * v2.getY().doubleValue();
    }

    private Point vecRot90CW(Point v)
    {
        return new Point(v.getY(), v.getX().negate());
    }

    private Point vecRot90CCW(Point v)
    {
        return new Point(v.getY().negate(), v.getX());
    }

    private Point intersectionPoint(Point[] line1, Point[] line2)
    {
        double a1 = line1[1].getX().doubleValue() - line1[0].getX().doubleValue();
        double b1 = line2[0].getX().doubleValue() - line2[1].getX().doubleValue();
        double c1 = line2[0].getX().doubleValue() - line1[0].getX().doubleValue();

        double a2 = line1[1].getY().doubleValue() - line1[0].getY().doubleValue();
        double b2 = line2[0].getY().doubleValue() - line2[1].getY().doubleValue();
        double c2 = line2[0].getY().doubleValue() - line1[0].getY().doubleValue();

        double t = (b1*c2 - b2*c1) / (a2*b1 - a1*b2);

        return new Point(new RealNumber(line1[0].getX().doubleValue() + t * (line1[1].getX().doubleValue() - line1[0].getX().doubleValue())),
                new RealNumber(line1[0].getY().doubleValue() + t * (line1[1].getY().doubleValue() - line1[0].getY().doubleValue())));
    }

    private boolean polyIsCw(Point[] p)
    {
        return vecDot( vecRot90CW(new Point(p[1].getX().subtract(p[0].getX()),  p[1].getY().subtract(p[0].getY()))),
                new Point(p[2].getX().subtract(p[1].getX()),  p[2].getY().subtract(p[1].getY()))) >= 0;
    }

    private ArrayList<Point> expandPolygon(ArrayList<Point> p, double distance)
    {
        ArrayList<Point> expanded = new ArrayList<Point>();
        Point d01, d12;

        for (int i = 0; i < p.size(); ++i)
        {
            Point pt0 = p.get(i > 0 ? i - 1 : p.size() - 1);
            Point pt1 = p.get(i);
            Point pt2 = p.get((i < p.size() - 1) ? i + 1 : 0);

            Point v01 = new Point(pt1.getX().subtract(pt0.getX()), pt1.getY().subtract(pt0.getY()));
            Point v12 = new Point(pt2.getX().subtract(pt1.getX()), pt2.getY().subtract(pt1.getY()));

            if (polyIsCw(p.toArray(new Point[p.size()])))
            {
                d01 = vecMul(vecUnit(vecRot90CCW(v01)), distance);
                d12 = vecMul(vecUnit(vecRot90CCW(v12)), distance);
            }
            else
            {
                d01 = vecMul(vecUnit(vecRot90CW(v01)), distance);
                d12 = vecMul(vecUnit(vecRot90CW(v12)), distance);
            }

            Point ptx0  = new Point(pt0.getX().add(d01.getX()), pt0.getY().add(d01.getY()));
            Point ptx10 = new Point(pt1.getX().add(d01.getX()), pt1.getY().add(d01.getY()));
            Point ptx12 = new Point(pt1.getX().add(d12.getX()), pt1.getY().add(d12.getY()));
            Point ptx2  = new Point(pt2.getX().add(d12.getX()), pt2.getY().add(d12.getY()));

            expanded.add(intersectionPoint(new Point[]{ptx0, ptx10}, new Point[]{ptx12, ptx2}));
        }
        return expanded;
    }


    private int fill(int x, int y, int fillColor)
    {
        WritableRaster r = preview.getRaster();
        int sampleColor = r.getPixel(x, y, (int[])null)[0];
        if (sampleColor == fillColor)
            return 0;
        LinkedList<PointI> queue = new LinkedList<PointI>();
        queue.add(new PointI(x, y));

        int y1;
        boolean spanLeft, spanRight;
        int perimeter = 0;
        while (!queue.isEmpty())
        {
            PointI p = queue.removeFirst();
            y1 = p.y;
            while (y1 >= 0 && r.getPixel(p.x, y1, dummyArray)[0] == sampleColor)
                y1--;
            y1++;
            spanLeft = false;
            spanRight = false;
            while (y1 < preview.getHeight() && r.getPixel(p.x, y1, dummyArray)[0] == sampleColor)
            {
                dummyArray[0] = fillColor;
                r.setPixel(p.x, y1, dummyArray);
                if (!spanLeft && p.x > 0 && r.getPixel(p.x - 1, y1, dummyArray)[0] == sampleColor)
                {
                    queue.addFirst(new PointI(p.x - 1, y1));
                    spanLeft = true;
                }
                else if (spanLeft && p.x > 0 && r.getPixel(p.x - 1, y1, dummyArray)[0] != sampleColor)
                {
                    spanLeft = false;
                    perimeter++;
                }

                if (!spanRight && p.x < preview.getWidth() - 1 && r.getPixel(p.x + 1, y1, dummyArray)[0] == sampleColor)
                {
                    queue.addFirst(new PointI(p.x + 1, y1));
                    spanRight = true;
                }
                else if (spanRight && p.x < preview.getWidth() - 1 && r.getPixel(p.x + 1, y1, dummyArray)[0] != sampleColor)
                {
                    spanRight = false;
                    perimeter++;
                }

                y1++;
            }
            perimeter += 2;
        }

        return perimeter;
    }

    private PointI[] findNonEmptyPixel()
    {
        WritableRaster r = preview.getRaster();
        for (; lastCleared.y < preview.getHeight(); lastCleared.y++)
            for (lastCleared.x = 0; lastCleared.x < preview.getWidth(); lastCleared.x++)
                if (r.getPixel(lastCleared.x, lastCleared.y, (int[])null)[0] != 0)
                {
                    int color = r.getPixel(lastCleared.x, lastCleared.y, (int[])null)[0];
                    int y0 = lastCleared.y * PREVIEW_RESOLUTION_FACTOR - PREVIEW_RESOLUTION_FACTOR * 3;
                    y0 = Math.max(0, y0);
                    int x0 = lastCleared.x * PREVIEW_RESOLUTION_FACTOR - PREVIEW_RESOLUTION_FACTOR * 3;
                    x0 = Math.max(0, x0);
                    int yMax = Math.min(height, y0 + PREVIEW_RESOLUTION_FACTOR * 6);
                    int xMax = Math.min(width, x0 + PREVIEW_RESOLUTION_FACTOR * 6);
                    for (int y1 = y0; y1 < yMax; y1++)
                    {
                        boolean isOutside = false;
                        for (int x1 = x0; x1 < xMax; x1++)
                        {
                            if (!isOutside)
                                isOutside = getPixel(x1, y1) != color;
                            else if (getPixel(x1, y1) == color)
                                return new PointI[] {new PointI(lastCleared.x, lastCleared.y), new PointI(x1, y1)};
                        }
                    }
                }
        return null;
    }

    public void saveTo(String file)
    {
        try
        {
            ImageIO.write(preview, "png", new File(file));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

//    public void saveWindow(String file)
//    {
//        System.out.println("windowLeftCorner: " + windowLowerLeftCorner);
//        try
//        {
//            ImageIO.write(window, "png", new File(file));
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//    }

    public static enum RenderingHint {ROW, COLUMN, SQUARE}

}
