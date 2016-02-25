package org.cirqwizard.layers;

import org.cirqwizard.fx.PCBSize;
import org.cirqwizard.generation.toolpath.Toolpath;
import org.cirqwizard.geom.Point;
import org.cirqwizard.logging.LoggerFactory;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Transient;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Root
public class Panel
{
    @Element (required = false)
    private PCBSize size;
    @ElementList (required = false)
    private List<PanelBoard> boards = new ArrayList<>();
    @Transient
    private HashMap<Board.LayerType, List<Toolpath>> toolpaths = new HashMap<>();

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

    public List<Toolpath> getToolspaths(Board.LayerType layerType)
    {
        return toolpaths.get(layerType);
    }

    public void setToolpaths(Board.LayerType layerType, List<Toolpath> toolpaths)
    {
        this.toolpaths.put(layerType, toolpaths);
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

    public List<? extends LayerElement> getCombinedElements(Board.LayerType layerType)
    {
        return boards.stream().map(b ->
        {
            Layer layer = b.getBoard().getLayer(layerType);
            if (layer == null)
                return null;
            Point offset = new Point(b.getX(), b.getY());
            return layer.getElements().stream().map(e ->
            {
                try
                {
                    LayerElement clone = (LayerElement) e.clone();
                    clone.move(offset);
                    return clone;
                }
                catch (CloneNotSupportedException e1) {
                    e1.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
        }).
                filter(l -> l != null).
                flatMap(Collection::stream).collect(Collectors.toList());
    }
}
