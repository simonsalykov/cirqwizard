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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.cirqwizard.fx.MainApplication;
import org.cirqwizard.generation.AdditionalToolpathGenerator;
import org.cirqwizard.generation.RubOutToolpathGenerator;
import org.cirqwizard.generation.ToolpathGenerator;
import org.cirqwizard.generation.ToolpathMerger;
import org.cirqwizard.generation.optimizer.Chain;
import org.cirqwizard.generation.optimizer.ChainDetector;
import org.cirqwizard.generation.optimizer.Optimizer;
import org.cirqwizard.generation.optimizer.TimeEstimator;
import org.cirqwizard.layers.Layer;
import org.cirqwizard.layers.TraceLayer;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.*;
import org.cirqwizard.toolpath.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class RuboutToolpathGenerationService extends ToolpathGenerationService
{
    private Layer layer;
    private int cacheLayerId;
    private long layerModificationDate;
    private GenerationKey generationKey;

    public RuboutToolpathGenerationService(MainApplication mainApplication, DoubleProperty overallProgressProperty,
                                           StringProperty estimatedMachiningTimeProperty,
                                           Layer layer, int cacheLayerId, long layerModificationDate)
    {
        super(mainApplication, overallProgressProperty, estimatedMachiningTimeProperty);
        this.layer = layer;
        this.cacheLayerId = cacheLayerId;
        this.layerModificationDate = layerModificationDate;
    }

    private class GenerationKey
    {
        private int toolDiameter;
        private int additionalToolpaths;
        private int additionalToolpathsOverlap;
        private boolean additionalToolpathsAroundPadsOnly;

        private GenerationKey(int toolDiameter, int additionalToolpaths, int additionalToolpathsOverlap, boolean additionalToolpathsAroundPadsOnly)
        {
            this.toolDiameter = toolDiameter;
            this.additionalToolpaths = additionalToolpaths;
            this.additionalToolpathsOverlap = additionalToolpathsOverlap;
            this.additionalToolpathsAroundPadsOnly = additionalToolpathsAroundPadsOnly;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GenerationKey that = (GenerationKey) o;

            if (additionalToolpaths != that.additionalToolpaths) return false;
            if (additionalToolpathsAroundPadsOnly != that.additionalToolpathsAroundPadsOnly) return false;
            if (additionalToolpathsOverlap != that.additionalToolpathsOverlap) return false;
            if (toolDiameter != that.toolDiameter) return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = toolDiameter;
            result = 31 * result + additionalToolpaths;
            result = 31 * result + additionalToolpathsOverlap;
            result = 31 * result + (additionalToolpathsAroundPadsOnly ? 1 : 0);
            return result;
        }
    }

    @Override
    public boolean needsRestart()
    {
        InsulationMillingSettings settings = SettingsFactory.getInsulationMillingSettings();
        GenerationKey newKey = new GenerationKey(settings.getToolDiameter().getValue(), settings.getAdditionalPasses().getValue(),
                settings.getAdditionalPassesOverlap().getValue(), settings.getAdditionalPassesPadsOnly().getValue());
        return generationKey == null || !generationKey.equals(newKey);
    }

    @Override
    protected Task<ObservableList<Toolpath>> createTask()
    {
        return new Task<ObservableList<Toolpath>>()
        {
            @Override
            protected ObservableList<Toolpath> call() throws Exception
            {
                try
                {
                    RubOutSettings settings = SettingsFactory.getRubOutSettings();
//                    GenerationKey newKey = new GenerationKey(settings.getToolDiameter().getValue(), settings.getAdditionalPasses().getValue(),
//                            settings.getAdditionalPassesOverlap().getValue(), settings.getAdditionalPassesPadsOnly().getValue());
//                    if (generationKey != null && generationKey.equals(newKey))
//                        return null;
//                    generationKey = newKey;

                    overallProgressProperty.unbind();
                    generationStageProperty.unbind();
                    estimatedMachiningTimeProperty.unbind();

                    int diameter = settings.getToolDiameter().getValue();
//                    ToolpathsCacheKey cacheKey = new ToolpathsCacheKey(cacheLayerId, context.getPcbLayout().getAngle(), diameter, settings.getAdditionalPasses().getValue(),
//                            settings.getAdditionalPassesOverlap().getValue(), settings.getAdditionalPassesPadsOnly().getValue());
//                    ToolpathsCache cache = null;
//                    try
//                    {
//                        cache = ToolpathsPersistor.loadFromFile(context.getPcbLayout().getFileName() + ".tmp");
//                    }
//                    catch (ToolpathPersistingException e)
//                    {
//                        LoggerFactory.getApplicationLogger().log(Level.INFO, e.getMessage(), e);
//                    }
//
                    TraceLayer traceLayer = (TraceLayer) layer;
//                    if (cache != null && cache.hasValidData(context.getPcbLayout().getFile().lastModified()))
//                    {
//                        if (cache.getToolpaths(cacheKey) != null)
//                        {
//                            traceLayer.setToolpaths(cache.getToolpaths(cacheKey));
//                            return FXCollections.observableArrayList(cache.getToolpaths(cacheKey));
//                        }
//                    }
//                    else
//                        cache = new ToolpathsCache();

                    List<Toolpath> toolpaths = new ArrayList<>();
                    ApplicationSettings applicationSettings = SettingsFactory.getApplicationSettings();

                    for (int pass = 0; pass < 2; pass++)
                    {
                        ToolpathGenerator g = new ToolpathGenerator();
                        g.init(mainApplication.getContext().getBoardWidth() + 1, mainApplication.getContext().getBoardHeight() + 1,
                                pass * (diameter - settings.getOverlap().getValue()) + settings.getInitialOffset().getValue() + diameter / 2, diameter, traceLayer.getElements(), applicationSettings.getProcessingThreads().getValue());
                        Platform.runLater(() ->
                        {
                            generationStageProperty.setValue("Generating tool paths...");
                            overallProgressProperty.bind(g.progressProperty());
                            estimatedMachiningTimeProperty.setValue("");
                        });

                        List<Toolpath> t = g.generate();
                        if (t == null || t.size() == 0)
                            continue;
                        toolpaths.addAll(new ToolpathMerger(t, diameter / 4).merge());
                    }

                    final RubOutToolpathGenerator generator = new RubOutToolpathGenerator();
                    generator.init(mainApplication.getContext().getBoardWidth() + 1, mainApplication.getContext().getBoardHeight() + 1,
                            settings.getInitialOffset().getValue(),
                            diameter, settings.getOverlap().getValue(), traceLayer.getElements(),
                            applicationSettings.getProcessingThreads().getValue(), 1);
                    Platform.runLater(() ->
                    {
                        generationStageProperty.setValue("Generating tool paths...");
                        overallProgressProperty.bind(generator.progressProperty());
                        estimatedMachiningTimeProperty.setValue("");
                    });

                    List<Toolpath> t = generator.generate();
                    final int mergeTolerance = diameter / 10;
                    long tt = System.currentTimeMillis();
                    if (t != null && t.size() > 0)
                        toolpaths.addAll(new ToolpathMerger(t, mergeTolerance).merge());


                    List<Chain> chains = new ChainDetector(toolpaths).detect();


                    final Optimizer optimizer = new Optimizer(chains, convertToDouble(settings.getFeedXY().getValue()) / 60, convertToDouble(settings.getFeedZ().getValue()) / 60,
                            convertToDouble(settings.getFeedXY().getValue()) / 60 * settings.getFeedArcs().getValue() / 100,
                            convertToDouble(settings.getClearance().getValue()), convertToDouble(settings.getSafetyHeight().getValue()), mergeTolerance);
                    Platform.runLater(() ->
                    {
                        generationStageProperty.setValue("Optimizing milling time...");
                        overallProgressProperty.unbind();
                        overallProgressProperty.bind(optimizer.progressProperty());
                    });

                    final DecimalFormat format = new DecimalFormat("00");
                    Platform.runLater(() ->
                    {
                        estimatedMachiningTimeProperty.bind(Bindings.createStringBinding(() ->
                        {
                            long totalDuration = (long) TimeEstimator.calculateTotalDuration(optimizer.getCurrentBestSolution(),
                                    convertToDouble(settings.getFeedXY().getValue()) / 60, convertToDouble(settings.getFeedZ().getValue()) / 60,
                                    convertToDouble(settings.getFeedXY().getValue()) / 60 * settings.getFeedArcs().getValue() / 100,
                                    convertToDouble(settings.getClearance().getValue()), convertToDouble(settings.getSafetyHeight().getValue()),
                                    true, mergeTolerance);
                            String time = format.format(totalDuration / 3600) + ":" + format.format(totalDuration % 3600 / 60) +
                                    ":" + format.format(totalDuration % 60);
                            return "Estimated machining time: " + time;
                        }, optimizer.currentBestSolutionProperty()));
                    });
                    chains = optimizer.optimize();

                    toolpaths.clear();
                    for (Chain p : chains)
                        toolpaths.addAll(p.getSegments());
                    traceLayer.setToolpaths(toolpaths);

//                    cache.setToolpaths(cacheKey, toolpaths);
//                    cache.setLastModified(layerModificationDate);
//
//                    try
//                    {
//                        ToolpathsPersistor.saveToFile(cache, context.getPcbLayout().getFileName()  + ".tmp");
//                    }
//                    catch (ToolpathPersistingException e)
//                    {
//                        LoggerFactory.getApplicationLogger().log(Level.INFO, e.getMessage(), e);
//                    }

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
        };
    }

    private double convertToDouble(Integer i)
    {
        return i.doubleValue() / ApplicationConstants.RESOLUTION;
    }
}
