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
package org.cirqwizard.generation;

import org.cirqwizard.fx.Context;
import org.cirqwizard.generation.optimizer.Chain;
import org.cirqwizard.generation.optimizer.ChainDetector;
import org.cirqwizard.layers.Layer;
import org.cirqwizard.layers.TraceLayer;
import org.cirqwizard.settings.RubOutSettings;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.generation.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.List;

public class RuboutToolpathGenerationService extends GenerationService
{
    public RuboutToolpathGenerationService(Context context, Layer layer)
    {
        super(context, layer);
    }

    @Override
    public List<Chain> generate()
    {
        RubOutSettings settings = SettingsFactory.getRubOutSettings();
        int diameter = settings.getToolDiameter().getValue();
        TraceLayer traceLayer = (TraceLayer) getLayer();

        List<Toolpath> toolpaths = new ArrayList<>();
        for (int pass = 0; pass < 2; pass++)
        {
            if (cancelledProperty().get())
                return null;
            ToolpathGenerator g = new ToolpathGenerator();
            g.init(getContext().getBoardWidth() + 1, getContext().getBoardHeight() + 1,
                    pass * (diameter - settings.getOverlap().getValue()) + settings.getInitialOffset().getValue() + diameter / 2, diameter, traceLayer.getElements(),
                    cancelledProperty());
            setCurrentStage("Generating tool paths...");
            progressProperty().bind(g.progressProperty());

            List<Toolpath> t = g.generate();
            if (t == null || t.size() == 0)
                continue;
            toolpaths.addAll(new ToolpathMerger(t, diameter / 4).merge());
        }
        if (cancelledProperty().get())
            return null;

        final RubOutToolpathGenerator generator = new RubOutToolpathGenerator();
        generator.init(getContext().getBoardWidth() + 1, getContext().getBoardHeight() + 1,
                settings.getInitialOffset().getValue(),
                diameter, settings.getOverlap().getValue(), traceLayer.getElements(),
                SettingsFactory.getApplicationSettings().getProcessingThreads().getValue(), cancelledProperty());
        progressProperty().unbind();
        progressProperty().bind(generator.progressProperty());

        List<Toolpath> t = generator.generate();
        if (cancelledProperty().get())
            return null;
        final int mergeTolerance = diameter / 10;
        if (t != null && t.size() > 0)
            toolpaths.addAll(new ToolpathMerger(t, mergeTolerance).merge());

        return new ChainDetector(toolpaths).detect();
    }
}
