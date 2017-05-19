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

package org.cirqwizard.fx.machining;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import org.cirqwizard.fx.PCBPane;
import org.cirqwizard.generation.toolpath.*;

import java.util.ArrayList;
import java.util.List;


public class PCBPaneMouseHandler implements EventHandler<MouseEvent>
{
    private PCBPane pcbPane;
    private Point2D startPoint;
    private Point2D startPointNonFlipped;
    private SimpleObjectProperty<List<Toolpath>> toolpaths = new SimpleObjectProperty<>();

    public PCBPaneMouseHandler(PCBPane pcbPane)
    {
        this.pcbPane = pcbPane;
    }

    @Override
    public void handle(MouseEvent event)
    {
        if (!event.isShortcutDown())
        {
            Point2D ePoint = new Point2D(event.getX(), event.getY());
            if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED))
            {
                startPoint = toPCBCoordinates(ePoint, pcbPane.isFlipHorizontal());
                startPointNonFlipped = toPCBCoordinates(ePoint, false);
                pcbPane.setSelection(startPointNonFlipped, 0, 0);
                ArrayList<Toolpath> changedToolpaths = new ArrayList<>();
                for (Toolpath toolpath :toolpaths.get())
                {
                    if (toolpath.isSelected())
                        changedToolpaths.add(toolpath);
                    toolpath.setSelected(false);
                }
                pcbPane.repaint(changedToolpaths);
                event.consume();
            }
            else if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED))
            {
                if (toolpaths == null)
                    return;
                Point2D dragPoint = toPCBCoordinates(ePoint, pcbPane.isFlipHorizontal());
                Point2D dragPointNonFlipped = toPCBCoordinates(ePoint, false);
                ArrayList<Toolpath> changedToolpaths = new ArrayList<>();
                for (Toolpath toolpath : toolpaths.get())
                {
                    Shape shape = createShapeForToolpath((CuttingToolpath) toolpath);
                    shape.setPickOnBounds(false);
                    boolean selected = shape.intersects(Math.min(dragPoint.getX(), startPoint.getX()), Math.min(dragPoint.getY(), startPoint.getY()),
                            Math.abs(dragPoint.getX() - startPoint.getX()), Math.abs(dragPoint.getY() - startPoint.getY()));
                    if (toolpath.isSelected() != selected)
                        changedToolpaths.add(toolpath);
                    toolpath.setSelected(selected);
                }
                pcbPane.repaint(changedToolpaths);
                pcbPane.setSelection(new Point2D(Math.min(dragPointNonFlipped.getX(), startPointNonFlipped.getX()),
                                Math.min(dragPointNonFlipped.getY(), startPointNonFlipped.getY())),
                        Math.abs(dragPointNonFlipped.getX() - startPointNonFlipped.getX()),
                        Math.abs(dragPointNonFlipped.getY() - startPointNonFlipped.getY()));
                event.consume();
            }
        }

        if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED))
            pcbPane.clearSelection();

    }

    private Shape createShapeForToolpath(CuttingToolpath toolpath)
    {
        Shape shape = null;
        if (toolpath instanceof LinearToolpath)
        {
            LinearToolpath t = (LinearToolpath) toolpath;
            shape = new Line(t.getCurve().getFrom().getX(), t.getCurve().getFrom().getY(),
                    t.getCurve().getTo().getX(), t.getCurve().getTo().getY());
        }
        else if (toolpath instanceof CircularToolpath)
        {
            org.cirqwizard.geom.Arc t = (org.cirqwizard.geom.Arc)toolpath.getCurve();
            shape = new Arc(t.getCenter().getX(), t.getCenter().getY(),
                    t.getRadius(), t.getRadius(),
                    -Math.toDegrees(t.getStart()), Math.toDegrees(t.getAngle()));
        }
        else if (toolpath instanceof DrillPoint)
        {
            DrillPoint drillPoint = (DrillPoint) toolpath;
            shape = new Arc(drillPoint.getPoint().getX(), drillPoint.getPoint().getY(),
                    drillPoint.getToolDiameter() / 2, drillPoint.getToolDiameter() / 2, 0, 360);
        }
        if (shape != null)
        {
            shape.setStrokeLineCap(StrokeLineCap.ROUND);
            shape.setStrokeWidth(toolpath.getToolDiameter());
        }

        return shape;
    }

    public List<Toolpath> getToolpaths()
    {
        return toolpaths.get();
    }

    public SimpleObjectProperty<List<Toolpath>> toolpathsProperty()
    {
        return toolpaths;
    }

    public Point2D toPCBCoordinates(Point2D point, boolean flipX)
    {
        return new Point2D((point.getX() - (flipX ? pcbPane.getWidth() : 0)) / pcbPane.scaleProperty().getValue() * (flipX ? -1 : 1),
                (point.getY() - pcbPane.getHeight()) / -pcbPane.scaleProperty().getValue());
    }
}
