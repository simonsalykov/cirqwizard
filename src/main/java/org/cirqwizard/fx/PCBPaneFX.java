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

import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import org.cirqwizard.appertures.CircularAperture;
import org.cirqwizard.appertures.OctagonalAperture;
import org.cirqwizard.appertures.OvalAperture;
import org.cirqwizard.appertures.RectangularAperture;
import org.cirqwizard.appertures.macro.*;
import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.*;
import org.cirqwizard.toolpath.CircularToolpath;
import org.cirqwizard.toolpath.DrillPoint;
import org.cirqwizard.toolpath.LinearToolpath;
import org.cirqwizard.toolpath.Toolpath;

import java.io.IOException;
import java.util.List;


public class PCBPaneFX extends javafx.scene.layout.Region
{
    private static final double DEFAULT_SCALE = 0.005;

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

    private Property<Double> scaleProperty = new SimpleObjectProperty<>(DEFAULT_SCALE);

    private double boardWidth;
    private double boardHeight;

    private java.util.List<GerberPrimitive> gerberPrimitives;
    private Property<ObservableList<Toolpath>> toolpaths = new SimpleListProperty<>();

    private Canvas canvas;
    private Rectangle selectionRectangle;

    private Color gerberColor = TOP_TRACE_COLOR;
    private Color toolpathColor = ENABLED_TOOLPATH_COLOR;

    private boolean flipHorizontal = false;

    public PCBPaneFX()
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PcbPane.fxml"));
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

        scaleProperty.addListener((v, oldV, newV) ->  repaint());
        toolpaths.addListener((v, oldV, newV) -> repaint());
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

    public boolean isFlipHorizontal()
    {
        return flipHorizontal;
    }

    public void setFlipHorizontal(boolean flipHorizontal)
    {
        this.flipHorizontal = flipHorizontal;
        repaint();
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
        toolpaths.forEach(t -> renderToolpath(g, t));
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
        g.scale(scaleProperty.getValue() * (flipHorizontal ? -1 : 1), -scaleProperty.getValue());
        g.translate(flipHorizontal ? -boardWidth : 0, -boardHeight);
        if (gerberPrimitives != null)
            for (GerberPrimitive primitive : gerberPrimitives)
                renderPrimitive(g, primitive);
        if (toolpaths.getValue() != null)
            for (Toolpath toolpath : toolpaths.getValue())
                renderToolpath(g, toolpath);
    }

    private void renderPrimitive(GraphicsContext g, GerberPrimitive primitive)
    {
        if (!(primitive instanceof Region) && !primitive.getAperture().isVisible())
            return;

        Color color = primitive.getPolarity() == GerberPrimitive.Polarity.DARK ? gerberColor : BACKGROUND_COLOR;
        g.setStroke(color);
        g.setFill(color);
        if (primitive instanceof LinearShape)
        {
            LinearShape linearShape = (LinearShape) primitive;
            g.setLineCap(linearShape.getAperture() instanceof CircularAperture ? StrokeLineCap.ROUND : StrokeLineCap.SQUARE);
            g.setLineWidth(linearShape.getAperture().getWidth());
            g.strokeLine(linearShape.getFrom().getX(), linearShape.getFrom().getY(),
                    linearShape.getTo().getX(), linearShape.getTo().getY());
        }
        else if (primitive instanceof CircularShape)
        {
            CircularShape circularShape = (CircularShape) primitive;
            g.setLineCap(circularShape.getAperture() instanceof CircularAperture ? StrokeLineCap.ROUND : StrokeLineCap.SQUARE);
            g.setLineWidth(circularShape.getAperture().getWidth());
            g.strokeArc(circularShape.getArc().getCenter().getX() - circularShape.getArc().getRadius(),
                    circularShape.getArc().getCenter().getY() - circularShape.getArc().getRadius(),
                    circularShape.getArc().getRadius() * 2, circularShape.getArc().getRadius() * 2,
                    -Math.toDegrees(circularShape.getArc().getStart()),
                    Math.toDegrees(circularShape.getArc().getAngle()) * (circularShape.getArc().isClockwise() ? 1 : -1),
                    ArcType.OPEN);
        }
        else if (primitive instanceof Region)
        {
            g.beginPath();
            Region region = (Region) primitive;
            InterpolatingShape firstElement = (InterpolatingShape) region.getSegments().get(0);
            g.moveTo(firstElement.getFrom().getX(), firstElement.getFrom().getY());

            for (GerberPrimitive segment : region.getSegments())
            {
                if (segment instanceof LinearShape)
                    g.lineTo(((LinearShape)segment).getTo().getX(), ((LinearShape)segment).getTo().getY());
                else if (segment instanceof CircularShape)
                {
                    Arc arc = ((CircularShape) segment).getArc();
                    g.arc(arc.getCenter().getX(), arc.getCenter().getY(),
                            arc.getRadius(), arc.getRadius(),
                            -Math.toDegrees(arc.getStart()), Math.toDegrees(arc.getAngle()) * (arc.isClockwise() ? 1 : -1));
                }
            }

            g.closePath();
            g.fill();
        }
        else if (primitive instanceof Flash)
        {
            Flash flash = (Flash) primitive;
            if (flash.getAperture() instanceof CircularAperture)
            {
                double d = ((CircularAperture)flash.getAperture()).getDiameter();
                double r = d / 2;
                g.fillOval(flash.getX() - r, flash.getY() - r, d, d);
            }
            else if (flash.getAperture() instanceof RectangularAperture)
            {
                RectangularAperture aperture = (RectangularAperture)flash.getAperture();
                g.fillRect(flash.getX() - aperture.getDimensions()[0] / 2,
                        flash.getY() - aperture.getDimensions()[1] / 2,
                        aperture.getDimensions()[0],
                        aperture.getDimensions()[1]);
            }
            else if (flash.getAperture() instanceof OctagonalAperture)
            {
                double edgeOffset = (Math.pow(2, 0.5) - 1) / 2 * ((OctagonalAperture)flash.getAperture()).getDiameter();
                double centerOffset =  0.5 * ((OctagonalAperture)flash.getAperture()).getDiameter();
                double x = flash.getX();
                double y = flash.getY();

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
            else if (flash.getAperture() instanceof OvalAperture)
            {
                OvalAperture aperture = (OvalAperture)flash.getAperture();
                double flashX = flash.getX();
                double flashY = flash.getY();
                double width = aperture.getWidth();
                double height = aperture.getHeight();
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
            else if (flash.getAperture() instanceof ApertureMacro)
            {
                ApertureMacro macro = (ApertureMacro) flash.getAperture();
                for (MacroPrimitive p : macro.getPrimitives())
                {
                    if (p instanceof MacroCenterLine)
                    {
                        MacroCenterLine centerLine = (MacroCenterLine) p;
                        Point from = centerLine.getFrom().add(flash.getPoint());
                        Point to = centerLine.getTo().add(flash.getPoint());
                        g.setLineCap(StrokeLineCap.BUTT);
                        g.setLineWidth(centerLine.getHeight());
                        g.strokeLine(from.getX(), from.getY(), to.getX(), to.getY());
                    }
                    else if (p instanceof MacroVectorLine)
                    {
                        MacroVectorLine vectorLine = (MacroVectorLine) p;
                        Point from = vectorLine.getTranslatedStart().add(flash.getPoint());
                        Point to = vectorLine.getTranslatedEnd().add(flash.getPoint());
                        g.setLineCap(StrokeLineCap.BUTT);
                        g.setLineWidth(vectorLine.getWidth());
                        g.strokeLine(from.getX(), from.getY(), to.getX(), to.getY());
                    }
                    else if (p instanceof MacroCircle)
                    {
                        MacroCircle circle = (MacroCircle) p;
                        double d = circle.getDiameter();
                        double r = d / 2;
                        Point point = circle.getCenter().add(flash.getPoint());
                        g.fillOval(point.getX() - r, point.getY() - r, d, d);

                    }
                    else if (p instanceof MacroOutline)
                    {
                        MacroOutline outline = (MacroOutline) p;
                        double x = flash.getX();
                        double y = flash.getY();

                        g.beginPath();
                        Point point = outline.getPoints().get(0);
                        g.moveTo(point.getX() + x, point.getY() + y);
                        for (int i = 1; i < outline.getTranslatedPoints().size(); i++)
                        {
                            point = outline.getPoints().get(i);
                            g.lineTo(point.getX() + x, point.getY() + y);
                        }
                        g.closePath();
                        g.fill();
                    }
                }
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
            g.setLineWidth(linearToolpath.getToolDiameter());
            g.strokeLine(linearToolpath.getCurve().getFrom().getX(), linearToolpath.getCurve().getFrom().getY(),
                    linearToolpath.getCurve().getTo().getX(), linearToolpath.getCurve().getTo().getY());
        }
        else if (toolpath instanceof CircularToolpath)
        {
            CircularToolpath circularToolpath = (CircularToolpath) toolpath;
            g.setLineCap(StrokeLineCap.ROUND);
            g.setLineWidth(circularToolpath.getToolDiameter());
            Arc arc = (Arc) circularToolpath.getCurve();
            g.strokeArc(arc.getCenter().getX() - arc.getRadius(),
                    arc.getCenter().getY() - arc.getRadius(),
                    arc.getRadius() * 2, arc.getRadius() * 2,
                    -Math.toDegrees(arc.getStart()), Math.toDegrees(arc.getAngle()) * (arc.isClockwise() ? 1 : -1), ArcType.OPEN);
        }
        else if (toolpath instanceof DrillPoint)
        {
            DrillPoint drillPoint = (DrillPoint) toolpath;
            g.setFill(color);
            g.fillOval(drillPoint.getPoint().getX() - drillPoint.getToolDiameter() / 2,
                    drillPoint.getPoint().getY() - drillPoint.getToolDiameter() / 2,
                    drillPoint.getToolDiameter(), drillPoint.getToolDiameter());
        }
    }

    public void setSelection(double x, double y, double width, double height)
    {
        if (selectionRectangle != null)
            getChildren().remove(selectionRectangle);
        selectionRectangle = new Rectangle();
        selectionRectangle.setStrokeWidth(0.5);
        selectionRectangle.getStyleClass().add("pcb-selection-rect");
        // It seems that in this case transforms get converted to int somewhere down the road. So can't use them here
        selectionRectangle.setX(x * scaleProperty().getValue());
        selectionRectangle.setY((-y - height + boardHeight) * scaleProperty().getValue());
        selectionRectangle.setWidth(width * scaleProperty().getValue());
        selectionRectangle.setHeight(height * scaleProperty().getValue());
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
