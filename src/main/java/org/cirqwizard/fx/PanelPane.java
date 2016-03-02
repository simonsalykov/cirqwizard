package org.cirqwizard.fx;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.transform.Affine;
import org.cirqwizard.geom.Point;
import org.cirqwizard.layers.Board;
import org.cirqwizard.layers.Layer;
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
    public static final Color SELECTED_BOARD_BACKGROUND_COLOR = Color.web("#eeffee");
    public static final Color CONTOUR_COLOR = Color.MAGENTA;

    private static final int ZOOM_INCREMENT = 15;
    private static final int PADDING = 5000;
    private static final int CONTOUR_WIDTH = 100;
    private static final int PIN_DIAMETER = 3000;

    private org.cirqwizard.layers.Panel panel;
    private int zoom;
    private int width;
    private int height;
    private boolean rendered = false;

    private SimpleObjectProperty<PanelBoard> selectedBoard = new SimpleObjectProperty<>();
    private Point2D mouseClickPoint;
    private Point initialBoardLocation;
    private BoardDragListener boardDragListener;

    private Canvas canvas;

    public Panel getPanel()
    {
        return panel;
    }

    public void setPanel(Panel panel)
    {
        this.panel = panel;
        selectedBoard.setValue(null);
    }

    public PanelPane()
    {
        canvas = new Canvas();
        getChildren().add(canvas);
        addEventFilter(MouseEvent.ANY, event ->
        {
            if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED))
            {
                mouseClickPoint = new Point2D(event.getX(), event.getY());
                selectBoard(getBoardForCoordinates(event.getX(), event.getY()));
                if (selectedBoard.getValue() != null)
                    initialBoardLocation = new Point(selectedBoard.getValue().getX(), selectedBoard.getValue().getY());
            }
            else if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED))
            {
                Point2D delta = new Point2D(event.getX() - mouseClickPoint.getX(), -(event.getY() - mouseClickPoint.getY()));
                if (selectedBoard.getValue() != null)
                {
                    selectedBoard.getValue().setX((int)(initialBoardLocation.getX() + delta.getX() * zoom));
                    selectedBoard.getValue().setY((int)(initialBoardLocation.getY() + delta.getY() * zoom));
                    render();
                }
            }
            else if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED))
            {
                if (selectedBoard.getValue() != null)
                    boardDragListener.boardDragged();
            }
        });
    }

    public void setBoardDragListener(BoardDragListener boardDragListener)
    {
        this.boardDragListener = boardDragListener;
    }

    public void render()
    {
        if (panel == null || panel.getSize() == null)
            return;

        width = panel.getSize().getWidth() + PADDING * 2;
        height = panel.getSize().getHeight() + PADDING * 2;
        canvas.setWidth(width / zoom);
        canvas.setHeight(height / zoom);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setTransform(new Affine());
        g.setFill(BACKGROUND_COLOR);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        double scale = 1.0 / zoom;
        g.scale(scale, -scale);
        g.translate(0, -canvas.getHeight() * zoom);
        renderContour(g);

        for (Point p : panel.getPinLocations())
            renderPin(g, p.getX(), p.getY());
        g.translate(PADDING, PADDING);

        PanelBoard selectedBoardValue = selectedBoard.getValue();
        if (selectedBoardValue != null)
        {
            g.setFill(SELECTED_BOARD_BACKGROUND_COLOR);
            g.fillRect(selectedBoardValue.getX(), selectedBoardValue.getY(), selectedBoardValue.getBoard().getWidth(),
                    selectedBoardValue.getBoard().getHeight());
        }

        renderLayer(g, Board.LayerType.BOTTOM, BOTTOM_TRACE_COLOR);
        renderLayer(g, Board.LayerType.TOP, TOP_TRACE_COLOR);
        renderLayer(g, Board.LayerType.MILLING, CONTOUR_COLOR);
        rendered = true;
    }

    private void renderLayer(GraphicsContext g, Board.LayerType layerType, Color color)
    {
        panel.getBoards().stream().
                forEach(board ->
                {
                    g.translate(board.getX(), board.getY());
                    g.setStroke(color);
                    g.setFill(color);
                    Layer layer = board.getBoard().getLayer(layerType);
                    if (layer != null)
                        layer.getElements().stream().forEach(e -> e.render(g));
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
        this.selectedBoard.setValue(board);
        render();
    }

    public PanelBoard getSelectedBoard()
    {
        return selectedBoard.get();
    }

    public SimpleObjectProperty<PanelBoard> selectedBoardProperty()
    {
        return selectedBoard;
    }
}
