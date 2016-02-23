package org.cirqwizard.layers;

import org.cirqwizard.fx.PCBSize;
import org.cirqwizard.logging.LoggerFactory;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Root
public class Panel
{
    @Element (required = false)
    private PCBSize size;
    @ElementList (required = false)
    private List<PanelBoard> boards = new ArrayList<>();

    public PCBSize getSize()
    {
        return size;
    }

    public void setSize(PCBSize size)
    {
        this.size = size;
    }

    public List<PanelBoard> getBoards()
    {
        return boards;
    }

    public void addBoard(PanelBoard board)
    {
        boards.add(board);
    }

    private void loadBoards()
    {
        boards.stream().forEach(b ->
        {
            try
            {
                b.loadBoard();
            }
            catch (IOException e)
            {
                LoggerFactory.logException("Could not load layers", e);
            }
        });
    }

    public void save(File file)
    {
        try
        {
            new Persister().write(this, file);
        }
        catch (Exception e)
        {
            LoggerFactory.logException("Could not save panel file", e);
        }
    }

    public static Panel loadFromFile(File file)
    {
        try
        {
            Panel panel = new Persister().read(Panel.class, file);
            panel.loadBoards();
            return panel;
        }
        catch (Exception e)
        {
            LoggerFactory.logException("Could not read panel file", e);
        }
        return null;
    }
}
