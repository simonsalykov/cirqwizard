/*
This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 3 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.cirqwizard.layers;

import org.cirqwizard.fx.PCBSize;
import org.cirqwizard.generation.toolpath.Toolpath;
import org.cirqwizard.geom.Point;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.ApplicationConstants;
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

    private void loadBoards(File panelFile)
    {
        String basename = panelFile.getAbsolutePath().substring(0,
                panelFile.getAbsolutePath().lastIndexOf(File.separatorChar));
        boards.stream().forEach(b ->
        {
            try
            {
                b.loadBoard();
                if (!b.getBoard().hasLayers())
                {
                    String filename = b.getFilename().substring(b.getFilename().lastIndexOf(File.separatorChar) + 1,
                            b.getFilename().length());
                    b.setFilename(basename + File.separatorChar + filename);
                    b.loadBoard();
                    if (b.getBoard().hasLayers())
                        save(panelFile);
                }
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
            panel.loadBoards(file);
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

    public void updateCacheTimestamps()
    {
        boards.stream().forEach(PanelBoard::updateCacheTimestamps);
    }

    public boolean isCacheValid()
    {
        return boards.stream().map(PanelBoard::validateCacheTimestamps).noneMatch(b -> !b);
    }

    public void resetCacheTimestamps()
    {
        boards.stream().forEach(PanelBoard::resetCacheTimestamps);
    }

    public Point[] getPinLocations()
    {
        return new Point[] {
                new Point(ApplicationConstants.REGISTRATION_PINS_INSET, ApplicationConstants.REGISTRATION_PINS_INSET),
                new Point(size.getWidth() - ApplicationConstants.REGISTRATION_PINS_INSET,
                        ApplicationConstants.REGISTRATION_PINS_INSET),
                new Point(ApplicationConstants.REGISTRATION_PINS_INSET,
                        size.getHeight() - ApplicationConstants.REGISTRATION_PINS_INSET),
                new Point(size.getWidth() - ApplicationConstants.REGISTRATION_PINS_INSET,
                        size.getHeight() - ApplicationConstants.getRegistrationPinsInset())
        };
    }
}
