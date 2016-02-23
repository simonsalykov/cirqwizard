package org.cirqwizard.layers;

import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.GerberParser;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Board
{
    public enum LayerType
    {
        TOP, BOTTOM, DRILLING, MILLING, SOLDER_PASTE, PLACEMENT
    }

    private HashMap<LayerType, BoardLayer> layers = new HashMap<>();
    private int width;
    private int height;

    public BoardLayer getLayer(LayerType type)
    {
        return layers.get(type);
    }

    public void setLayer(LayerType type, BoardLayer layer)
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
        setLayer(LayerType.TOP, new BoardLayer(new GerberParser(new FileReader(filename + ".cmp")).parse()));
        setLayer(LayerType.BOTTOM, new BoardLayer(new GerberParser(new FileReader(filename + ".sol")).parse()));
        setLayer(LayerType.MILLING, new BoardLayer(new GerberParser(new FileReader(filename + ".ncl")).parse()));
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

}
