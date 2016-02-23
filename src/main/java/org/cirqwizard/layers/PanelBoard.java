package org.cirqwizard.layers;

/**
 * Created by simon on 22.02.16.
 */
public class PanelBoard
{
    private int x;
    private int y;
    private Board board;

    public PanelBoard(int x, int y, Board board)
    {
        this.x = x;
        this.y = y;
        this.board = board;
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
