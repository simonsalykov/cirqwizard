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

public class RuboutToolpathGenerationService extends MillingToolpathGenerationService
{
    public RuboutToolpathGenerationService(MainApplication mainApplication, DoubleProperty overallProgressProperty,
                                           StringProperty estimatedMachiningTimeProperty,
                                           Layer layer, int cacheLayerId, long layerModificationDate)
    {
        super(mainApplication, overallProgressProperty, estimatedMachiningTimeProperty, layer, cacheLayerId, layerModificationDate);
    }

    @Override
    protected ToolpathsCacheKey getCacheKey()
    {
        RubOutSettings settings = SettingsFactory.getRubOutSettings();
        return new ToolpathsCacheKey(cacheLayerId, context.getPcbLayout().getAngle(), settings.getToolDiameter().getValue(), 0,
                            0, false, settings.getInitialOffset().getValue(), settings.getOverlap().getValue());
    }

    @Override
    protected List<Chain> generate()
    {
        RubOutSettings settings = SettingsFactory.getRubOutSettings();
        int diameter = settings.getToolDiameter().getValue();
        TraceLayer traceLayer = (TraceLayer) layer;

        List<Toolpath> toolpaths = new ArrayList<>();
        for (int pass = 0; pass < 2; pass++)
        {
            if (serviceStateProperty.getValue() == State.CANCELLED)
                return null;
            ToolpathGenerator g = new ToolpathGenerator();
            g.init(mainApplication.getContext().getBoardWidth() + 1, mainApplication.getContext().getBoardHeight() + 1,
                    pass * (diameter - settings.getOverlap().getValue()) + settings.getInitialOffset().getValue() + diameter / 2, diameter, traceLayer.getElements(),
                    SettingsFactory.getApplicationSettings().getProcessingThreads().getValue(), serviceStateProperty);
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
        if (serviceStateProperty.getValue() == State.CANCELLED)
            return null;

        final RubOutToolpathGenerator generator = new RubOutToolpathGenerator();
        generator.init(mainApplication.getContext().getBoardWidth() + 1, mainApplication.getContext().getBoardHeight() + 1,
                settings.getInitialOffset().getValue(),
                diameter, settings.getOverlap().getValue(), traceLayer.getElements(),
                SettingsFactory.getApplicationSettings().getProcessingThreads().getValue(), serviceStateProperty);
        Platform.runLater(() ->
        {
            generationStageProperty.setValue("Generating tool paths...");
            overallProgressProperty.bind(generator.progressProperty());
            estimatedMachiningTimeProperty.setValue("");
        });

        List<Toolpath> t = generator.generate();
        if (serviceStateProperty.getValue() == State.CANCELLED)
            return null;
        final int mergeTolerance = diameter / 10;
        if (t != null && t.size() > 0)
            toolpaths.addAll(new ToolpathMerger(t, mergeTolerance).merge());

        return new ChainDetector(toolpaths).detect();
    }

    @Override
    protected int getMergeTolerance()
    {
        return SettingsFactory.getRubOutSettings().getToolDiameter().getValue() / 10;
    }

}
