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
import org.cirqwizard.settings.ToolSettings;
import org.cirqwizard.generation.toolpath.Toolpath;

import java.util.List;

public class ToolpathGenerationService extends GenerationService
{
    public ToolpathGenerationService(Context context, Layer layer)
    {
        super(context, layer);
    }

    @Override
    public List<Chain> generate()
    {
        ToolSettings currentTool = getContext().getCurrentMillingTool();
        int diameter = currentTool.getDiameter();

        final ToolpathGenerator generator = new ToolpathGenerator();
        TraceLayer traceLayer = (TraceLayer) getLayer();
        generator.init(getContext().getBoardWidth() + 1, getContext().getBoardHeight() + 1,
                diameter / 2, diameter, traceLayer.getElements(), cancelledProperty());
        setCurrentStage("Generating tool paths...");
        progressProperty().bind(generator.progressProperty());

        List<Toolpath> toolpaths = generator.generate();

        if (isCancelled())
            return null;
        if (toolpaths == null || toolpaths.size() == 0)
            return null;
        toolpaths = new ToolpathMerger(toolpaths, getMergeTolerance()).merge();

        if (currentTool.getAdditionalPasses() > 0)
        {
            setCurrentStage("Generating additional passes...");

            if (!currentTool.isAdditionalPassesPadsOnly())
            {
                for (int i = 0 ; i < currentTool.getAdditionalPasses(); i++)
                {
                    int offset = diameter * (100 - currentTool.getAdditionalPassesOverlap()) / 100;
                    generator.init(getContext().getBoardWidth() + 1, getContext().getBoardHeight() + 1,
                            diameter / 2 + offset * (i + 1), diameter, traceLayer.getElements(),
                            cancelledProperty());
                    List<Toolpath> additionalToolpaths = generator.generate();
                    if (additionalToolpaths == null || additionalToolpaths.size() == 0)
                        continue;
                    if (isCancelled())
                        return null;
                    toolpaths.addAll(new ToolpathMerger(additionalToolpaths, getMergeTolerance()).merge());
                }
            }
            else
            {
                progressProperty().unbind();
                generatePadsOnlyAdditionalPasses();
            }
        }

        return new ChainDetector(toolpaths).detect();
    }

    private List<Toolpath> generatePadsOnlyAdditionalPasses()
    {
        ToolSettings currentTool = getContext().getCurrentMillingTool();
        TraceLayer traceLayer = (TraceLayer) getLayer();
        AdditionalToolpathGenerator additionalGenerator = new AdditionalToolpathGenerator(getContext().getBoardWidth() + 1,
                getContext().getBoardHeight() + 1, currentTool.getAdditionalPasses(),
                currentTool.getAdditionalPassesOverlap(), currentTool.getDiameter(), traceLayer.getElements());
        progressProperty().unbind();
        progressProperty().bind(additionalGenerator.progressProperty());
        return new ToolpathMerger(additionalGenerator.generate(), getMergeTolerance()).merge();
    }

    private int getMergeTolerance()
    {
        return  getContext().getCurrentMillingTool().getDiameter() / 4;
    }
}
