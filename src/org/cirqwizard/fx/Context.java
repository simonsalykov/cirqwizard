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

import org.cirqwizard.GerberParser;
import org.cirqwizard.excellon.ExcellonParser;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.layers.*;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.pp.ComponentId;
import org.cirqwizard.pp.Feeder;
import org.cirqwizard.pp.PPParser;
import org.cirqwizard.settings.Settings;
import org.cirqwizard.toolpath.CuttingToolpath;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


public class Context
{
    private Settings settings;
    private File file;
    private boolean fileLoaded = false;

    private TraceLayer topTracesLayer;
    private TraceLayer bottomTracesLayer;
    private SolderPasteLayer solderPasteLayer;
    private MillingLayer millingLayer;
    private DrillingLayer drillingLayer;
    private ComponentsLayer componentsLayer;

    private boolean topTracesSelected;
    private boolean bottomTracesSelected;
    private boolean drillingSelected;
    private boolean contourSelected;
    private boolean pasteSelected;
    private boolean placingSelected;

    private PCBSize pcbSize;
    private String g54X;
    private String g54Y;
    private String g54Z;

    private boolean zOffsetEstablished;

    private NumberFormat drillFormat = new DecimalFormat("0.0#");
    private List<String> drillDiameters;
    private int currentDrill;

    private String contourMillDiameter;

    private double boardWidth;
    private double boardHeight;

    private String dispensingNeedleDiameter;

    private List<ComponentId> componentIds;
    private int currentComponent;
    private Feeder feeder;
    private int feederRow;
    private RealNumber componentPitch;

    public Context(Settings settings)
    {
        this.settings = settings;
    }

    public void reset()
    {
        file = null;
        topTracesLayer = null;
        bottomTracesLayer = null;
        solderPasteLayer = null;
        millingLayer = null;
        drillingLayer = null;
        componentsLayer = null;
    }

    public File getFile()
    {
        return file;
    }

    public void setFile(File file)
    {
        reset();
        this.file = file;
        fileLoaded = false;
    }

    public boolean isFileLoaded()
    {
        return fileLoaded;
    }

    public TraceLayer getTopTracesLayer()
    {
        return topTracesLayer;
    }

    public TraceLayer getBottomTracesLayer()
    {
        return bottomTracesLayer;
    }

    public SolderPasteLayer getSolderPasteLayer()
    {
        return solderPasteLayer;
    }

    public MillingLayer getMillingLayer()
    {
        return millingLayer;
    }

    public DrillingLayer getDrillingLayer()
    {
        return drillingLayer;
    }

    public ComponentsLayer getComponentsLayer()
    {
        return componentsLayer;
    }

    public void loadFile()
    {
        String filename = file.getAbsolutePath();
        filename = filename.substring(0, filename.lastIndexOf('.'));
        openFile(filename + ".cmp");
        openFile(filename + ".ncl");
        openFile(filename + ".crc");
        openFile(filename + ".sol");
        openFile(filename + ".drd");
        openFile(filename + ".mnt");
        moveToOrigin();
        fileLoaded = true;
    }

    private void openFile(String file)
    {
        if (!new File(file).exists())
            return;
        if (file.toLowerCase().endsWith(".drd"))
            openDrilling(file);
        else if (file.toLowerCase().endsWith(".ncl"))
            openMilling(file);
        else if (file.toLowerCase().endsWith(".crc"))
            openSolderPaste(file);
        else if (file.toLowerCase().endsWith(".sol"))
            openBottomTraces(file);
        else if (file.toLowerCase().endsWith(".cmp"))
            openTopTraces(file);
        else if (file.toLowerCase().endsWith(".mnt"))
            openComponents(file);
    }

    private void openTopTraces(String file)
    {
        topTracesLayer = new TraceLayer();
        topTracesLayer.setElements(parseGerber(file));
        if (topTracesLayer.getElements().isEmpty())
            topTracesLayer = null;
        topTracesSelected = topTracesLayer != null;
    }

    private void openBottomTraces(String file)
    {
        bottomTracesLayer = new TraceLayer();
        bottomTracesLayer.setElements(parseGerber(file));
        if (bottomTracesLayer.getElements().isEmpty())
            bottomTracesLayer = null;
        bottomTracesSelected = bottomTracesLayer != null;
    }

    private void openSolderPaste(String file)
    {
        solderPasteLayer = new SolderPasteLayer();
        solderPasteLayer.setElements(parseGerber(file));
        if (solderPasteLayer.getElements().isEmpty())
            solderPasteLayer = null;
        pasteSelected = solderPasteLayer != null;
    }

    private void openMilling(String file)
    {
        millingLayer = new MillingLayer();
        millingLayer.setElements(parseGerber(file));
        if (millingLayer.getElements().isEmpty())
            millingLayer = null;
        else
        {
            millingLayer.generateToolpaths();
            contourMillDiameter = drillFormat.format(((CuttingToolpath)millingLayer.getToolpaths().get(0)).getToolDiameter().getValue());
        }
        contourSelected = millingLayer != null;
    }

    private void openDrilling(String file)
    {
        ExcellonParser parser = new ExcellonParser();
        parser.parse(file);
        drillingLayer = new DrillingLayer();
        drillingLayer.setDrillPoints(parser.getDrillPoints());
        if (drillingLayer.getToolpaths().isEmpty())
            drillingLayer = null;
        else
        {
            drillDiameters = new ArrayList<String>();
            for (RealNumber diameter : drillingLayer.getDrillDiameters())
                drillDiameters.add(drillFormat.format(diameter.getValue()));
        }
        drillingSelected = drillingLayer != null;
    }

    private void openComponents(String file)
    {
        PPParser parser = new PPParser(file);
        parser.parse();
        if (!parser.getComponents().isEmpty())
        {
            componentsLayer = new ComponentsLayer();
            componentsLayer.setPoints(parser.getComponents());
            componentIds = new ArrayList<ComponentId>(componentsLayer.getComponentIds());
        }
        placingSelected = componentsLayer != null;
    }

    private static ArrayList<GerberPrimitive> parseGerber(String file)
    {
        GerberParser parser = new GerberParser(file);
        parser.parse();
        return parser.getElements();
    }

    public ArrayList<Layer> getLayers()
    {
        ArrayList<Layer> layers = new ArrayList<Layer>();
        if (topTracesLayer != null)
            layers.add(topTracesLayer);
        if (drillingLayer != null)
            layers.add(drillingLayer);
        if (millingLayer != null)
            layers.add(millingLayer);
        if (bottomTracesLayer != null)
            layers.add(bottomTracesLayer);
        if (solderPasteLayer != null)
            layers.add(solderPasteLayer);
        if (componentsLayer != null)
            layers.add(componentsLayer);
        return layers;
    }

    private void moveToOrigin()
    {
        Point min = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (Layer layer : getLayers())
        {
            if (layer.getMinPoint().getX().lessThan(min.getX()))
                min = new Point(layer.getMinPoint().getX(), min.getY());
            if (layer.getMinPoint().getY().lessThan(min.getY()))
                min = new Point(min.getX(), layer.getMinPoint().getY());
        }
        min = new Point(min.getX().negate(), min.getY().negate());
        for (Layer layer : getLayers())
            layer.move(min);
    }

    public void rotate(boolean clockwise)
    {
        for (Layer layer : getLayers())
            layer.rotate(clockwise);
        moveToOrigin();
    }

    public boolean isTopTracesSelected()
    {
        return topTracesSelected;
    }

    public void setTopTracesSelected(boolean topTracesSelected)
    {
        this.topTracesSelected = topTracesSelected;
    }

    public boolean isBottomTracesSelected()
    {
        return bottomTracesSelected;
    }

    public void setBottomTracesSelected(boolean bottomTracesSelected)
    {
        this.bottomTracesSelected = bottomTracesSelected;
    }

    public boolean isDrillingSelected()
    {
        return drillingSelected;
    }

    public void setDrillingSelected(boolean drillingSelected)
    {
        this.drillingSelected = drillingSelected;
    }

    public boolean isContourSelected()
    {
        return contourSelected;
    }

    public void setContourSelected(boolean contourSelected)
    {
        this.contourSelected = contourSelected;
    }

    public boolean isPasteSelected()
    {
        return pasteSelected;
    }

    public void setPasteSelected(boolean pasteSelected)
    {
        this.pasteSelected = pasteSelected;
    }

    public boolean isPlacingSelected()
    {
        return placingSelected;
    }

    public void setPlacingSelected(boolean placingSelected)
    {
        this.placingSelected = placingSelected;
    }

    public PCBSize getPcbSize()
    {
        return pcbSize == null ? settings.getPCBSize() : pcbSize;
    }

    public void setPcbSize(PCBSize pcbSize)
    {
        this.pcbSize = pcbSize;
        settings.setPCBSize(pcbSize);
    }

    public String getG54X()
    {
        return g54X != null ? g54X : settings.getG54X();
    }

    public void setG54X(String g54X)
    {
        this.g54X = g54X;
        settings.setG54X(g54X);
    }

    public String getG54Y()
    {
        return g54Y != null ? g54Y : settings.getG54Y();
    }

    public void setG54Y(String g54Y)
    {
        this.g54Y = g54Y;
        settings.setG54Y(g54Y);
    }

    public String getG54Z()
    {
        return g54Z;
    }

    public void setG54Z(String g54Z)
    {
        this.g54Z = g54Z;
    }

    public boolean iszOffsetEstablished()
    {
        return zOffsetEstablished;
    }

    public void setzOffsetEstablished(boolean zOffsetEstablished)
    {
        this.zOffsetEstablished = zOffsetEstablished;
    }

    public List<String> getDrillDiameters()
    {
        return drillDiameters;
    }

    public int getCurrentDrill()
    {
        return currentDrill;
    }

    public void setCurrentDrill(int currentDrill)
    {
        this.currentDrill = currentDrill;
    }

    public String getContourMillDiameter()
    {
        return contourMillDiameter;
    }

    public double getBoardHeight()
    {
        return boardHeight;
    }

    public void setBoardHeight(double boardHeight)
    {
        this.boardHeight = boardHeight;
    }

    public double getBoardWidth()
    {
        return boardWidth;
    }

    public void setBoardWidth(double boardWidth)
    {
        this.boardWidth = boardWidth;
    }

    public String getDispensingNeedleDiameter()
    {
        return dispensingNeedleDiameter;
    }

    public void setDispensingNeedleDiameter(String dispensingNeedleDiameter)
    {
        this.dispensingNeedleDiameter = dispensingNeedleDiameter;
    }

    public List<ComponentId> getComponentIds()
    {
        return componentIds;
    }

    public int getCurrentComponent()
    {
        return currentComponent;
    }

    public void setCurrentComponent(int currentComponent)
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

    public RealNumber getComponentPitch()
    {
        return componentPitch;
    }

    public void setComponentPitch(RealNumber componentPitch)
    {
        this.componentPitch = componentPitch;
    }
}
