package org.cirqwizard.fx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.layers.Board;
import org.cirqwizard.layers.Panel;
import org.cirqwizard.layers.PanelBoard;

public class PanelPane extends Region
{
    public static final Color BACKGROUND_COLOR = Color.web("#ddfbdd");
    public static final Color PANEL_CONTOUR = Color.BLACK;
    public static final Color PIN_COLOR = Color.BLACK;

    private static final int DEFAULT_ZOOM = 100;
    private static final int ZOOM_INCREMENT = 10;
    private static final int PADDING = 5000;
    private static final int CONTOUR_WIDTH = 100;
    private static final int PIN_DIAMETER = 3000;
    private static final int PIN_INSET = 5000;

    private PCBSize size;
    private org.cirqwizard.layers.Panel panel;
    private int zoom = DEFAULT_ZOOM;
    private int width;
    private int height;

    private Rectangle selectionRectangle;

    public PCBSize getSize()
    {
        return size;
    }

    public void setSize(PCBSize size)
    {
        this.size = size;
        render();
    }

    public Panel getPanel()
    {
        return panel;
    }

    public void setPanel(Panel panel)
    {
        this.panel = panel;
    }

    public void render()
    {
        if (size == null)
            return;

        width = size.getWidth() + PADDING * 2;
        height = size.getHeight() + PADDING * 2;
        Canvas canvas = new Canvas(width / zoom, height / zoom);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(BACKGROUND_COLOR);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        double scale = 1.0 / zoom;
        g.scale(scale, -scale);
        g.translate(0, -canvas.getHeight() * zoom);
        renderContour(g);

        renderPin(g, PIN_INSET, PIN_INSET);
        renderPin(g, size.getWidth() - PIN_INSET, PIN_INSET);
        renderPin(g, PIN_INSET, size.getHeight() - PIN_INSET);
        renderPin(g, size.getWidth() - PIN_INSET, size.getHeight() - PIN_INSET);

        g.setStroke(Color.RED);
        g.setFill(Color.RED);
        g.translate(PADDING, PADDING);
        if (panel != null)
            panel.getBoards().stream().
                    forEach(board ->
                    {
                        g.translate(board.getX(), board.getY());
                        board.getBoard().getLayer(Board.LayerType.TOP).getElements().stream().
                                forEach(e -> ((GerberPrimitive)e).render(g));
                        g.translate(-board.getX(), -board.getY());
                    });
        getChildren().clear();
        getChildren().add(canvas);
    }

    private void renderContour(GraphicsContext g)
    {
        g.setStroke(PANEL_CONTOUR);
        g.setLineWidth(CONTOUR_WIDTH);
        g.strokeRect(PADDING, PADDING, size.getWidth(), size.getHeight());
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

    public void zoomToFit(double width, double height)
    {
        double xScale = this.width / width;
        double yScale = this.height / height;
        zoom = (int) Math.max(xScale, yScale);
        render();
    }

    public void selectBoard(PanelBoard board)
    {
        if (selectionRectangle != null)
            getChildren().remove(selectionRectangle);
        if (board != null)
        {
            selectionRectangle = new Rectangle((board.getX() + PADDING) / zoom,
                    (-board.getY() + - board.getBoard().getHeight() + height - PADDING) / zoom,
                    board.getBoard().getWidth() / zoom, board.getBoard().getHeight() / zoom);
            selectionRectangle.setStrokeWidth(10);
            selectionRectangle.getStyleClass().add("board-selection-rect");
            getChildren().add(selectionRectangle);
        }
    }

}
