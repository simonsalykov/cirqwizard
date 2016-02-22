package org.cirqwizard.layers;

import java.util.HashMap;

public class Board
{
    public enum LayerType
    {
        TOP, BOTTOM, DRILLING, MILLING, SOLDER_PASTE, PLACEMENT
    }

    private HashMap<LayerType, BoardLayer> layers = new HashMap<>();

    public BoardLayer getLayer(LayerType type)
    {
        return layers.get(type);
    }

    public void setLayer(LayerType type, BoardLayer layer)
    {
        layers.put(type, layer);
    }
}
