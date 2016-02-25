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
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.cirqwizard.generation.toolpath.Toolpath;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.layers.LayerElement;

import java.io.IOException;
import java.util.List;


public class PCBPane extends javafx.scene.layout.Region
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

    private java.util.List<? extends LayerElement> gerberPrimitives;
    private Property<ObservableList<Toolpath>> toolpaths = new SimpleListProperty<>();

    private Canvas canvas;
    private Rectangle selectionRectangle;

    private Color gerberColor = TOP_TRACE_COLOR;
    private Color toolpathColor = ENABLED_TOOLPATH_COLOR;

    private boolean flipHorizontal = false;

    public PCBPane()
    {
        scaleProperty.addListener((v, oldV, newV) ->  repaint());
        toolpaths.addListener((v, oldV, newV) -> repaint());
    }

    public Property<ObservableList<Toolpath>> toolpathsProperty()
    {
        return toolpaths;
    }

    public void setGerberPrimitives(List<? extends LayerElement> gerberPrimitives)
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
            gerberPrimitives.forEach(p -> renderPrimitive(g, p));
        if (toolpaths.getValue() != null)
            toolpaths.getValue().forEach(t -> renderToolpath(g, t));
    }

    private void renderPrimitive(GraphicsContext g, LayerElement element)
    {
        if (!element.isVisible())
            return;

        Color color = gerberColor;
        if ((element instanceof GerberPrimitive) && ((GerberPrimitive)element).getPolarity() == GerberPrimitive.Polarity.CLEAR)
            color = BACKGROUND_COLOR;
        g.setStroke(color);
        g.setFill(color);
        element.render(g);
    }

    private void renderToolpath(GraphicsContext g, Toolpath toolpath)
    {
        Color color = toolpath.isEnabled() ? toolpathColor : DISABLED_TOOLPATH_COLOR;
        if (toolpath.isSelected())
            color = SELECTED_TOOLPATH_COLOR;
        g.setStroke(color);
        g.setFill(color);
        toolpath.render(g);
    }

    public void setSelection(Point2D point, double width, double height)
    {
        if (selectionRectangle != null)
            getChildren().remove(selectionRectangle);
        selectionRectangle = new Rectangle();
        selectionRectangle.setStrokeWidth(0.5);
        selectionRectangle.getStyleClass().add("pcb-selection-rect");
        // It seems that in this case transforms get converted to int somewhere down the road. So can't use them here
        selectionRectangle.setX(point.getX() * scaleProperty().getValue());
        selectionRectangle.setY((-point.getY() - height + boardHeight) * scaleProperty().getValue());
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
