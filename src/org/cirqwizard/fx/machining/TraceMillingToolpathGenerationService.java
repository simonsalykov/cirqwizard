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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Worker;
import org.cirqwizard.fx.MainApplication;
import org.cirqwizard.generation.AdditionalToolpathGenerator;
import org.cirqwizard.generation.ToolpathGenerator;
import org.cirqwizard.generation.ToolpathMerger;
import org.cirqwizard.generation.optimizer.Chain;
import org.cirqwizard.generation.optimizer.ChainDetector;
import org.cirqwizard.layers.Layer;
import org.cirqwizard.layers.TraceLayer;
import org.cirqwizard.settings.ApplicationSettings;
import org.cirqwizard.settings.InsulationMillingSettings;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.toolpath.Toolpath;
import org.cirqwizard.toolpath.ToolpathsCacheKey;

import java.util.List;

public class TraceMillingToolpathGenerationService extends MillingToolpathGenerationService
{

    public TraceMillingToolpathGenerationService(MainApplication mainApplication, DoubleProperty overallProgressProperty,
                                                 StringProperty estimatedMachiningTimeProperty,
                                                 Layer layer, int cacheLayerId, long layerModificationDate)
    {
        super(mainApplication, overallProgressProperty, estimatedMachiningTimeProperty, layer, cacheLayerId, layerModificationDate);
    }

    @Override
    protected ToolpathsCacheKey getCacheKey()
    {
        InsulationMillingSettings settings = SettingsFactory.getInsulationMillingSettings();
        return new ToolpathsCacheKey(cacheLayerId, context.getPcbLayout().getAngle(), settings.getToolDiameter().getValue(),
                settings.getAdditionalPasses().getValue(), settings.getAdditionalPassesOverlap().getValue(), settings.getAdditionalPassesPadsOnly().getValue(), 0, 0);
    }

    @Override
    protected int getMergeTolerance()
    {
        return SettingsFactory.getInsulationMillingSettings().getToolDiameter().getValue() / 4;
    }

    @Override
    protected List<Chain> generate()
    {
        InsulationMillingSettings settings = SettingsFactory.getInsulationMillingSettings();
        int diameter = settings.getToolDiameter().getValue();

        final ToolpathGenerator generator = new ToolpathGenerator();
        ApplicationSettings applicationSettings = SettingsFactory.getApplicationSettings();
        TraceLayer traceLayer = (TraceLayer) layer;
        generator.init(mainApplication.getContext().getBoardWidth() + 1, mainApplication.getContext().getBoardHeight() + 1,
                diameter / 2, diameter, traceLayer.getElements(), applicationSettings.getProcessingThreads().getValue(), serviceStateProperty);
        Platform.runLater(() ->
        {
            generationStageProperty.setValue("Generating tool paths...");
            overallProgressProperty.bind(generator.progressProperty());
            estimatedMachiningTimeProperty.setValue("");
        });

        List<Toolpath> toolpaths = generator.generate();
        if (serviceStateProperty.getValue() == State.CANCELLED)
            return null;
        if (toolpaths == null || toolpaths.size() == 0)
            return null;
        final int mergeTolerance = getMergeTolerance();
        toolpaths = new ToolpathMerger(toolpaths, mergeTolerance).merge();

        if (!settings.getAdditionalPassesPadsOnly().getValue())
        {
            Platform.runLater(() -> generationStageProperty.setValue("Generating additional passes..."));
            for (int i = 0 ; i < settings.getAdditionalPasses().getValue(); i++)
            {
                int offset = diameter * (100 - settings.getAdditionalPassesOverlap().getValue()) / 100;
                generator.init(mainApplication.getContext().getBoardWidth() + 1, mainApplication.getContext().getBoardHeight() + 1,
                        diameter / 2 + offset * (i + 1), diameter, traceLayer.getElements(), applicationSettings.getProcessingThreads().getValue(),
                        serviceStateProperty);
                List<Toolpath> additionalToolpaths = generator.generate();
                if (additionalToolpaths == null || additionalToolpaths.size() == 0)
                    continue;
                if (serviceStateProperty.getValue() == State.CANCELLED)
                    return null;
                toolpaths.addAll(new ToolpathMerger(additionalToolpaths, mergeTolerance).merge());
            }
        }
        else if (settings.getAdditionalPasses().getValue() > 0)
        {
            final AdditionalToolpathGenerator additionalGenerator = new AdditionalToolpathGenerator(mainApplication.getContext().getBoardWidth() + 1,
                    mainApplication.getContext().getBoardHeight() + 1, settings.getAdditionalPasses().getValue(),
                    settings.getAdditionalPassesOverlap().getValue(), diameter, applicationSettings.getProcessingThreads().getValue(), traceLayer.getElements());
            Platform.runLater(() ->
            {
                generationStageProperty.setValue("Generating additional passes...");
                overallProgressProperty.bind(additionalGenerator.progressProperty());
            });
            toolpaths.addAll(new ToolpathMerger(additionalGenerator.generate(), mergeTolerance).merge());
            if (serviceStateProperty.getValue() == State.CANCELLED)
                return null;
        }


        return new ChainDetector(toolpaths).detect();
    }

}
