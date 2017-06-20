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

import org.cirqwizard.excellon.ExcellonParser;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.GerberParser;
import org.cirqwizard.pp.PPParser;
import org.cirqwizard.settings.ImportSettings;
import org.cirqwizard.settings.SettingsFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Board
{
    public enum LayerType
    {
        TOP, BOTTOM, DRILLING, MILLING, SOLDER_PASTE, PLACEMENT
    }

    private HashMap<LayerType, Layer> layers = new HashMap<>();
    private int width;
    private int height;

    public Layer getLayer(LayerType type)
    {
        return layers.get(type);
    }

    public void setLayer(LayerType type, Layer layer)
    {
        layers.put(type, layer);
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public void loadLayers(String filename) throws IOException
    {
        if (new File(filename + ".cmp").exists())
            setLayer(LayerType.TOP, new Layer(new GerberParser(new FileReader(filename + ".cmp")).parse()));
        if (new File(filename + ".sol").exists())
            setLayer(LayerType.BOTTOM, new Layer(new GerberParser(new FileReader(filename + ".sol")).parse()));
        ImportSettings importSettings = SettingsFactory.getImportSettings();
        if (new File(filename + ".drd").exists())
                setLayer(LayerType.DRILLING, new Layer(new ExcellonParser(importSettings.getExcellonIntegerPlaces().getValue(),
                        importSettings.getExcellonDecimalPlaces().getValue(),
                        importSettings.getExcellonUnits().getValue().getMultiplier(),
                        importSettings.getZeroesOmision().getValue().isLeadingZeros(),
                        new FileReader(filename + ".drd")).parse()));
        if (new File(filename + ".ncl").exists())
            setLayer(LayerType.MILLING, new Layer(new GerberParser(new FileReader(filename + ".ncl")).parse()));
        if (new File(filename + ".crc").exists())
            setLayer(LayerType.SOLDER_PASTE, new Layer(new GerberParser(new FileReader(filename + ".crc")).parse()));
        if (new File(filename + ".mnt").exists())
        {
            setLayer(LayerType.PLACEMENT, new Layer(new PPParser(new FileReader(filename + ".mnt"),
                    importSettings.getCentroidFileFormat().getValue().getRegex(),
                    importSettings.getCentroidUnits().getValue().getMultiplier()).parse()));
        }
        List<LayerType> toRemove = layers.keySet().stream().filter(k -> layers.get(k).getElements().isEmpty()).collect(Collectors.toList());
        toRemove.stream().forEach(k -> layers.remove(k));
        if (hasLayers())
            moveToOrigin();
    }

    public void moveToOrigin()
    {
        int minX = layers.values().stream().mapToInt(layer -> layer.getMinPoint().getX()).min().getAsInt();
        int minY = layers.values().stream().mapToInt(layer -> layer.getMinPoint().getY()).min().getAsInt();
        Point min = new Point(-minX, -minY);
        layers.values().stream().forEach(layer -> layer.move(min));
        updateDimensions();
    }

    private void updateDimensions()
    {
        width = layers.values().stream().mapToInt(layer -> layer.getMaxPoint().getX()).max().getAsInt();
        height = layers.values().stream().mapToInt(layer -> layer.getMaxPoint().getY()).max().getAsInt();
    }

    public void rotate(boolean clockwise)
    {
        layers.values().stream().forEach(l -> l.rotate(clockwise));
        moveToOrigin();
    }

    public boolean hasLayers()
    {
        return !layers.isEmpty();
    }

}
