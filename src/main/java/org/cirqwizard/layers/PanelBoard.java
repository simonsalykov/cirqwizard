package org.cirqwizard.layers;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Transient;

import java.io.IOException;

public class PanelBoard
{
    @Element
    private String filename;
    @Element
    private int x;
    @Element
    private int y;
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

    public Board getBoard()
    {
        return board;
    }

    public void loadBoard() throws IOException
    {
        board = new Board();
        board.loadLayers(filename);
    }
}
