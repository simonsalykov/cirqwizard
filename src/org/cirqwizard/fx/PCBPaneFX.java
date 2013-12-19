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

package org.cirqwizard.fx;

import org.cirqwizard.appertures.*;
import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.gerber.LinearShape;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.toolpath.CircularToolpath;
import org.cirqwizard.toolpath.DrillPoint;
import org.cirqwizard.toolpath.LinearToolpath;
import org.cirqwizard.toolpath.Toolpath;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Transform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PCBPaneFX extends Region
{
    private static final double DEFAULT_SCALE = 5.0;

    public static final Color BACKGROUND_COLOR = Color.web("#ddfbdd");
    public static final Color ENABLED_TOOLPATH_COLOR = Color.web("#191970");
    public static final Color PASTE_TOOLPATH_COLOR = Color.GOLD;
    public static final Color DISABLED_TOOLPATH_COLOR = Color.web("#dcdcdc");
    public static final Color SELECTED_TOOLPATH_COLOR = Color.CYAN;
    public static final Color TOP_TRACE_COLOR = Color.RED;
    public static final Color BOTTOM_TRACE_COLOR = Color.BLUE;
    public static final Color DRILL_POINT_COLOR = Color.BLACK;
    public static final Color CONTOUR_COLOR = Color.MAGENTA;
    public static final Color SOLDER_PAD_COLOR = Color.NAVY;
    public static final Color PCB_BORDER = Color.BLACK;

    private Property<Double> scaleProperty = new SimpleObjectProperty<Double>(DEFAULT_SCALE);

    private double boardWidth;
    private double boardHeight;

    private java.util.List<GerberPrimitive> gerberPrimitives;
    private Property<ObservableList<Toolpath>> toolpaths = new SimpleListProperty<Toolpath>();

    private Canvas canvas;
    private Rectangle selectionRectangle;

    private Color gerberColor = TOP_TRACE_COLOR;
    private Color toolpathColor = ENABLED_TOOLPATH_COLOR;

    public PCBPaneFX()
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("pcb-pane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try
        {
            fxmlLoader.load();
        }
        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }

        scaleProperty.addListener(new ChangeListener<Double>()
        {
            @Override
            public void changed(ObservableValue<? extends Double> observableValue, Double aDouble, Double aDouble2)
            {
                repaint();
            }
        });
        toolpaths.addListener(new ChangeListener<ObservableList<Toolpath>>()
        {
            @Override
            public void changed(ObservableValue<? extends ObservableList<Toolpath>> observableValue, ObservableList<Toolpath> shapes, ObservableList<Toolpath> shapes2)
            {
                repaint();
            }
        });
    }

    public Property<ObservableList<Toolpath>> toolpathsProperty()
    {
        return toolpaths;
    }

    public void setGerberPrimitives(List<GerberPrimitive> gerberPrimitives)
    {
        this.gerberPrimitives = gerberPrimitives;
        repaint();
    }

    public void setGerberColor(Color gerberColor)
    {
        this.gerberColor = gerberColor;
    }

    public void setToolpathColor(Color toolpathColor)
    {
        this.toolpathColor = toolpathColor;
    }

    public void repaint()
    {
        getChildren().remove(canvas);
        renderImage();
        getChildren().add(canvas);
    }

    public void repaint(List<? extends Toolpath> toolpaths)
    {
        GraphicsContext g = canvas.getGraphicsContext2D();
        for (Toolpath toolpath : toolpaths)
            renderToolpath(g, toolpath);
    }

    private void renderImage()
    {
        canvas = new Canvas(boardWidth * scaleProperty.getValue() + 1, boardHeight * scaleProperty.getValue() + 1);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(BACKGROUND_COLOR);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setStroke(PCB_BORDER);
        g.setLineWidth(1);
        g.strokeRect(0, 0, canvas.getWidth() - 1, canvas.getHeight() - 1);
        g.scale(scaleProperty.getValue(), -scaleProperty.getValue());
        g.translate(0, -boardHeight);
        if (gerberPrimitives != null)
            for (GerberPrimitive primitive : gerberPrimitives)
                renderPrimitive(g, primitive);
        if (toolpaths.getValue() != null)
            for (Toolpath toolpath : toolpaths.getValue())
                renderToolpath(g, toolpath);
    }

    private void renderPrimitive(GraphicsContext g, GerberPrimitive primitive)
    {
        if (!primitive.getAperture().isVisible())
            return;

        g.setStroke(gerberColor);
        g.setFill(gerberColor);
        if (primitive instanceof LinearShape)
        {
            LinearShape linearShape = (LinearShape) primitive;
            g.setLineCap(linearShape.getAperture() instanceof CircularAperture ? StrokeLineCap.ROUND : StrokeLineCap.SQUARE);
            g.setLineWidth(linearShape.getAperture().getWidth(new RealNumber(0)).doubleValue());
            g.strokeLine(linearShape.getFrom().getX().doubleValue(), linearShape.getFrom().getY().doubleValue(),
                    linearShape.getTo().getX().doubleValue(), linearShape.getTo().getY().doubleValue());
        }
        else if (primitive instanceof Flash)
        {
            Flash flash = (Flash) primitive;
            if (flash.getAperture() instanceof CircularAperture)
            {
                double d = ((CircularAperture)flash.getAperture()).getDiameter().doubleValue();
                double r = d / 2;
                g.fillOval(flash.getX().doubleValue() - r, flash.getY().doubleValue() - r, d, d);
            }
            else if (flash.getAperture() instanceof RectangularAperture)
            {
                RectangularAperture aperture = (RectangularAperture)flash.getAperture();
                g.fillRect(flash.getX().doubleValue() - aperture.getDimensions()[0].doubleValue() / 2,
                        flash.getY().doubleValue() - aperture.getDimensions()[1].doubleValue() / 2,
                        aperture.getDimensions()[0].doubleValue(),
                        aperture.getDimensions()[1].doubleValue());
            }
            else if (flash.getAperture() instanceof OctagonalAperture)
            {
                double edgeOffset = ((OctagonalAperture)flash.getAperture()).getDiameter().doubleValue() * (Math.pow(2, 0.5) - 1) / 2;
                double centerOffset = ((OctagonalAperture)flash.getAperture()).getDiameter().doubleValue() * 0.5;
                double x = flash.getX().doubleValue();
                double y = flash.getY().doubleValue();

                g.beginPath();
                g.moveTo(centerOffset + x, edgeOffset + y);
                g.lineTo(edgeOffset + x, centerOffset + y);
                g.lineTo(-edgeOffset + x, centerOffset + y);
                g.lineTo(-centerOffset + x, edgeOffset + y);
                g.lineTo(-centerOffset + x, -edgeOffset + y);
                g.lineTo(-edgeOffset + x, -centerOffset + y);
                g.lineTo(edgeOffset + x, -centerOffset + y);
                g.lineTo(centerOffset + x, -edgeOffset + y);
                g.closePath();
                g.fill();
            }
            else if (flash.getAperture() instanceof PolygonalAperture)
            {
                PolygonalAperture aperture = (PolygonalAperture)flash.getAperture();
                ArrayList<Point> points = aperture.getPoints();
                double flashX = flash.getX().doubleValue();
                double flashY = flash.getY().doubleValue();

                g.beginPath();
                g.moveTo(points.get(0).getX().doubleValue() + flashX, points.get(0).getY().doubleValue() + flashY);
                for (int i = 1; i < points.size(); i++)
                    g.lineTo(points.get(i).getX().doubleValue() + flashX, points.get(i).getY().doubleValue() + flashY);

                g.closePath();
                g.fill();
            }
            else if (flash.getAperture() instanceof OvalAperture)
            {
                OvalAperture aperture = (OvalAperture)flash.getAperture();
                double flashX = flash.getX().doubleValue();
                double flashY = flash.getY().doubleValue();
                double width = aperture.getWidth().doubleValue();
                double height = aperture.getHeight().doubleValue();
                double d = Math.min(width, height);
                double l = aperture.isHorizontal() ? width - height : height - width;
                double xOffset = aperture.isHorizontal() ? l / 2 : 0;
                double yOffset = aperture.isHorizontal() ? 0 : l / 2;
                g.fillOval(flashX + xOffset - d / 2, flashY + yOffset - d / 2, d, d);
                g.fillOval(flashX - xOffset - d / 2, flashY - yOffset - d / 2, d, d);

                double rectX = aperture.isHorizontal() ? flashX - l / 2 : flashX - width / 2;
                double rectY = aperture.isHorizontal() ? flashY - height / 2 : flashY - l / 2;
                double rectWidth =  aperture.isHorizontal() ? l : width;
                double rectHeight =  aperture.isHorizontal() ? height : l;
                g.fillRect(rectX, rectY, rectWidth, rectHeight);
            }
        }
    }

    private void renderToolpath(GraphicsContext g, Toolpath toolpath)
    {
        Color color = toolpath.isEnabled() ? toolpathColor : DISABLED_TOOLPATH_COLOR;
        if (toolpath.isSelected())
            color = SELECTED_TOOLPATH_COLOR;
        g.setStroke(color);
        if (toolpath instanceof LinearToolpath)
        {
            LinearToolpath linearToolpath = (LinearToolpath) toolpath;
            g.setLineCap(StrokeLineCap.ROUND);
            g.setLineWidth(linearToolpath.getToolDiameter().doubleValue());
            g.strokeLine(linearToolpath.getCurve().getFrom().getX().doubleValue(), linearToolpath.getCurve().getFrom().getY().doubleValue(),
                    linearToolpath.getCurve().getTo().getX().doubleValue(), linearToolpath.getCurve().getTo().getY().doubleValue());
        }
        else if (toolpath instanceof CircularToolpath)
        {
            CircularToolpath circularToolpath = (CircularToolpath) toolpath;
            g.setLineCap(StrokeLineCap.ROUND);
            g.setLineWidth(circularToolpath.getToolDiameter().doubleValue());
            Arc arc = (Arc) circularToolpath.getCurve();
            g.strokeArc(arc.getCenter().getX().doubleValue()- arc.getRadius().doubleValue(),
                    arc.getCenter().getY().doubleValue() - arc.getRadius().doubleValue(),
                    arc.getRadius().doubleValue() * 2, arc.getRadius().doubleValue() * 2,
                    -Math.toDegrees(arc.getStart().doubleValue()), Math.toDegrees(arc.getAngle().doubleValue()), ArcType.OPEN);
        }
        else if (toolpath instanceof DrillPoint)
        {
            DrillPoint drillPoint = (DrillPoint) toolpath;
            g.setFill(color);
            g.fillOval(drillPoint.getPoint().getX().doubleValue() - drillPoint.getToolDiameter().doubleValue() / 2,
                    drillPoint.getPoint().getY().doubleValue() - drillPoint.getToolDiameter().doubleValue() / 2,
                    drillPoint.getToolDiameter().doubleValue(), drillPoint.getToolDiameter().doubleValue());
        }
    }

    public void setSelection(double x, double y, double width, double height)
    {
        if (selectionRectangle != null)
            getChildren().remove(selectionRectangle);
        selectionRectangle = new Rectangle(x, y, width, height);
        selectionRectangle.setStrokeWidth(0.5 / scaleProperty().getValue());
        selectionRectangle.getStyleClass().add("pcb-selection-rect");
        selectionRectangle.getTransforms().add(Transform.scale(scaleProperty.getValue(), -scaleProperty.getValue()));
        selectionRectangle.getTransforms().add(Transform.translate(0, -boardHeight));
        getChildren().add(selectionRectangle);
    }

    public void clearSelection()
    {
        if (selectionRectangle != null)
        {
            getChildren().remove(selectionRectangle);
            selectionRectangle = null;
        }
    }

    public Property<Double> scaleProperty()
    {
        return scaleProperty;
    }

    public void setBoardWidth(double boardWidth)
    {
        this.boardWidth = boardWidth;
    }

    public void setBoardHeight(double boardHeight)
    {
        this.boardHeight = boardHeight;
    }
}
