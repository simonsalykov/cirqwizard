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

package org.cirqwizard.fx.services;

import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.MainApplication;
import org.cirqwizard.layers.*;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.render.Raster;
import org.cirqwizard.toolpath.DrillPoint;
import org.cirqwizard.toolpath.Toolpath;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


public class ToolpathGenerationService extends Service<ObservableList<Toolpath>>
{
    private Property<String> toolDiameter = new SimpleObjectProperty<String>();
    private MainApplication mainApplication;
    private DoubleProperty traceProgressProperty;
    private DoubleProperty overallProgressProperty;
    private Context context;

    public ToolpathGenerationService(MainApplication mainApplication, DoubleProperty traceProgressProperty, DoubleProperty overallProgressProperty)
    {
        this.mainApplication = mainApplication;
        this.context = mainApplication.getContext();
        this.traceProgressProperty = traceProgressProperty;
        this.overallProgressProperty = overallProgressProperty;
    }

    @Override
    protected Task<ObservableList<Toolpath>> createTask()
    {
        return new ToolpathGenerationTask();
    }

    public Property<String> toolDiameterProperty()
    {
        return toolDiameter;
    }

    private Layer getLayer()
    {
        if (mainApplication.getState() == org.cirqwizard.fx.State.MILLING_TOP_INSULATION)
            return context.getTopTracesLayer();
        if (mainApplication.getState() == org.cirqwizard.fx.State.MILLING_BOTTOM_INSULATION)
            return context.getBottomTracesLayer();
        if (mainApplication.getState() == org.cirqwizard.fx.State.DRILLING)
            return context.getDrillingLayer();
        if (mainApplication.getState() == org.cirqwizard.fx.State.MILLING_CONTOUR)
            return context.getMillingLayer();
        if (mainApplication.getState() == org.cirqwizard.fx.State.DISPENSING)
            return context.getSolderPasteLayer();
        return null;
    }

    public class ToolpathGenerationTask extends Task<ObservableList<Toolpath>>
    {
        @Override
        protected ObservableList<Toolpath> call() throws Exception
        {
            try
            {
                Layer layer = getLayer();
                if (layer instanceof TraceLayer)
                {
                    Raster raster = new Raster(mainApplication.getContext().getBoardWidth() + 1, mainApplication.getContext().getBoardHeight() + 1, 1000, Double.valueOf(toolDiameter.getValue()) / 2);
                    TraceLayer traceLayer = (TraceLayer) layer;
                    traceProgressProperty.bind(raster.traceProgressProperty());
                    overallProgressProperty.bind(raster.generationProgressProperty());
                    long t = System.currentTimeMillis();
                    traceLayer.generateToolpaths(raster, new RealNumber(toolDiameter.getValue()));
                    t = System.currentTimeMillis() - t;
                    System.out.println("generation time: " + t);
                }
                else if (layer instanceof SolderPasteLayer)
                {
                    SolderPasteLayer solderPasteLayer = (SolderPasteLayer) layer;
                    solderPasteLayer.generateToolpaths(new RealNumber(toolDiameter.getValue()));
                }
                else if (layer instanceof MillingLayer)
                {
                    MillingLayer millingLayer = (MillingLayer) layer;
                    millingLayer.generateToolpaths();
                }

                List<? extends Toolpath> toolpaths = layer.getToolpaths();
                if (layer instanceof DrillingLayer)
                {
                    ArrayList<DrillPoint> drillPoints = new ArrayList<DrillPoint>();
                    for (DrillPoint p : ((DrillingLayer)layer).getToolpaths())
                        if (Math.abs(p.getToolDiameter().doubleValue() - Double.valueOf(toolDiameter.getValue())) < 0.05)
                            drillPoints.add(p);
                    toolpaths = drillPoints;
                }

                return FXCollections.observableArrayList(toolpaths);
            }
            catch (NumberFormatException e)
            {
                LoggerFactory.getApplicationLogger().log(Level.WARNING, "Could not parse tool diameter", e);
                throw e;
            }
            catch (Exception e)
            {
                LoggerFactory.logException("Error generating toolpaths", e);
                throw e;
            }
        }
    }

}
