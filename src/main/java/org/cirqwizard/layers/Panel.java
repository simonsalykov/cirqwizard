package org.cirqwizard.layers;

import org.cirqwizard.fx.PCBSize;

import java.util.ArrayList;
import java.util.List;

public class Panel
{
    private PCBSize size;
    private List<PanelBoard> boards = new ArrayList<>();

    public List<PanelBoard> getBoards()
    {
        return boards;
    }

    public void addBoard(PanelBoard board)
    {
        boards.add(board);
    }
}
