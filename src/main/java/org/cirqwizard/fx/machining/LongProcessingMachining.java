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
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import org.cirqwizard.generation.GenerationService;
import org.cirqwizard.generation.ProcessingService;
import org.cirqwizard.generation.optimizer.Chain;
import org.cirqwizard.generation.optimizer.OptimizationService;
import org.cirqwizard.generation.toolpath.*;
import org.cirqwizard.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public abstract class LongProcessingMachining extends Machining
{
    @FXML protected VBox generationPane;

    @FXML protected Label generationStageLabel;
    @FXML protected ProgressBar overallProgressBar;
    @FXML protected Label machiningTimeEstimationLabel;
    @FXML protected Button stopGenerationButton;

    private boolean generationCancelled;
    private ProcessingService boundService;

    private Service<Void> longProcessingService = new Service<Void>()
    {
        @Override
        protected Task<Void> createTask()
        {
            return new Task<Void>()
            {
                @Override
                protected Void call() throws Exception
                {
                    try
                    {
                        List<Toolpath> toolpaths = new ArrayList<>();

                        cacheKey = getCacheKey();
                        GenerationService generationService = getGenerationService();
                        bindToService(generationService);
                        List<Chain> chains = generationService.generate();
                        if (generationCancelled)
                        {
                            cacheKey = null;
                            return null;
                        }

                        OptimizationService optimizationService = getOptimizationService(chains);
                        bindToService(optimizationService);
                        chains = optimizationService.optimize();

                        for (Chain p : chains)
                            toolpaths.addAll(p.getSegments());
//                        updateCache(toolpaths);
                        getMainApplication().getContext().getPanel().setToolpaths(getCurrentLayer(), toolpaths);
                        Platform.runLater(() -> pcbPane.toolpathsProperty().setValue(
                                FXCollections.observableArrayList(toolpaths)));
                    }
                    catch (Throwable th)
                    {
                        LoggerFactory.getApplicationLogger().log(Level.WARNING, "Exception caught while generating tool paths", th);
                    }

                    return null;
                }
            };
        }
    };

    protected ToolpathsCacheKey cacheKey;

    protected abstract ToolpathsCacheKey getCacheKey();
    protected abstract GenerationService getGenerationService();
    protected abstract OptimizationService getOptimizationService(List<Chain> chains);
    protected abstract int getMergeTolerance();
    protected abstract long getLayerModificationDate();

    @Override
    public void refresh()
    {
        stopGenerationButton.setDisable(false);
        if (getMainApplication().getCNCController() != null)
            goButton.disableProperty().bind(longProcessingService.runningProperty());
        generationPane.visibleProperty().bind(longProcessingService.runningProperty());
        veil.visibleProperty().bind(longProcessingService.runningProperty());
        cacheKey = null;
        super.refresh();
    }

    public boolean needsRestart()
    {
        return cacheKey == null || !cacheKey.equals(getCacheKey());
    }


    private boolean loadFromCache()
    {
        return false;
/*        try
        {
            ToolpathsCache cache = ToolpathsPersistor.loadFromFile(getMainApplication().getContext().getPcbLayout().getFileName() + ".tmp");
            if (cache == null || !cache.hasValidData(getMainApplication().getContext().getPcbLayout().getFile().lastModified()))
                return false;
            List<Toolpath> toolpaths = cache.getToolpaths(getCacheKey());
            if (toolpaths != null)
            {
                cacheKey = getCacheKey();
                ((TraceLayer)getCurrentLayer()).setToolpaths(toolpaths);
                pcbPane.toolpathsProperty().setValue(FXCollections.observableArrayList(toolpaths));
                return true;
            }
        }
        catch (ToolpathPersistingException e)
        {
            LoggerFactory.getApplicationLogger().log(Level.INFO, e.getMessage(), e);
        }
        return false;*/
    }

    private void updateCache(List<Toolpath> toolpaths)
    {
        try
        {
            String filename = getMainApplication().getContext().getPanelFile().getAbsolutePath();
            filename = filename.substring(0, filename.lastIndexOf('.'));
            ToolpathsCache cache = ToolpathsPersistor.loadFromFile(filename + ".tmp");
            if (cache == null)
                cache = new ToolpathsCache();
            cache.setToolpaths(getCacheKey(), toolpaths);
            cache.setLastModified(getLayerModificationDate());
            ToolpathsPersistor.saveToFile(cache, filename  + ".tmp");
        }
        catch (ToolpathPersistingException e)
        {
            LoggerFactory.getApplicationLogger().log(Level.INFO, e.getMessage(), e);
        }

    }

    protected void bindToService(ProcessingService service)
    {
        this.boundService = service;
        Platform.runLater(() ->
        {
            overallProgressBar.progressProperty().unbind();
            generationStageLabel.textProperty().unbind();
            machiningTimeEstimationLabel.textProperty().unbind();
            overallProgressBar.progressProperty().bind(service.progressProperty());
            generationStageLabel.textProperty().bind(service.currentStageProperty());
            machiningTimeEstimationLabel.textProperty().bind(service.additionalInformationProperty());
        });
    }

    @Override
    protected void generateToolpaths()
    {
        if (!needsRestart())
            return;
        stopGeneration();
        if (loadFromCache())
            return;

        veil.visibleProperty().bind(longProcessingService.runningProperty());
        stopGenerationButton.setDisable(false);
        generationCancelled = false;
        longProcessingService.restart();
    }

    @Override
    public void stopGeneration()
    {
        stopGenerationButton.setDisable(true);
        generationCancelled = true;
        if (boundService != null)
            boundService.setCancelled(true);
    }

}
