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

import org.cirqwizard.appertures.*;
import org.cirqwizard.appertures.macro.*;
import org.cirqwizard.fx.Context;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.gerber.LinearShape;
import org.cirqwizard.gerber.Region;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.toolpath.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.shape.*;

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
            Line line = new Line(linearShape.getFrom().getX().doubleValue(), linearShape.getFrom().getY().doubleValue(),
                    linearShape.getTo().getX().doubleValue(), linearShape.getTo().getY().doubleValue());
            line.setStrokeWidth(linearShape.getAperture().getWidth(new RealNumber(0)).doubleValue());
            if (linearShape.getAperture() instanceof CircularAperture)
                line.setStrokeLineCap(StrokeLineCap.ROUND);
            if (linearShape.getAperture().getWidth(new RealNumber(0)).equals(0))
                line.setClip(new Rectangle(linearShape.getFrom().getX().doubleValue(), linearShape.getFrom().getY().doubleValue(),
                        linearShape.getTo().getX().doubleValue() - linearShape.getFrom().getX().doubleValue(), linearShape.getTo().getY().doubleValue() - linearShape.getFrom().getY().doubleValue()));
            return Arrays.asList((Shape)line);
        }
        else if (primitive instanceof Region)
        {
            Region region = (Region) primitive;
            Polygon polygon = new Polygon();
            List<LinearShape> segments = region.getSegments();
            for (LinearShape segment : segments)
            {
                polygon.getPoints().add(segment.getFrom().getX().doubleValue());
                polygon.getPoints().add(segment.getFrom().getY().doubleValue());
            }
            polygon.getPoints().add(segments.get(segments.size() - 1).getTo().getX().doubleValue());
            polygon.getPoints().add(segments.get(segments.size() - 1).getTo().getY().doubleValue());

            polygon.setStrokeWidth(0);
            return Arrays.asList((Shape) polygon);
        }
        else if (primitive instanceof Flash)
        {
            Flash flash = (Flash) primitive;
            if (flash.getAperture() instanceof CircularAperture)
            {
                Circle circle = new Circle(flash.getX().doubleValue(), flash.getY().doubleValue(),
                        ((CircularAperture)flash.getAperture()).getDiameter().doubleValue() / 2);
                circle.setStrokeWidth(0);
                return Arrays.asList((Shape) circle);
            }
            else if (flash.getAperture() instanceof RectangularAperture)
            {
                RectangularAperture aperture = (RectangularAperture)flash.getAperture();
                Rectangle rectangle = new Rectangle(flash.getX().doubleValue() - aperture.getDimensions()[0].doubleValue() / 2,
                        flash.getY().doubleValue() - aperture.getDimensions()[1].doubleValue() / 2,
                        aperture.getDimensions()[0].doubleValue(), aperture.getDimensions()[1].doubleValue());
                rectangle.setStrokeWidth(0);
                return Arrays.asList((Shape) rectangle);
            }
            else if (flash.getAperture() instanceof OctagonalAperture)
            {
                double octagonDiameter = ((OctagonalAperture)flash.getAperture()).getDiameter().doubleValue();
                double edgeOffset = octagonDiameter * (Math.pow(2, 0.5) - 1) / 2;
                double centerOffset = octagonDiameter * 0.5;
                double flashX = flash.getX().doubleValue();
                double flashY = flash.getY().doubleValue();

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
                double flashX = flash.getX().doubleValue();
                double flashY = flash.getY().doubleValue();
                double width = aperture.getWidth().doubleValue();
                double height = aperture.getHeight().doubleValue();
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
                        Line line = new Line(from.getX().doubleValue(), from.getY().doubleValue(),
                                to.getX().doubleValue(), to.getY().doubleValue());
                        line.setStrokeWidth(centerLine.getHeight().doubleValue());
                        line.setStrokeLineCap(StrokeLineCap.BUTT);
                        list.add(line);
                    }
                    else if (p instanceof MacroVectorLine)
                    {
                        MacroVectorLine vectorLine = (MacroVectorLine) p;
                        Point from = vectorLine.getTranslatedStart().add(flash.getPoint());
                        Point to = vectorLine.getTranslatedEnd().add(flash.getPoint());
                        Line line = new Line(from.getX().doubleValue(), from.getY().doubleValue(),
                                to.getX().doubleValue(), to.getY().doubleValue());
                        line.setStrokeLineCap(StrokeLineCap.BUTT);
                        line.setStrokeWidth(vectorLine.getWidth().doubleValue());
                        list.add(line);
                    }
                    else if (p instanceof MacroCircle)
                    {
                        MacroCircle circle = (MacroCircle) p;
                        double d = circle.getDiameter().doubleValue();
                        double r = d / 2;
                        Point point = circle.getCenter().add(flash.getPoint());

                        Circle c = new Circle(point.getX().doubleValue(), point.getY().doubleValue(),
                                circle.getDiameter().doubleValue() / 2);
                        c.setStrokeWidth(0);
                        list.add(c);
                    }
                    else if (p instanceof MacroOutline)
                    {
                        MacroOutline outline = (MacroOutline) p;
                        double x = flash.getX().doubleValue();
                        double y = flash.getY().doubleValue();

                        Polygon polygon = new Polygon();
                        for (Point point : outline.getTranslatedPoints())
                            polygon.getPoints().addAll(point.getX().doubleValue() + x, point.getY().doubleValue() + y);
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
        Shape shape = new Arc(drillPoint.getPoint().getX().doubleValue(), drillPoint.getPoint().getY().doubleValue(),
                drillPoint.getToolDiameter().doubleValue() / 2, drillPoint.getToolDiameter().doubleValue() / 2, 0, 360);
        shape.setStrokeLineCap(StrokeLineCap.ROUND);
        shape.setStrokeWidth(drillPoint.getToolDiameter().doubleValue());
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
                        shape.getStyleClass().add(style);
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
                if (!context.isFileLoaded())
                    context.loadFile();
                if (context.getTopTracesLayer() != null)
                    addGerberPrimitives("pcb-top-trace", context.getTopTracesLayer().getElements());
                if (context.getBottomTracesLayer() != null)
                    addGerberPrimitives("pcb-bottom-trace", context.getBottomTracesLayer().getElements());
                if (context.getMillingLayer() != null)
                    addGerberPrimitives("pcb-contour-milling", context.getMillingLayer().getElements());
                if (context.getDrillingLayer() != null)
                {
                    for (DrillPoint p : context.getDrillingLayer().getToolpaths())
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
