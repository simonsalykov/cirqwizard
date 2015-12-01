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

package org.cirqwizard.fx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.cirqwizard.layers.PCBLayout;
import org.cirqwizard.pp.ComponentId;
import org.cirqwizard.pp.Feeder;
import org.cirqwizard.settings.ToolSettings;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class Context
{
    public static enum PcbPlacement {FACE_UP, FACE_DOWN, FACE_UP_SPACER}

    private ObjectProperty<PCBLayout> pcbLayout = new SimpleObjectProperty<>();

    private PcbPlacement pcbPlacement;
    private Tool insertedTool;
    private ToolSettings currentMillingTool;
    private int currentMillingToolIndex;
    private PCBSize pcbSize;
    private Integer g54X;
    private Integer g54Y;
    private Integer g54Z;

    private int currentDrill;

    private int boardWidth;
    private int boardHeight;

    private ComponentId currentComponent;
    private Feeder feeder;
    private int feederRow;
    private Integer componentPitch;
    private Map<String, Integer> pitchCache;

    public PCBLayout getPcbLayout()
    {
        return pcbLayout.get();
    }

    public ObjectProperty<PCBLayout> pcbLayoutProperty()
    {
        return pcbLayout;
    }

    public void setFile(File file)
    {
        pcbPlacement = null;
        insertedTool = null;
        currentMillingTool = null;
        currentMillingToolIndex = -1;
        pcbSize = null;
        g54X = null;
        g54Y = null;
        g54Z = null;
        currentDrill = 0;
        boardWidth = 0;
        boardHeight = 0;
        currentComponent = null;
        feeder = null;
        feederRow = 0;
        componentPitch = 0;
        pitchCache = new HashMap<>();
        pcbLayout.setValue(new PCBLayout());
        pcbLayout.getValue().setFile(file);
    }

    public PcbPlacement getPcbPlacement()
    {
        return pcbPlacement;
    }

    public void setPcbPlacement(PcbPlacement pcbPlacement)
    {
        this.pcbPlacement = pcbPlacement;
    }

    public Tool getInsertedTool()
    {
        return insertedTool;
    }

    public void setInsertedTool(Tool insertedTool)
    {
        this.insertedTool = insertedTool;
    }

    public ToolSettings getCurrentMillingTool()
    {
        return currentMillingTool;
    }

    public void setCurrentMillingTool(ToolSettings currentMillingTool)
    {
        this.currentMillingTool = currentMillingTool;
    }

    public int getCurrentMillingToolIndex()
    {
        return currentMillingToolIndex;
    }

    public void setCurrentMillingToolIndex(int currentMillingToolIndex)
    {
        this.currentMillingToolIndex = currentMillingToolIndex;
    }

    public PCBSize getPcbSize()
    {
        return pcbSize;
    }

    public void setPcbSize(PCBSize pcbSize)
    {
        this.pcbSize = pcbSize;
    }

    public Integer getG54X()
    {
        return g54X;
    }

    public void setG54X(Integer g54X)
    {
        this.g54X = g54X;
    }

    public Integer getG54Y()
    {
        return g54Y;
    }

    public void setG54Y(Integer g54Y)
    {
        this.g54Y = g54Y;
    }

    public Integer getG54Z()
    {
        return g54Z;
    }

    public void setG54Z(Integer g54Z)
    {
        this.g54Z = g54Z;
    }

    public int getCurrentDrill()
    {
        return currentDrill;
    }

    public void setCurrentDrill(int currentDrill)
    {
        this.currentDrill = currentDrill;
    }

    public int getBoardHeight()
    {
        return boardHeight;
    }

    public void setBoardHeight(int boardHeight)
    {
        this.boardHeight = boardHeight;
    }

    public int getBoardWidth()
    {
        return boardWidth;
    }

    public void setBoardWidth(int boardWidth)
    {
        this.boardWidth = boardWidth;
    }

    public ComponentId getCurrentComponent()
    {
        return currentComponent;
    }

    public void setCurrentComponent(ComponentId currentComponent)
    {
        this.currentComponent = currentComponent;
    }

    public Feeder getFeeder()
    {
        return feeder;
    }

    public void setFeeder(Feeder feeder)
    {
        this.feeder = feeder;
    }

    public int getFeederRow()
    {
        return feederRow;
    }

    public void setFeederRow(int feederRow)
    {
        this.feederRow = feederRow;
    }

    public Integer getComponentPitch()
    {
        return componentPitch;
    }

    public void setComponentPitch(int componentPitch)
    {
        this.componentPitch = componentPitch;
    }

    public Integer getPitchFromCache(String componentPackage)
    {
        return pitchCache.get(componentPackage);
    }

    public void savePitchToCache(String componentPackage, Integer pitch)
    {
        pitchCache.put(componentPackage, pitch);
    }
}
