package org.cirqwizard.fx;

import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.layers.Board;
import org.cirqwizard.layers.Panel;

public class PanelPane extends ScrollPane
{
    public static final Color BACKGROUND_COLOR = Color.web("#ddfbdd");
    public static final Color PANEL_CONTOUR = Color.BLACK;
    public static final Color PIN_COLOR = Color.BLACK;

    private static final int DEFAULT_ZOOM = 100;
    private static final int PADDING = 5000;
    private static final int CONTOUR_WIDTH = 100;
    private static final int PIN_DIAMETER = 3000;
    private static final int PIN_INSET = 5000;

    private PCBSize size;
    private org.cirqwizard.layers.Panel panel;
    private ImageView image = new ImageView();
    private Group group = new Group();

    public PanelPane()
    {
        setContent(group);
        setPannable(true);
    }

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

        Canvas canvas = new Canvas((size.getWidth() + PADDING * 2) / DEFAULT_ZOOM, (size.getHeight() + PADDING * 2) / DEFAULT_ZOOM);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(BACKGROUND_COLOR);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        double scale = 1.0 / DEFAULT_ZOOM;
        g.scale(scale, -scale);
        g.translate(0, -canvas.getHeight() * DEFAULT_ZOOM);
        renderContour(g);

        renderPin(g, PIN_INSET, PIN_INSET);
        renderPin(g, size.getWidth() - PIN_INSET, PIN_INSET);
        renderPin(g, PIN_INSET, size.getHeight() - PIN_INSET);
        renderPin(g, size.getWidth() - PIN_INSET, size.getHeight() - PIN_INSET);

        g.setStroke(Color.RED);
        g.setFill(Color.RED);
        if (panel != null)
            panel.getBoards().stream().
                    forEach(board ->
                    {
                        g.translate(board.getX(), board.getY());
                        board.getBoard().getLayer(Board.LayerType.TOP).getElements().stream().
                                forEach(e -> ((GerberPrimitive)e).render(g));
                        g.translate(-board.getX(), -board.getY());
                    });
        group.getChildren().clear();
        group.getChildren().add(canvas);
        setPrefSize(canvas.getWidth(), canvas.getHeight());
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

}
