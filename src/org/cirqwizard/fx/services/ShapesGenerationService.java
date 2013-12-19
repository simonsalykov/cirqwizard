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

import org.cirqwizard.appertures.CircularAperture;
import org.cirqwizard.appertures.OctagonalAperture;
import org.cirqwizard.appertures.PolygonalAperture;
import org.cirqwizard.appertures.RectangularAperture;
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
            Line line = new Line(linearShape.getFrom().getX(), linearShape.getFrom().getY(),
                    linearShape.getTo().getX(), linearShape.getTo().getY());
            line.setStrokeWidth(linearShape.getAperture().getWidth(0));
            if (linearShape.getAperture() instanceof CircularAperture)
                line.setStrokeLineCap(StrokeLineCap.ROUND);
            return line;
        }
        else if (primitive instanceof Flash)
        {
            Flash flash = (Flash) primitive;
            if (flash.getAperture() instanceof CircularAperture)
            {
                Circle circle = new Circle(flash.getX(), flash.getY(),
                        ((CircularAperture)flash.getAperture()).getDiameter() / 2);
                circle.setStrokeWidth(0);
                return circle;
            }
            else if (flash.getAperture() instanceof RectangularAperture)
            {
                RectangularAperture aperture = (RectangularAperture)flash.getAperture();
                Rectangle rectangle = new Rectangle(flash.getX() - aperture.getDimensions()[0] / 2,
                        flash.getY() - aperture.getDimensions()[1] / 2,
                        aperture.getDimensions()[0], aperture.getDimensions()[1]);
                rectangle.setStrokeWidth(0);
                return rectangle;
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
                return polygon;
            }
            else if (flash.getAperture() instanceof PolygonalAperture)
            {
                PolygonalAperture aperture = (PolygonalAperture)flash.getAperture();
                Polygon polygon = new Polygon();
                double flashX = flash.getX();
                double flashY = flash.getY();

                for (Point p : aperture.getPoints())
                {
                    polygon.getPoints().add(p.getX() + flashX);
                    polygon.getPoints().add(p.getY() + flashY);
                }

                polygon.setStrokeWidth(0);
                return polygon;
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
