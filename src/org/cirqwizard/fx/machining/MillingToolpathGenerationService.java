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
import org.cirqwizard.generation.optimizer.Chain;
import org.cirqwizard.generation.optimizer.Optimizer;
import org.cirqwizard.generation.optimizer.TimeEstimator;
import org.cirqwizard.layers.Layer;
import org.cirqwizard.layers.TraceLayer;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.ApplicationConstants;
import org.cirqwizard.settings.InsulationMillingSettings;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.toolpath.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public abstract class MillingToolpathGenerationService extends ToolpathGenerationService
{
    protected Layer layer;
    protected int cacheLayerId;
    protected long layerModificationDate;
    protected GenerationKey generationKey;

    public MillingToolpathGenerationService(MainApplication mainApplication, DoubleProperty overallProgressProperty,
                                                 StringProperty estimatedMachiningTimeProperty,
                                                 Layer layer, int cacheLayerId, long layerModificationDate)
    {
        super(mainApplication, overallProgressProperty, estimatedMachiningTimeProperty);
        this.layer = layer;
        this.cacheLayerId = cacheLayerId;
        this.layerModificationDate = layerModificationDate;
    }

    protected class GenerationKey
    {
        private int toolDiameter;
        private int additionalToolpaths;
        private int additionalToolpathsOverlap;
        private boolean additionalToolpathsAroundPadsOnly;

        public GenerationKey(int toolDiameter, int additionalToolpaths, int additionalToolpathsOverlap, boolean additionalToolpathsAroundPadsOnly)
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

    protected abstract GenerationKey getGenerationKey();
    protected abstract ToolpathsCacheKey getCacheKey();
    protected abstract List<Chain> generate();
    protected abstract int getMergeTolerance();

    @Override
    public boolean needsRestart()
    {
        return generationKey == null || !generationKey.equals(getGenerationKey());
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
                    InsulationMillingSettings settings = SettingsFactory.getInsulationMillingSettings();
                    GenerationKey newKey = getGenerationKey();
                    if (generationKey != null && generationKey.equals(newKey))
                        return null;
                    generationKey = newKey;

                    overallProgressProperty.unbind();
                    generationStageProperty.unbind();
                    estimatedMachiningTimeProperty.unbind();

                    ToolpathsCacheKey cacheKey = getCacheKey();
                    ToolpathsCache cache = null;
                    try
                    {
                        cache = ToolpathsPersistor.loadFromFile(context.getPcbLayout().getFileName() + ".tmp");
                    }
                    catch (ToolpathPersistingException e)
                    {
                        LoggerFactory.getApplicationLogger().log(Level.INFO, e.getMessage(), e);
                    }

                    TraceLayer traceLayer = (TraceLayer) layer;
                    if (cache != null && cache.hasValidData(context.getPcbLayout().getFile().lastModified()))
                    {
                        if (cache.getToolpaths(cacheKey) != null)
                        {
                            traceLayer.setToolpaths(cache.getToolpaths(cacheKey));
                            return FXCollections.observableArrayList(cache.getToolpaths(cacheKey));
                        }
                    }
                    else
                        cache = new ToolpathsCache();

                    List<Chain> chains = generate();

                    final Optimizer optimizer = new Optimizer(chains, convertToDouble(settings.getFeedXY().getValue()) / 60, convertToDouble(settings.getFeedZ().getValue()) / 60,
                            convertToDouble(settings.getFeedXY().getValue()) / 60 * settings.getFeedArcs().getValue() / 100,
                            convertToDouble(settings.getClearance().getValue()), convertToDouble(settings.getSafetyHeight().getValue()), getMergeTolerance());
                    Platform.runLater(() ->
                    {
                        generationStageProperty.setValue("Optimizing milling time...");
                        overallProgressProperty.unbind();
                        overallProgressProperty.bind(optimizer.progressProperty());
                    });

                    final DecimalFormat format = new DecimalFormat("00");
                    Platform.runLater(() ->
                        estimatedMachiningTimeProperty.bind(Bindings.createStringBinding(() ->
                        {
                            long totalDuration = (long) TimeEstimator.calculateTotalDuration(optimizer.getCurrentBestSolution(),
                                    convertToDouble(settings.getFeedXY().getValue()) / 60, convertToDouble(settings.getFeedZ().getValue()) / 60,
                                    convertToDouble(settings.getFeedXY().getValue()) / 60 * settings.getFeedArcs().getValue() / 100,
                                    convertToDouble(settings.getClearance().getValue()), convertToDouble(settings.getSafetyHeight().getValue()),
                                    true, getMergeTolerance());
                            String time = format.format(totalDuration / 3600) + ":" + format.format(totalDuration % 3600 / 60) +
                                    ":" + format.format(totalDuration % 60);
                            return "Estimated machining time: " + time;
                        }, optimizer.currentBestSolutionProperty()))
                    );
                    chains = optimizer.optimize();

                    List<Toolpath> toolpaths = new ArrayList<>();
                    for (Chain p : chains)
                        toolpaths.addAll(p.getSegments());
                    traceLayer.setToolpaths(toolpaths);

                    cache.setToolpaths(cacheKey, toolpaths);
                    cache.setLastModified(layerModificationDate);

                    try
                    {
                        ToolpathsPersistor.saveToFile(cache, context.getPcbLayout().getFileName()  + ".tmp");
                    }
                    catch (ToolpathPersistingException e)
                    {
                        LoggerFactory.getApplicationLogger().log(Level.INFO, e.getMessage(), e);
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
        };
    }

    private double convertToDouble(Integer i)
    {
        return i.doubleValue() / ApplicationConstants.RESOLUTION;
    }
}
