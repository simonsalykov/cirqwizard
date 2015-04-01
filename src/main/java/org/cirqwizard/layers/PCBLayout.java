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

import org.cirqwizard.gerber.GerberParser;
import org.cirqwizard.excellon.ExcellonParser;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.pp.ComponentId;
import org.cirqwizard.pp.PPParser;
import org.cirqwizard.settings.ApplicationConstants;
import org.cirqwizard.settings.ApplicationSettings;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.toolpath.CuttingToolpath;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class PCBLayout
{
    private NumberFormat drillFormat = new DecimalFormat("0.0#");

    private File file;
    private boolean fileLoaded = false;

    private TraceLayer topTracesLayer;
    private TraceLayer bottomTracesLayer;
    private SolderPasteLayer solderPasteLayer;
    private MillingLayer millingLayer;
    private DrillingLayer drillingLayer;
    private ComponentsLayer componentsLayer;

    private int angle;

    private List<String> drillDiameters;
    private String contourMillDiameter;
    private List<ComponentId> componentIds;

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

    public ArrayList<Layer> getLayers()
    {
        ArrayList<Layer> layers = new ArrayList<>();
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

    public void setFile(File file)
    {
        this.file = file;
        fileLoaded = false;
    }

    public File getFile()
    {
        return file;
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

    public long getTopLayerModificationDate()
    {
        return new File(getFileName() + ".cmp").lastModified();
    }

    public long getBottomLayerModificationDate()
    {
        return new File(getFileName() + ".sol").lastModified();
    }

    public void loadFile(String filename)
    {
        openFile(filename + ".cmp");
        openFile(filename + ".ncl");
        openFile(filename + ".crc");
        openFile(filename + ".sol");
        openFile(filename + ".drd");
        openFile(filename + ".mnt");
        moveToOrigin();
        fileLoaded = true;
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
        angle += clockwise ? -90 : 90;
        angle =  angle % 360;           // reduce the angle
        angle = (angle + 360) % 360;    // force it to be the positive

        for (Layer layer : getLayers())
            layer.rotate(clockwise);
        moveToOrigin();
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
    }

    private void openBottomTraces(String file)
    {
        bottomTracesLayer = new TraceLayer();
        bottomTracesLayer.setElements(parseGerber(file));
        if (bottomTracesLayer.getElements().isEmpty())
            bottomTracesLayer = null;
    }

    private void openSolderPaste(String file)
    {
        solderPasteLayer = new SolderPasteLayer();
        solderPasteLayer.setElements(parseGerber(file));
        if (solderPasteLayer.getElements().isEmpty())
            solderPasteLayer = null;
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
            contourMillDiameter = drillFormat.format((double)((CuttingToolpath)millingLayer.getToolpaths().get(0)).getToolDiameter() / ApplicationConstants.RESOLUTION);
        }
    }

    private void openDrilling(String file)
    {
        drillingLayer = new DrillingLayer();
        try
        {
            ApplicationSettings settings = SettingsFactory.getApplicationSettings();
            ExcellonParser parser = new ExcellonParser(settings.getExcellonDecimalPlaces().getValue(), settings.getExcellonUnits().getValue().getMultiplier(), new FileReader(file));
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
                drillDiameters.add(drillFormat.format((double)diameter / ApplicationConstants.RESOLUTION));
        }
    }

    private void openComponents(String file)
    {
        componentsLayer = new ComponentsLayer();
        try
        {
            PPParser parser = new PPParser(new FileReader(file), SettingsFactory.getApplicationSettings().getCentroidFileFormat().getValue());
            componentsLayer.setPoints(parser.parse());
            componentIds = new ArrayList<>(componentsLayer.getComponentIds());
        }
        catch (IOException e)
        {
            LoggerFactory.logException("Could not load centroid file", e);
        }
        if (componentsLayer.getPoints().isEmpty())
            componentsLayer = null;
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

    public List<String> getDrillDiameters()
    {
        return drillDiameters;
    }

    public String getContourMillDiameter()
    {
        return contourMillDiameter;
    }

    public List<ComponentId> getComponentIds()
    {
        return componentIds;
    }

    public int getAngle()
    {
        return angle;
    }

}
