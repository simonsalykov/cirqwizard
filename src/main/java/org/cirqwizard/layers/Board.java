package org.cirqwizard.layers;

import org.cirqwizard.excellon.ExcellonParser;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.GerberParser;
import org.cirqwizard.pp.PPParser;
import org.cirqwizard.settings.ImportSettings;
import org.cirqwizard.settings.SettingsFactory;

import java.io.*;
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
        if (new File(filename + ".drd").exists())
                setLayer(LayerType.DRILLING, new Layer(new ExcellonParser(new FileReader(filename + ".drd")).parse()));
        if (new File(filename + ".ncl").exists())
            setLayer(LayerType.MILLING, new Layer(new GerberParser(new FileReader(filename + ".ncl")).parse()));
        if (new File(filename + ".crc").exists())
            setLayer(LayerType.SOLDER_PASTE, new Layer(new GerberParser(new FileReader(filename + ".crc")).parse()));
        if (new File(filename + ".mnt").exists())
        {
            ImportSettings importSettings = SettingsFactory.getImportSettings();
            setLayer(LayerType.PLACEMENT, new Layer(new PPParser(new FileReader(filename + ".mnt"),
                    importSettings.getCentroidFileFormat().getValue().getRegex(),
                    importSettings.getCentroidUnits().getValue().getMultiplier()).parse()));
        }
        List<LayerType> toRemove = layers.keySet().stream().filter(k -> layers.get(k).getElements().isEmpty()).collect(Collectors.toList());
        toRemove.stream().forEach(k -> layers.remove(k));
        moveToOrigin();
    }

    private Reader getReaderForFile(String filename)
    {
        try
        {
            if (new File(filename).exists())
                return new FileReader(filename);
        }
        catch (FileNotFoundException e) {}
        return new StringReader("");
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

}
