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

package org.cirqwizard.fx.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.shape.*;
import org.cirqwizard.appertures.CircularAperture;
import org.cirqwizard.appertures.OctagonalAperture;
import org.cirqwizard.appertures.OvalAperture;
import org.cirqwizard.appertures.RectangularAperture;
import org.cirqwizard.appertures.macro.*;
import org.cirqwizard.fx.Context;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.*;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.toolpath.DrillPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ShapesGenerationService extends Service<ObservableList<Shape>>
{
    protected Context context;

    public ShapesGenerationService(Context context)
    {
        this.context = context;
    }

    @Override
    protected Task<ObservableList<Shape>> createTask()
    {
        return new ShapesGenerationTask();
    }

    private java.util.List<Shape> getShapeForPrimitive(GerberPrimitive primitive)
    {
        if (primitive instanceof LinearShape)
        {
            LinearShape linearShape = (LinearShape) primitive;
            Line line = new Line(linearShape.getFrom().getX(), linearShape.getFrom().getY(),
                    linearShape.getTo().getX(), linearShape.getTo().getY());
            line.setStrokeWidth(linearShape.getAperture().getWidth());
            if (linearShape.getAperture() instanceof CircularAperture)
                line.setStrokeLineCap(StrokeLineCap.ROUND);
            if (linearShape.getAperture().getWidth() == 0)
                line.setClip(new Rectangle(linearShape.getFrom().getX(), linearShape.getFrom().getY(),
                        linearShape.getTo().getX() - linearShape.getFrom().getX(), linearShape.getTo().getY() - linearShape.getFrom().getY()));
            return Arrays.asList((Shape)line);
        }
        else if (primitive instanceof CircularShape)
        {
            CircularShape circularShape = (CircularShape) primitive;
            Arc arc = new Arc(circularShape.getArc().getCenter().getX(), circularShape.getArc().getCenter().getY(),
                    circularShape.getArc().getRadius(), circularShape.getArc().getRadius(),
                    -Math.toDegrees(circularShape.getArc().getStart()),
                    Math.toDegrees(circularShape.getArc().getAngle()) * (circularShape.getArc().isClockwise() ? 1 : -1));
            arc.setStrokeWidth(circularShape.getAperture().getWidth());
            if (circularShape.getAperture() instanceof CircularAperture)
                arc.setStrokeLineCap(StrokeLineCap.ROUND);
            return Arrays.asList((Shape)arc);
        }
        else if (primitive instanceof Region)
        {
            Region region = (Region) primitive;
            Path path = new Path();
            List<GerberPrimitive> segments = region.getSegments();

            Point firstPoint = ((InterpolatingShape)segments.get(0)).getFrom();
            path.getElements().add(new MoveTo(firstPoint.getX(), firstPoint.getY()));

            for (GerberPrimitive segment : segments)
            {
                if (segment instanceof LinearShape)
                {
                    LinearShape l = (LinearShape) segment;
                    path.getElements().add(new LineTo(l.getTo().getX(), l.getTo().getY()));
                }
                else if (segment instanceof CircularShape)
                {
                    org.cirqwizard.geom.Arc arc = ((CircularShape) segment).getArc();
                    path.getElements().add(new ArcTo(arc.getRadius(), arc.getRadius(),
                            -Math.toDegrees(arc.getStart()),
                            arc.getTo().getX(), arc.getTo().getY(), false, false));
                }
            }

            path.setStrokeWidth(0);
            return Arrays.asList((Shape) path);
        }
        else if (primitive instanceof Flash)
        {
            Flash flash = (Flash) primitive;
            if (flash.getAperture() instanceof CircularAperture)
            {
                Circle circle = new Circle(flash.getX(), flash.getY(),
                        ((CircularAperture)flash.getAperture()).getDiameter() / 2);
                circle.setStrokeWidth(0);
                return Arrays.asList((Shape) circle);
            }
            else if (flash.getAperture() instanceof RectangularAperture)
            {
                RectangularAperture aperture = (RectangularAperture)flash.getAperture();
                Rectangle rectangle = new Rectangle(flash.getX() - aperture.getDimensions()[0] / 2,
                        flash.getY() - aperture.getDimensions()[1] / 2,
                        aperture.getDimensions()[0], aperture.getDimensions()[1]);
                rectangle.setStrokeWidth(0);
                return Arrays.asList((Shape) rectangle);
            }
            else if (flash.getAperture() instanceof OctagonalAperture)
            {
                double octagonDiameter = ((OctagonalAperture)flash.getAperture()).getDiameter();
                double edgeOffset = octagonDiameter * (Math.pow(2, 0.5) - 1) / 2;
                double centerOffset = octagonDiameter * 0.5;
                double flashX = flash.getX();
                double flashY = flash.getY();

                Polygon polygon = new Polygon();
                polygon.getPoints().addAll(
                        centerOffset + flashX, edgeOffset + flashY,
                        edgeOffset + flashX, centerOffset + flashY,
                        -edgeOffset + flashX, centerOffset + flashY,
                        -centerOffset + flashX, edgeOffset + flashY,
                        -centerOffset + flashX, -edgeOffset + flashY,
                        -edgeOffset + flashX, -centerOffset + flashY,
                        edgeOffset + flashX, -centerOffset + flashY,
                        centerOffset + flashX, -edgeOffset + flashY );
                polygon.setStrokeWidth(0);
                return Arrays.asList((Shape) polygon);
            }
            else if (flash.getAperture() instanceof OvalAperture)
            {
                OvalAperture aperture = (OvalAperture)flash.getAperture();
                double flashX = flash.getX();
                double flashY = flash.getY();
                double width = aperture.getWidth();
                double height = aperture.getHeight();
                double r = aperture.isHorizontal() ? height / 2 : width / 2;
                double l = aperture.isHorizontal() ? width - height : height - width;
                Path path = new Path();

                if (aperture.isHorizontal())
                {
                    path.getElements().add(new MoveTo(flashX - l / 2, flashY + height / 2));
                    path.getElements().add(new HLineTo(flashX + l / 2));
                    path.getElements().add(new ArcTo(r, r, 0, flashX + l / 2, flashY - height / 2, false, false));
                    path.getElements().add(new HLineTo(flashX - l / 2));
                    path.getElements().add(new ArcTo(r, r, 0, flashX - l / 2, flashY + height / 2, false, false));
                }
                else
                {
                    path.getElements().add(new MoveTo(flashX - width / 2, flashY + l / 2));
                    path.getElements().add(new ArcTo(r, r, 0, flashX + width / 2, flashY + l / 2, false, false));
                    path.getElements().add(new VLineTo(flashY - l / 2));
                    path.getElements().add(new ArcTo(r, r, 0, flashX - width / 2, flashY - l / 2, false, false));
                    path.getElements().add(new VLineTo(flashY + l / 2));
                }

                path.setStrokeWidth(0);
                return Arrays.asList((Shape) path);
            }
            else if (flash.getAperture() instanceof ApertureMacro)
            {
                ArrayList<Shape> list = new ArrayList<>();
                ApertureMacro macro = (ApertureMacro) flash.getAperture();
                for (MacroPrimitive p : macro.getPrimitives())
                {
                    if (p instanceof MacroCenterLine)
                    {
                        MacroCenterLine centerLine = (MacroCenterLine) p;
                        Point from = centerLine.getFrom().add(flash.getPoint());
                        Point to = centerLine.getTo().add(flash.getPoint());
                        Line line = new Line(from.getX(), from.getY(), to.getX(), to.getY());
                        line.setStrokeWidth(centerLine.getHeight());
                        line.setStrokeLineCap(StrokeLineCap.BUTT);
                        list.add(line);
                    }
                    else if (p instanceof MacroVectorLine)
                    {
                        MacroVectorLine vectorLine = (MacroVectorLine) p;
                        Point from = vectorLine.getTranslatedStart().add(flash.getPoint());
                        Point to = vectorLine.getTranslatedEnd().add(flash.getPoint());
                        Line line = new Line(from.getX(), from.getY(), to.getX(), to.getY());
                        line.setStrokeLineCap(StrokeLineCap.BUTT);
                        line.setStrokeWidth(vectorLine.getWidth());
                        list.add(line);
                    }
                    else if (p instanceof MacroCircle)
                    {
                        MacroCircle circle = (MacroCircle) p;
                        double d = circle.getDiameter();
                        double r = d / 2;
                        Point point = circle.getCenter().add(flash.getPoint());

                        Circle c = new Circle(point.getX(), point.getY(), circle.getDiameter() / 2);
                        c.setStrokeWidth(0);
                        list.add(c);
                    }
                    else if (p instanceof MacroOutline)
                    {
                        MacroOutline outline = (MacroOutline) p;
                        double x = flash.getX();
                        double y = flash.getY();

                        Polygon polygon = new Polygon();
                        for (Point point : outline.getTranslatedPoints())
                            polygon.getPoints().addAll(point.getX() + x, point.getY() + y);
                        polygon.setStrokeWidth(0);
                        list.add(polygon);
                    }
                }
                return list;
            }
        }
        return null;
    }

    public Shape createShapeForDrillPoint(DrillPoint drillPoint)
    {
        Shape shape = new Arc(drillPoint.getPoint().getX(), drillPoint.getPoint().getY(),
                drillPoint.getToolDiameter() / 2, drillPoint.getToolDiameter() / 2, 0, 360);
        shape.setStrokeLineCap(StrokeLineCap.ROUND);
        shape.setStrokeWidth(drillPoint.getToolDiameter());
        return shape;

    }

    public class ShapesGenerationTask extends Task<ObservableList<Shape>>
    {
        protected ObservableList<Shape> list = FXCollections.observableArrayList();

        protected void addGerberPrimitives(String style, List<GerberPrimitive> primitives)
        {
            for (GerberPrimitive primitive : primitives)
            {
                for (Shape shape : getShapeForPrimitive(primitive))
                {
                    if (shape != null)
                    {
                        shape.getStyleClass().add(primitive.getPolarity() == GerberPrimitive.Polarity.DARK ? style : "pcb-clear-polarity");
                        list.add(shape);
                    }
                }
            }
        }

        @Override
        protected ObservableList<Shape> call() throws Exception
        {
            try
            {
                if (!context.getPcbLayout().isFileLoaded())
                    context.getPcbLayout().loadFile(context.getPcbLayout().getFileName());
                if (context.getPcbLayout().getTopTracesLayer() != null)
                    addGerberPrimitives("pcb-top-trace", context.getPcbLayout().getTopTracesLayer().getElements());
                if (context.getPcbLayout().getBottomTracesLayer() != null)
                    addGerberPrimitives("pcb-bottom-trace", context.getPcbLayout().getBottomTracesLayer().getElements());
                if (context.getPcbLayout().getMillingLayer() != null)
                    addGerberPrimitives("pcb-contour-milling", context.getPcbLayout().getMillingLayer().getElements());
                if (context.getPcbLayout().getDrillingLayer() != null)
                {
                    for (DrillPoint p : context.getPcbLayout().getDrillingLayer().getToolpaths())
                    {
                        Shape shape = createShapeForDrillPoint(p);
                        shape.getStyleClass().add("pcb-drill-point");
                        list.add(shape);
                    }
                }
            }
            catch(Exception e)
            {
                LoggerFactory.logException("Error in ShapesGenerationTask:", e);
                throw e;
            }
            return list;
        }
    }

}
