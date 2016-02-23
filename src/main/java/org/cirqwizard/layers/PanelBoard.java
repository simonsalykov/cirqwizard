package org.cirqwizard.layers;

/**
 * Created by simon on 22.02.16.
 */
public class PanelBoard
{
    private String filename;
    private int x;
    private int y;
    private Board board;

    public PanelBoard(String filename, int x, int y, Board board)
    {
        this.filename = filename;
        this.x = x;
        this.y = y;
        this.board = board;
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

    public void setBoard(Board board)
    {
        this.board = board;
    }
}
