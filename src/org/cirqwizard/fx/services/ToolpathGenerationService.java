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
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.MainApplication;
import org.cirqwizard.generation.ToolpathGenerator;
import org.cirqwizard.generation.ToolpathMerger;
import org.cirqwizard.layers.*;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.generation.optimizer.OptimizerGraph;
import org.cirqwizard.generation.optimizer.Path;
import org.cirqwizard.settings.Settings;
import org.cirqwizard.toolpath.DrillPoint;
import org.cirqwizard.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


public class ToolpathGenerationService extends Service<ObservableList<Toolpath>>
{
    private Property<String> toolDiameter = new SimpleObjectProperty<>();
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
                Layer layer = getLayer();
                if (layer instanceof TraceLayer)
                {
                    int diameter = (int)(Double.valueOf(toolDiameter.getValue()) * Settings.RESOLUTION);
                    ToolpathGenerator generator = new ToolpathGenerator(mainApplication.getContext().getBoardWidth() + 1, mainApplication.getContext().getBoardHeight() + 1,
                            diameter / 2, diameter, ((TraceLayer) layer).getElements());

                    long t = System.currentTimeMillis();
                    Platform.runLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            generationStageProperty.setValue("Generating tool paths...");
                        }
                    });
                    overallProgressProperty.bind(generator.progressProperty());
                    estimatedMachiningTimeProperty.setValue("");
                    TraceLayer traceLayer = (TraceLayer) layer;
                    List<Toolpath> toolpaths = generator.generate();

                    toolpaths = new ToolpathMerger(toolpaths).merge();

                    final OptimizerGraph optimizer = new OptimizerGraph(toolpaths);
                    Platform.runLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            generationStageProperty.setValue("Optimizing milling time...");
                            overallProgressProperty.unbind();
                            overallProgressProperty.bind(optimizer.progressProperty());
                            estimatedMachiningTimeProperty.bind(optimizer.estimatedMachiningTimeProperty());
                        }
                    });
                    List<Path> optimized = optimizer.optimize();
                    toolpaths.clear();
                    for (Path p : optimized)
                        toolpaths.addAll(p.getSegments());
                    traceLayer.setToolpaths(toolpaths);

                    t = System.currentTimeMillis() - t;
                    System.out.println("generation time: " + t);
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
