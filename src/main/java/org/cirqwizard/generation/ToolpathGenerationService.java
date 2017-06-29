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
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.layers.Board;
import org.cirqwizard.settings.ToolSettings;

import java.util.List;

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

        List<GerberPrimitive> elements = (List<GerberPrimitive>) getContext().getPanel().getCombinedElements(getLayer());
        final VectorToolPathGenerator generator = new VectorToolPathGenerator(diameter / 2, diameter, elements,
                cancelledProperty(), currentTool.getAdditionalPasses(), currentTool.getAdditionalPassesOverlap());
        setCurrentStage("Generating tool paths...");
        progressProperty().bind(generator.progressProperty());

        return generator.generate();
    }
}
