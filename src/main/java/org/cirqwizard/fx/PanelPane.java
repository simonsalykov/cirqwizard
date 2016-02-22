package org.cirqwizard.fx;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import org.cirqwizard.settings.ApplicationConstants;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PanelPane extends ScrollPane
{
    private static final Color BACKGROUND_COLOR = Color.decode("#ddfbdd");
    private static final Color PANEL_CONTOUR = Color.black;
    private static final Color PIN_COLOR = Color.black;

    private static final int RESOLUTION = 10;
    private static final int PADDING = 5;
    private static final int CONTOUR_WIDTH = 1;
    private static final int PIN_DIAMETER = 3;

    private PCBSize size;
    private ImageView image = new ImageView();

    public PanelPane()
    {
        setContent(image);
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

    public void render()
    {
        if (size == null)
            return;

        BufferedImage bufferedImage = new BufferedImage(convertResolution(size.getWidth()) + PADDING * 2 * RESOLUTION,
                convertResolution(size.getHeight()) + PADDING * 2 * RESOLUTION, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedImage.createGraphics();
        g.setBackground(BACKGROUND_COLOR);
        g.clearRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
        renderContour(g);

        renderPin(g, 5, 5);
        renderPin(g, size.getWidth() / ApplicationConstants.RESOLUTION - 5, 5);
        renderPin(g, 5, size.getHeight() / ApplicationConstants.RESOLUTION - 5);
        renderPin(g, size.getWidth() / ApplicationConstants.RESOLUTION - 5, size.getHeight() / ApplicationConstants.RESOLUTION - 5);

        WritableImage img = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        SwingFXUtils.toFXImage(bufferedImage, img);
        image.setImage(img);
        setPrefSize(bufferedImage.getWidth(), bufferedImage.getHeight());
    }

    private int convertResolution(int dimension)
    {
        return dimension / ApplicationConstants.RESOLUTION * RESOLUTION;
    }

    private void renderContour(Graphics2D g)
    {
        g.setColor(PANEL_CONTOUR);
        g.setStroke(new BasicStroke(convertResolution(CONTOUR_WIDTH)));
        g.drawRect(PADDING * RESOLUTION, PADDING * RESOLUTION, convertResolution(size.getWidth()),
                convertResolution(size.getHeight()));
    }

    private void renderPin(Graphics2D g, int x, int y)
    {
        g.setColor(PIN_COLOR);
        g.fillArc(PADDING * RESOLUTION + x * RESOLUTION - PIN_DIAMETER * RESOLUTION / 2,
                PADDING * RESOLUTION + y * RESOLUTION - PIN_DIAMETER * RESOLUTION / 2,
            PIN_DIAMETER * RESOLUTION / 2, PIN_DIAMETER * RESOLUTION / 2, 0, 360);
    }

}
