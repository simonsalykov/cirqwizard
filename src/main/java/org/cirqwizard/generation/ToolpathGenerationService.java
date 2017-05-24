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
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.layers.*;
import org.cirqwizard.settings.ToolSettings;
import org.cirqwizard.generation.toolpath.Toolpath;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ToolpathGenerationService extends GenerationService
{
    public ToolpathGenerationService(Context context, Board.LayerType layer)
    {
        super(context, layer);
    }

    @Override
    public List<Chain> generate()
    {
        ToolSettings currentTool = getContext().getCurrentMillingTool();
        int diameter = currentTool.getDiameter();

        final VectorToolPathGenerator generator = new VectorToolPathGenerator();
        List<GerberPrimitive> elements = (List<GerberPrimitive>) getContext().getPanel().getCombinedElements(getLayer());
        generator.init(getContext().getPanel().getSize().getWidth() + 1, getContext().getPanel().getSize().getHeight() + 1,
                diameter / 2, diameter, elements, cancelledProperty());
        setCurrentStage("Generating tool paths...");
        progressProperty().bind(generator.progressProperty());

        return generator.generate();

//        if (isCancelled())
//            return null;
//        if (toolpaths == null || toolpaths.size() == 0)
//            return null;
//        toolpaths = new ToolpathMerger(toolpaths, getMergeTolerance()).merge();
//
//        if (currentTool.getAdditionalPasses() > 0)
//        {
//            setCurrentStage("Generating additional passes...");
//
//            if (!currentTool.isAdditionalPassesPadsOnly())
//            {
//                for (int i = 0 ; i < currentTool.getAdditionalPasses(); i++)
//                {
//                    int offset = diameter * (100 - currentTool.getAdditionalPassesOverlap()) / 100;
//                    generator.init(getContext().getPanel().getSize().getWidth() + 1, getContext().getPanel().getSize().getHeight() + 1,
//                            diameter / 2 + offset * (i + 1), diameter, elements,
//                            cancelledProperty());
//                    List<Toolpath> additionalToolpaths = generator.generate();
//                    if (additionalToolpaths == null || additionalToolpaths.size() == 0)
//                        continue;
//                    if (isCancelled())
//                        return null;
//                    toolpaths.addAll(new ToolpathMerger(additionalToolpaths, getMergeTolerance()).merge());
//                }
//            }
//            else
//            {
//                progressProperty().unbind();
//                generatePadsOnlyAdditionalPasses();
//            }
//        }

//        return new ChainDetector(toolpaths).detect();
    }

    private List<Toolpath> generatePadsOnlyAdditionalPasses()
    {
        ToolSettings currentTool = getContext().getCurrentMillingTool();
        List<? extends LayerElement> elements = getContext().getPanel().getBoards().stream().
                map(b -> b.getBoard().getLayer(Board.LayerType.TOP)).
                filter(l -> l != null).
                map(Layer::getElements).
                flatMap(Collection::stream).collect(Collectors.toList());
        AdditionalToolpathGenerator additionalGenerator = new AdditionalToolpathGenerator(getContext().getPanel().getSize().getWidth() + 1,
                getContext().getPanel().getSize().getHeight() + 1, currentTool.getAdditionalPasses(),
                currentTool.getAdditionalPassesOverlap(), currentTool.getDiameter(), (List<GerberPrimitive>) elements);
        progressProperty().unbind();
        progressProperty().bind(additionalGenerator.progressProperty());
        return new ToolpathMerger(additionalGenerator.generate(), getMergeTolerance()).merge();
    }

    private int getMergeTolerance()
    {
        return  getContext().getCurrentMillingTool().getDiameter() / 4;
    }
}
