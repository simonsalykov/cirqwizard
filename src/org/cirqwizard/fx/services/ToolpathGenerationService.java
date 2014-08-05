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
    private IntegerProperty toolDiameter = new SimpleIntegerProperty();
    private IntegerProperty feedProperty = new SimpleIntegerProperty();
    private IntegerProperty arcFeedProperty = new SimpleIntegerProperty();
    private IntegerProperty zFeedProperty = new SimpleIntegerProperty();
    private IntegerProperty clearanceProperty = new SimpleIntegerProperty();
    private IntegerProperty safetyHeightProperty = new SimpleIntegerProperty();

    private MainApplication mainApplication;
    private StringProperty generationStageProperty = new SimpleStringProperty();
    private DoubleProperty overallProgressProperty;
    private StringProperty estimatedMachiningTimeProperty;
    private Context context;

    private Integer lastToolDiameter;

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
                if (layer instanceof TraceLayer)
                {
                    InsulationMillingSettings settings = SettingsFactory.getInsulationMillingSettings();
                    int diameter = toolDiameter.getValue();
                    ToolpathsCacheKey cacheKey = new ToolpathsCacheKey(mainApplication.getState(), context.getAngle(), diameter, settings.getAdditionalPasses().getValue(),
                            settings.getAdditionalPassesOverlap().getValue(), settings.getAdditionalPassesPadsOnly().getValue());
                    ToolpathsCache cache = null;
                    try
                    {
                        cache = ToolpathsPersistor.loadFromFile(context.getFileName() + ".tmp");
                    }
                    catch (ToolpathPersistingException e)
                    {
                        LoggerFactory.getApplicationLogger().log(Level.INFO, e.getMessage(), e);
                    }

                    TraceLayer traceLayer = (TraceLayer) layer;
                    if (cache != null && cache.hasValidData(context.getFile().lastModified()))
                    {
                        if (cache.getToolpaths(cacheKey) != null)
                        {
                            traceLayer.setToolpaths(cache.getToolpaths(cacheKey));
                            return FXCollections.observableArrayList(cache.getToolpaths(cacheKey));
                        }
                    }
                    else
                        cache = new ToolpathsCache();

                    final ToolpathGenerator generator = new ToolpathGenerator();
                    ApplicationSettings applicationSettings = SettingsFactory.getApplicationSettings();
                    generator.init(mainApplication.getContext().getBoardWidth() + 1, mainApplication.getContext().getBoardHeight() + 1,
                            diameter / 2, diameter, traceLayer.getElements(), applicationSettings.getProcessingThreads().getValue());
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

                    List<Toolpath> toolpaths = generator.generate();
                    if (toolpaths == null || toolpaths.size() == 0)
                        return null;
                    final int mergeTolerance = toolDiameter.intValue() / 4;
                    toolpaths = new ToolpathMerger(toolpaths, mergeTolerance).merge();

                    if (!settings.getAdditionalPassesPadsOnly().getValue())
                    {
                        Platform.runLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                generationStageProperty.setValue("Generating additional passes...");
                            }
                        });
                        for (int i = 0 ; i < settings.getAdditionalPasses().getValue(); i++)
                        {
                            int offset = diameter * (100 - settings.getAdditionalPassesOverlap().getValue()) / 100;
                            generator.init(mainApplication.getContext().getBoardWidth() + 1, mainApplication.getContext().getBoardHeight() + 1,
                                    diameter / 2 + offset * (i + 1), diameter, traceLayer.getElements(), applicationSettings.getProcessingThreads().getValue());
                            List<Toolpath> additionalToolpaths = generator.generate();
                            if (additionalToolpaths == null || additionalToolpaths.size() == 0)
                                continue;
                            toolpaths.addAll(new ToolpathMerger(additionalToolpaths, mergeTolerance).merge());
                        }
                    }
                    else if (settings.getAdditionalPasses().getValue() > 0)
                    {
                        final AdditionalToolpathGenerator additionalGenerator = new AdditionalToolpathGenerator(mainApplication.getContext().getBoardWidth() + 1,
                                mainApplication.getContext().getBoardHeight() + 1, settings.getAdditionalPasses().getValue(),
                                settings.getAdditionalPassesOverlap().getValue(), diameter, applicationSettings.getProcessingThreads().getValue(), traceLayer.getElements());
                        Platform.runLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                generationStageProperty.setValue("Generating additional passes...");
                                overallProgressProperty.bind(additionalGenerator.progressProperty());
                            }
                        });
                        toolpaths.addAll(new ToolpathMerger(additionalGenerator.generate(), mergeTolerance).merge());
                    }


                    List<Chain> chains = new ChainDetector(toolpaths).detect();

                    final Optimizer optimizer = new Optimizer(chains, feedProperty.doubleValue() / ApplicationConstants.RESOLUTION / 60, zFeedProperty.doubleValue() / ApplicationConstants.RESOLUTION / 60,
                            arcFeedProperty.doubleValue() / ApplicationConstants.RESOLUTION / 60, clearanceProperty.doubleValue() / ApplicationConstants.RESOLUTION,
                            safetyHeightProperty.doubleValue() / ApplicationConstants.RESOLUTION, mergeTolerance);
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
                    Platform.runLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            estimatedMachiningTimeProperty.bind(Bindings.createStringBinding(new Callable<String>()
                            {
                                @Override
                                public String call() throws Exception
                                {
                                    long totalDuration = (long) TimeEstimator.calculateTotalDuration(optimizer.getCurrentBestSolution(),
                                            feedProperty.doubleValue() / ApplicationConstants.RESOLUTION / 60, zFeedProperty.doubleValue() / ApplicationConstants.RESOLUTION / 60,
                                            arcFeedProperty.doubleValue() / ApplicationConstants.RESOLUTION / 60, clearanceProperty.doubleValue() / ApplicationConstants.RESOLUTION,
                                            safetyHeightProperty.doubleValue() / ApplicationConstants.RESOLUTION,
                                            true, mergeTolerance);
                                    String time = format.format(totalDuration / 3600) + ":" + format.format(totalDuration % 3600 / 60) +
                                            ":" + format.format(totalDuration % 60);
                                    return "Estimated machining time: " + time;
                                }
                            }, optimizer.currentBestSolutionProperty()));
                        }
                    });
                    chains = optimizer.optimize();

                    toolpaths.clear();
                    for (Chain p : chains)
                        toolpaths.addAll(p.getSegments());
                    traceLayer.setToolpaths(toolpaths);

                    cache.setToolpaths(cacheKey, toolpaths);
                    cache.setLastModified(mainApplication.getState() == org.cirqwizard.fx.State.MILLING_TOP_INSULATION ? context.getTopLayerModificationDate() : context.getBottomLayerModificationDate());

                    try
                    {
                        ToolpathsPersistor.saveToFile(cache, context.getFileName()  + ".tmp");
                    }
                    catch (ToolpathPersistingException e)
                    {
                        LoggerFactory.getApplicationLogger().log(Level.INFO, e.getMessage(), e);
                    }
                }
                else if (layer instanceof SolderPasteLayer)
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
