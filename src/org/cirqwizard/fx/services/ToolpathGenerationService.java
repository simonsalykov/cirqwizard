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

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.MainApplication;
import org.cirqwizard.generation.ToolpathGenerator;
import org.cirqwizard.generation.ToolpathMerger;
import org.cirqwizard.generation.optimizer.Chain;
import org.cirqwizard.generation.optimizer.ChainDetector;
import org.cirqwizard.generation.optimizer.Optimizer;
import org.cirqwizard.generation.optimizer.TimeEstimator;
import org.cirqwizard.layers.*;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.Settings;
import org.cirqwizard.toolpath.DrillPoint;
import org.cirqwizard.toolpath.Toolpath;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;


public class ToolpathGenerationService extends Service<ObservableList<Toolpath>>
{
    private IntegerProperty toolDiameter = new SimpleIntegerProperty();
    private IntegerProperty feedProperty = new SimpleIntegerProperty();
    private IntegerProperty zFeedProperty = new SimpleIntegerProperty();
    private IntegerProperty clearanceProperty = new SimpleIntegerProperty();
    private IntegerProperty safetyHeightProperty = new SimpleIntegerProperty();

    private MainApplication mainApplication;
    private StringProperty generationStageProperty = new SimpleStringProperty();
    private DoubleProperty overallProgressProperty;
    private StringProperty estimatedMachiningTimeProperty;
    private Context context;

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
                overallProgressProperty.unbind();
                generationStageProperty.unbind();
                estimatedMachiningTimeProperty.unbind();

                Layer layer = getLayer();
                if (layer instanceof TraceLayer)
                {
                    int diameter = toolDiameter.getValue();
                    final ToolpathGenerator generator = new ToolpathGenerator(mainApplication.getContext().getBoardWidth() + 1, mainApplication.getContext().getBoardHeight() + 1,
                            diameter / 2, diameter, ((TraceLayer) layer).getElements());

                    Platform.runLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            generationStageProperty.setValue("Generating tool paths...");
                            overallProgressProperty.bind(generator.progressProperty());
                            estimatedMachiningTimeProperty.setValue("");
                        }
                    });
                    TraceLayer traceLayer = (TraceLayer) layer;
                    List<Toolpath> toolpaths = generator.generate();

                    toolpaths = new ToolpathMerger(toolpaths).merge();

                    List<Chain> chains = new ChainDetector(toolpaths).detect();

                    final Optimizer optimizer = new Optimizer(chains);
                    Platform.runLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            generationStageProperty.setValue("Optimizing milling time...");
                            overallProgressProperty.unbind();
                            overallProgressProperty.bind(optimizer.progressProperty());
                        }
                    });

                    final DecimalFormat format = new DecimalFormat("00");
                    estimatedMachiningTimeProperty.bind(Bindings.createStringBinding(new Callable<String>()
                    {
                        @Override
                        public String call() throws Exception
                        {
                            long totalDuration = (long) TimeEstimator.calculateTotalDuration(optimizer.getCurrentBestSolution(),
                                    feedProperty.doubleValue() / Settings.RESOLUTION / 60, zFeedProperty.doubleValue() / Settings.RESOLUTION / 60,
                                    clearanceProperty.doubleValue() / Settings.RESOLUTION, safetyHeightProperty.doubleValue() / Settings.RESOLUTION,
                                    true);
                            String time = format.format(totalDuration / 3600) + ":" + format.format(totalDuration % 3600 / 60) +
                                    ":" + format.format(totalDuration % 60);
                            return "Estimated machining time: " + time;
                        }
                    }, optimizer.currentBestSolutionProperty()));
                    chains = optimizer.optimize();

                    toolpaths.clear();
                    for (Chain p : chains)
                        toolpaths.addAll(p.getSegments());
                    traceLayer.setToolpaths(toolpaths);
                }
                else if (layer instanceof SolderPasteLayer)
                {
                    SolderPasteLayer solderPasteLayer = (SolderPasteLayer) layer;
                    solderPasteLayer.generateToolpaths((int)(Double.valueOf(toolDiameter.getValue()) * Settings.RESOLUTION));
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
                        if (Math.abs(Double.valueOf(toolDiameter.getValue()) - p.getToolDiameter()) < 0.05)
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
