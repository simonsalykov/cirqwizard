package org.cirqwizard.fx;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.layers.Board;
import org.cirqwizard.layers.Panel;
import org.cirqwizard.layers.PanelBoard;

public class PanelPane extends Region
{
    public static final Color BACKGROUND_COLOR = Color.web("#ddfbdd");
    public static final Color PANEL_CONTOUR = Color.BLACK;
    public static final Color PIN_COLOR = Color.BLACK;
    public static final Color TOP_TRACE_COLOR = Color.color(1, 0, 0, 0.8);
    public static final Color BOTTOM_TRACE_COLOR = Color.color(0, 0, 1, 0.8);
    public static final Color DRILL_POINT_COLOR = Color.BLACK;
    public static final Color CONTOUR_COLOR = Color.MAGENTA;

    private static final int DEFAULT_ZOOM = 100;
    private static final int ZOOM_INCREMENT = 10;
    private static final int PADDING = 5000;
    private static final int CONTOUR_WIDTH = 100;
    private static final int PIN_DIAMETER = 3000;
    private static final int PIN_INSET = 5000;

    private org.cirqwizard.layers.Panel panel;
    private int zoom = DEFAULT_ZOOM;
    private int width;
    private int height;
    private boolean rendered = false;

    private Rectangle selectionRectangle;
    private PanelBoard selectedBoard;
    private Point2D mouseClickPoint;
    private Point initialBoardLocation;

    public Panel getPanel()
    {
        return panel;
    }

    public void setPanel(Panel panel)
    {
        this.panel = panel;
    }

    public PanelPane()
    {
        addEventFilter(MouseEvent.ANY, event ->
        {
            if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED))
            {
                mouseClickPoint = new Point2D(event.getX(), event.getY());
                selectBoard(getBoardForCoordinates(event.getX(), event.getY()));
                if (selectedBoard != null)
                    initialBoardLocation = new Point(selectedBoard.getX(), selectedBoard.getY());
                event.consume();
            }
            else if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED))
            {
                Point2D delta = new Point2D(event.getX() - mouseClickPoint.getX(), -(event.getY() - mouseClickPoint.getY()));
                if (selectedBoard != null)
                {
                    selectedBoard.setX((int)(initialBoardLocation.getX() + delta.getX() * zoom));
                    selectedBoard.setY((int)(initialBoardLocation.getY() + delta.getY() * zoom));
                    render();
                }
            }
        });
    }

    public void render()
    {
        if (panel == null || panel.getSize() == null)
            return;

        width = panel.getSize().getWidth() + PADDING * 2;
        height = panel.getSize().getHeight() + PADDING * 2;
        Canvas canvas = new Canvas(width / zoom, height / zoom);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(BACKGROUND_COLOR);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        double scale = 1.0 / zoom;
        g.scale(scale, -scale);
        g.translate(0, -canvas.getHeight() * zoom);
        renderContour(g);

        renderPin(g, PIN_INSET, PIN_INSET);
        renderPin(g, panel.getSize().getWidth() - PIN_INSET, PIN_INSET);
        renderPin(g, PIN_INSET, panel.getSize().getHeight() - PIN_INSET);
        renderPin(g, panel.getSize().getWidth() - PIN_INSET, panel.getSize().getHeight() - PIN_INSET);

        g.translate(PADDING, PADDING);

        renderLayer(g, Board.LayerType.BOTTOM, BOTTOM_TRACE_COLOR);
        renderLayer(g, Board.LayerType.TOP, TOP_TRACE_COLOR);
        renderLayer(g, Board.LayerType.MILLING, CONTOUR_COLOR);

        getChildren().clear();
        getChildren().add(canvas);
        rendered = true;
    }

    private void renderLayer(GraphicsContext g, Board.LayerType layerType, Color color)
    {
        g.setStroke(color);
        g.setFill(color);
        panel.getBoards().stream().
                forEach(board ->
                {
                    g.translate(board.getX(), board.getY());
                    board.getBoard().getLayer(layerType).getElements().stream().
                            forEach(e -> ((GerberPrimitive)e).render(g));
                    g.translate(-board.getX(), -board.getY());
                });
    }

    private void renderContour(GraphicsContext g)
    {
        g.setStroke(PANEL_CONTOUR);
        g.setLineWidth(CONTOUR_WIDTH);
        g.strokeRect(PADDING, PADDING, panel.getSize().getWidth(), panel.getSize().getHeight());
    }

    private void renderPin(GraphicsContext g, int x, int y)
    {
        g.setFill(PIN_COLOR);
        g.fillArc(PADDING + x - PIN_DIAMETER / 2, PADDING + y - PIN_DIAMETER / 2,
            PIN_DIAMETER / 2, PIN_DIAMETER / 2, 0, 360, ArcType.ROUND);
    }

    public void zoomIn()
    {
        zoom -= ZOOM_INCREMENT;
        render();
    }

    public void zoomOut()
    {
        zoom += ZOOM_INCREMENT;
        render();
    }

    public void zoomToFit(double width, double height, boolean force)
    {
        if (rendered && !force)
            return;
        if (panel == null || panel.getSize() == null || width == 0 || height == 0)
            return;
        double xScale = (panel.getSize().getWidth() + PADDING * 2) / width;
        double yScale = (panel.getSize().getHeight() + PADDING * 2) / height;
        zoom = (int) Math.max(xScale, yScale);
        render();
    }

    private PanelBoard getBoardForCoordinates(double x, double y)
    {
        return panel.getBoards().stream().filter(b -> getBoardRectangle(b).contains(x, y)).findFirst().orElse(null);
    }

    private Rectangle2D getBoardRectangle(PanelBoard board)
    {
        return new Rectangle2D((board.getX() + PADDING) / zoom,
            (-board.getY() + - board.getBoard().getHeight() + height - PADDING) / zoom,
            board.getBoard().getWidth() / zoom, board.getBoard().getHeight() / zoom);
    }

    public void selectBoard(PanelBoard board)
    {
        this.selectedBoard = board;
        if (selectionRectangle != null)
            getChildren().remove(selectionRectangle);
        if (board != null)
        {
            Rectangle2D r = getBoardRectangle(board);
            selectionRectangle = new Rectangle(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight());
            selectionRectangle.setStrokeWidth(10);
            selectionRectangle.getStyleClass().add("board-selection-rect");
            getChildren().add(selectionRectangle);
        }
    }

}
