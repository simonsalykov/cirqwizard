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
import javafx.beans.property.ReadOnlyObjectProperty;
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
    protected ReadOnlyObjectProperty<State> serviceStateProperty;

    protected Layer layer;
    protected int cacheLayerId;
    protected long layerModificationDate;
    protected ToolpathsCacheKey cacheKey;

    public MillingToolpathGenerationService(MainApplication mainApplication, DoubleProperty overallProgressProperty,
                                                 StringProperty estimatedMachiningTimeProperty,
                                                 Layer layer, int cacheLayerId, long layerModificationDate)
    {
        super(mainApplication, overallProgressProperty, estimatedMachiningTimeProperty);
        this.layer = layer;
        this.cacheLayerId = cacheLayerId;
        this.layerModificationDate = layerModificationDate;
        this.serviceStateProperty = stateProperty();
    }

    protected abstract ToolpathsCacheKey getCacheKey();
    protected abstract List<Chain> generate();
    protected abstract int getMergeTolerance();

    @Override
    public boolean needsRestart()
    {
        return cacheKey == null || !cacheKey.equals(getCacheKey());
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
                    if (!needsRestart())
                        return null;

                    overallProgressProperty.unbind();
                    generationStageProperty.unbind();
                    estimatedMachiningTimeProperty.unbind();

                    cacheKey = getCacheKey();
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
                    if (serviceStateProperty.getValue() == State.CANCELLED)
                        return null;

                    InsulationMillingSettings settings = SettingsFactory.getInsulationMillingSettings();
                    final Optimizer optimizer = new Optimizer(chains, convertToDouble(settings.getFeedXY().getValue()) / 60, convertToDouble(settings.getFeedZ().getValue()) / 60,
                            convertToDouble(settings.getFeedXY().getValue()) / 60 * settings.getFeedArcs().getValue() / 100,
                            convertToDouble(settings.getClearance().getValue()), convertToDouble(settings.getSafetyHeight().getValue()), getMergeTolerance(),
                            serviceStateProperty);
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
                    if (serviceStateProperty.getValue() == State.CANCELLED)
                        return null;

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
