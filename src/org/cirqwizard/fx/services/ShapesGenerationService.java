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
import org.cirqwizard.fx.Context;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.gerber.LinearShape;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.toolpath.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.shape.*;

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

    private Shape getShapeForPrimitive(GerberPrimitive primitive)
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
            return line;
        }
        else if (primitive instanceof Flash)
        {
            Flash flash = (Flash) primitive;
            if (flash.getAperture() instanceof CircularAperture)
            {
                Circle circle = new Circle(flash.getX().doubleValue(), flash.getY().doubleValue(),
                        ((CircularAperture)flash.getAperture()).getDiameter().doubleValue() / 2);
                circle.setStrokeWidth(0);
                return circle;
            }
            else if (flash.getAperture() instanceof RectangularAperture)
            {
                RectangularAperture aperture = (RectangularAperture)flash.getAperture();
                Rectangle rectangle = new Rectangle(flash.getX().doubleValue() - aperture.getDimensions()[0].doubleValue() / 2,
                        flash.getY().doubleValue() - aperture.getDimensions()[1].doubleValue() / 2,
                        aperture.getDimensions()[0].doubleValue(), aperture.getDimensions()[1].doubleValue());
                rectangle.setStrokeWidth(0);
                return rectangle;
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
                return polygon;
            }
            else if (flash.getAperture() instanceof PolygonalAperture)
            {
                PolygonalAperture aperture = (PolygonalAperture)flash.getAperture();
                Polygon polygon = new Polygon();
                double flashX = flash.getX().doubleValue();
                double flashY = flash.getY().doubleValue();

                for (Point p : aperture.getPoints())
                {
                    polygon.getPoints().add(p.getX().doubleValue() + flashX);
                    polygon.getPoints().add(p.getY().doubleValue() + flashY);
                }

                polygon.setStrokeWidth(0);
                return polygon;
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
                return path;
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
                Shape shape = getShapeForPrimitive(primitive);
                shape.getStyleClass().add(style);
                list.add(shape);
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
