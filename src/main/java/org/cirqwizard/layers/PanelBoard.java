package org.cirqwizard.layers;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Transient;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class PanelBoard
{
    @Element
    private String filename;
    @Element
    private int x;
    @Element
    private int y;
    @Element
    private int angle;
    @Element(required = false)
    private Date topLayerTimestamp;
    @Element(required = false)
    private Date bottomLayerTimestamp;
    @Transient
    private Board board;

    public PanelBoard()
    {
    }

    public PanelBoard(String filename, int x, int y)
    {
        this.filename = filename;
        this.x = x;
        this.y = y;
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public int getAngle()
    {
        return angle;
    }

    public void setAngle(int angle)
    {
        this.angle = angle;
    }

    public void resetCacheTimestamps()
    {
        topLayerTimestamp = null;
        bottomLayerTimestamp = null;
    }

    public void updateCacheTimestamps()
    {
        File topLayer = new File(filename + ".cmp");
        topLayerTimestamp = topLayer.exists() ? new Date(topLayer.lastModified()) : null;
        File bottomLayer = new File(filename + ".sol");
        bottomLayerTimestamp = bottomLayer.exists() ? new Date(bottomLayer.lastModified()) : null;
    }

    private boolean validateTimestamp(Date timestamp, File file)
    {
        if (timestamp == null && !file.exists())
            return true;
        if (timestamp == null && file.exists())
            return false;
        if (!file.exists())
            return false;
        return timestamp.compareTo(new Date(file.lastModified())) >= 0;
    }

    public boolean validateCacheTimestamps()
    {
        if (!validateTimestamp(topLayerTimestamp, new File(filename + ".cmp")))
            return false;
        return validateTimestamp(bottomLayerTimestamp, new File(filename + ".sol"));
    }

    public void rotate(boolean clockwise)
    {
        angle += clockwise ? 90 : -90;
        angle %= 360;
        board.rotate(clockwise);
    }

    public Board getBoard()
    {
        return board;
    }

    public void loadBoard() throws IOException
    {
        board = new Board();
        board.loadLayers(filename);
        if (!board.hasLayers())
            return;
        int rotations = angle / 90;
        while (rotations != 0)
        {
            board.rotate(angle > 0);
            rotations += rotations > 0 ? -1 : 1;
        }
    }

    public void centerInPanel(Panel panel)
    {
        x = (panel.getSize().getWidth() - board.getWidth()) / 2;
        y = (panel.getSize().getHeight() - board.getHeight()) / 2;
    }

}
