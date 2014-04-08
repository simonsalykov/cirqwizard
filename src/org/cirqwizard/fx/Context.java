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
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.pp.ComponentId;
import org.cirqwizard.pp.Feeder;
import org.cirqwizard.pp.PPParser;
import org.cirqwizard.settings.Settings;
import org.cirqwizard.toolpath.CuttingToolpath;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    private Integer g54X;
    private Integer g54Y;
    private Integer g54Z;

    private boolean zOffsetEstablished;

    private NumberFormat drillFormat = new DecimalFormat("0.0#");
    private List<String> drillDiameters;
    private int currentDrill;

    private String contourMillDiameter;

    private int boardWidth;
    private int boardHeight;

    private int angle;

    private String dispensingNeedleDiameter;

    private List<ComponentId> componentIds;
    private int currentComponent;
    private Feeder feeder;
    private int feederRow;
    private Integer componentPitch;

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

    public String getFileName()
    {
        String filename = file.getAbsolutePath();
        return filename.substring(0, filename.lastIndexOf('.'));
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
        String filename = getFileName();
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
            contourMillDiameter = drillFormat.format((double)((CuttingToolpath)millingLayer.getToolpaths().get(0)).getToolDiameter() / Settings.RESOLUTION);
        }
        contourSelected = millingLayer != null;
    }

    private void openDrilling(String file)
    {
        drillingLayer = new DrillingLayer();
        try
        {
            ExcellonParser parser = new ExcellonParser(settings, new FileReader(file));
            drillingLayer.setDrillPoints(parser.parse());
        }
        catch (IOException | RuntimeException e)
        {
            LoggerFactory.logException("Could not load excellon file", e);
        }
        if (drillingLayer.getToolpaths() == null || drillingLayer.getToolpaths().isEmpty())
            drillingLayer = null;
        else
        {
            drillDiameters = new ArrayList<>();
            for (int diameter : drillingLayer.getDrillDiameters())
                drillDiameters.add(drillFormat.format((double)diameter / Settings.RESOLUTION));
        }
        drillingSelected = drillingLayer != null;
    }

    private void openComponents(String file)
    {
        componentsLayer = new ComponentsLayer();
        try
        {
            PPParser parser = new PPParser(new FileReader(file), settings.getImportPPRegex());
            componentsLayer.setPoints(parser.parse());
            componentIds = new ArrayList<>(componentsLayer.getComponentIds());
        }
        catch (IOException e)
        {
            LoggerFactory.logException("Could not load centroid file", e);
        }
        if (componentsLayer.getPoints().isEmpty())
            componentsLayer = null;
        placingSelected = componentsLayer != null;
    }

    public long getTopLayerModificationDate()
    {
        return new File(getFileName() + ".cmp").lastModified();
    }

    public long getBottomLayerModificationDate()
    {
        return new File(getFileName() + ".sol").lastModified();
    }

    private static ArrayList<GerberPrimitive> parseGerber(String file)
    {
        try
        {
            GerberParser parser = new GerberParser(new FileReader(file));
            return parser.parse();
        }
        catch (IOException e)
        {
            LoggerFactory.logException("Could not load gerber file", e);
            return null;
        }
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
            if (layer.getMinPoint().getX() < min.getX())
                min = new Point(layer.getMinPoint().getX(), min.getY());
            if (layer.getMinPoint().getY() < min.getY())
                min = new Point(min.getX(), layer.getMinPoint().getY());
        }
        min = new Point(-min.getX(), -min.getY());
        for (Layer layer : getLayers())
            layer.move(min);
    }

    public void rotate(boolean clockwise)
    {
        angle+= clockwise ? -90 : 90;
        angle =  angle % 360;           // reduce the angle
        angle = (angle + 360) % 360;    // force it to be the positive

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

    public Integer getG54X()
    {
        return g54X != null ? g54X : settings.getG54X();
    }

    public void setG54X(Integer g54X)
    {
        this.g54X = g54X;
        settings.setG54X(g54X);
    }

    public Integer getG54Y()
    {
        return g54Y != null ? g54Y : settings.getG54Y();
    }

    public void setG54Y(Integer g54Y)
    {
        this.g54Y = g54Y;
        settings.setG54Y(g54Y);
    }

    public Integer getG54Z()
    {
        return g54Z;
    }

    public void setG54Z(Integer g54Z)
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

    public int getAngle()
    {
        return angle;
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

    public Integer getComponentPitch()
    {
        return componentPitch;
    }

    public void setComponentPitch(int componentPitch)
    {
        this.componentPitch = componentPitch;
    }
}
