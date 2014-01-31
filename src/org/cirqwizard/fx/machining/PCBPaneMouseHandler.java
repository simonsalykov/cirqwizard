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

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import org.cirqwizard.fx.PCBPaneFX;
import org.cirqwizard.fx.services.ToolpathGenerationService;
import org.cirqwizard.toolpath.*;

import java.util.ArrayList;


public class PCBPaneMouseHandler implements EventHandler<MouseEvent>
{
    private PCBPaneFX pcbPane;
    private Point2D clickPoint;
    private ToolpathGenerationService service;

    public PCBPaneMouseHandler(PCBPaneFX pcbPane)
    {
        this.pcbPane = pcbPane;
    }

    @Override
    public void handle(MouseEvent event)
    {
        if (!event.isShortcutDown())
        {
            if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED))
            {
                clickPoint = toPCBCoordinates(new Point2D(event.getX(), event.getY()));
                pcbPane.setSelection(clickPoint.getX(), clickPoint.getY(), 0, 0);
                ArrayList<Toolpath> changedToolpaths = new ArrayList<>();
                for (Toolpath toolpath : service.getValue())
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
                Point2D eventPoint = toPCBCoordinates(new Point2D(event.getX(), event.getY()));
                ArrayList<Toolpath> changedToolpaths = new ArrayList<>();
                for (Toolpath toolpath : service.getValue())
                {
                    Shape shape = createShapeForToolpath((CuttingToolpath) toolpath);
                    shape.setPickOnBounds(false);
                    boolean selected = shape.intersects(Math.min(eventPoint.getX(), clickPoint.getX()), Math.min(eventPoint.getY(), clickPoint.getY()),
                            Math.abs(eventPoint.getX() - clickPoint.getX()), Math.abs(eventPoint.getY() - clickPoint.getY()));
                    if (selected)
                        System.out.println("^^ " + ((CuttingToolpath) toolpath).getCurve());
                    if (toolpath.isSelected() != selected)
                        changedToolpaths.add(toolpath);
                    toolpath.setSelected(selected);
                }
                pcbPane.repaint(changedToolpaths);
                pcbPane.setSelection(Math.min(eventPoint.getX(), clickPoint.getX()), Math.min(eventPoint.getY(), clickPoint.getY()),
                        Math.abs(eventPoint.getX() - clickPoint.getX()), Math.abs(eventPoint.getY() - clickPoint.getY()));
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

    public void setService(ToolpathGenerationService service)
    {
        this.service = service;
    }

    public Point2D toPCBCoordinates(Point2D point)
    {
        return new Point2D(point.getX() / pcbPane.scaleProperty().getValue(), (point.getY() - pcbPane.getHeight()) / -pcbPane.scaleProperty().getValue());
    }
}
