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

package org.cirqwizard.fx.machining;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.MainApplication;
import org.cirqwizard.generation.AdditionalToolpathGenerator;
import org.cirqwizard.generation.ToolpathGenerator;
import org.cirqwizard.generation.ToolpathMerger;
import org.cirqwizard.generation.optimizer.Chain;
import org.cirqwizard.generation.optimizer.ChainDetector;
import org.cirqwizard.generation.optimizer.Optimizer;
import org.cirqwizard.generation.optimizer.TimeEstimator;
import org.cirqwizard.layers.*;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.*;
import org.cirqwizard.toolpath.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;


public class ToolpathGenerationService extends Service<ObservableList<Toolpath>>
{
    protected IntegerProperty toolDiameter = new SimpleIntegerProperty();
    protected IntegerProperty feedProperty = new SimpleIntegerProperty();
    protected IntegerProperty arcFeedProperty = new SimpleIntegerProperty();
    protected IntegerProperty zFeedProperty = new SimpleIntegerProperty();
    protected IntegerProperty clearanceProperty = new SimpleIntegerProperty();
    protected IntegerProperty safetyHeightProperty = new SimpleIntegerProperty();

    protected MainApplication mainApplication;
    protected StringProperty generationStageProperty = new SimpleStringProperty();
    protected DoubleProperty overallProgressProperty;
    protected StringProperty estimatedMachiningTimeProperty;
    protected Context context;

    protected Integer lastToolDiameter;

    public ToolpathGenerationService(MainApplication mainApplication, DoubleProperty overallProgressProperty,
                                     StringProperty estimatedMachiningTimeProperty)
    {
        this.mainApplication = mainApplication;
        this.context = mainApplication.getContext();
        this.overallProgressProperty = overallProgressProperty;
        this.estimatedMachiningTimeProperty = estimatedMachiningTimeProperty;
    }

    @Override
    protected Task<ObservableList<Toolpath>> createTask()
    {
        return new ToolpathGenerationTask();
    }

    public IntegerProperty toolDiameterProperty()
    {
        return toolDiameter;
    }

    public IntegerProperty feedProperty()
    {
        return feedProperty;
    }

    public IntegerProperty arcFeedProperty()
    {
        return arcFeedProperty;
    }

    public IntegerProperty zFeedProperty()
    {
        return zFeedProperty;
    }

    public IntegerProperty clearanceProperty()
    {
        return clearanceProperty;
    }

    public IntegerProperty safetyHeightProperty()
    {
        return safetyHeightProperty;
    }

    private Layer getLayer()
    {
//        if (mainApplication.getState() == org.cirqwizard.fx.State.MILLING_CONTOUR)
//            return context.getPcbLayout().getMillingLayer();
//        if (mainApplication.getState() == org.cirqwizard.fx.State.DISPENSING)
//            return context.getPcbLayout().getSolderPasteLayer();
        return null;
    }

    public Integer getLastToolDiameter()
    {
        return lastToolDiameter;
    }

    public StringProperty generationStageProperty()
    {
        return generationStageProperty;
    }

    public class ToolpathGenerationTask extends Task<ObservableList<Toolpath>>
    {
        @Override
        protected ObservableList<Toolpath> call() throws Exception
        {
            try
            {
                lastToolDiameter = toolDiameter.get();
                overallProgressProperty.unbind();
                generationStageProperty.unbind();
                estimatedMachiningTimeProperty.unbind();

                Layer layer = getLayer();
                if (layer instanceof SolderPasteLayer)
                {
                    SolderPasteLayer solderPasteLayer = (SolderPasteLayer) layer;
                    solderPasteLayer.generateToolpaths(toolDiameter.getValue());
                }
                else if (layer instanceof MillingLayer)
                {
                    MillingLayer millingLayer = (MillingLayer) layer;
                    millingLayer.generateToolpaths();
                }

                List<? extends Toolpath> toolpaths = layer.getToolpaths();
                if (layer instanceof DrillingLayer)
                {
                    ArrayList<DrillPoint> drillPoints = new ArrayList<>();
                    for (DrillPoint p : ((DrillingLayer)layer).getToolpaths())
                        if (Math.abs(toolDiameter.getValue() - p.getToolDiameter()) < 50)
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
