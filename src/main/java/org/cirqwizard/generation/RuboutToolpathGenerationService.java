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
import org.cirqwizard.generation.toolpath.Toolpath;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.gerber.appertures.Aperture;
import org.cirqwizard.gerber.appertures.CircularAperture;
import org.cirqwizard.layers.Board;
import org.cirqwizard.settings.RubOutSettings;
import org.cirqwizard.settings.SettingsFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RuboutToolpathGenerationService extends GenerationService
{
    public RuboutToolpathGenerationService(Context context, Board.LayerType layer)
    {
        super(context, layer);
    }

    @Override
    public List<Chain> generate()
    {
        RubOutSettings settings = SettingsFactory.getRubOutSettings();
        int diameter = settings.getToolDiameter().getValue();
        List<GerberPrimitive> elements = (List<GerberPrimitive>) getContext().getPanel().getCombinedElements(getLayer());

        List<Toolpath> toolpaths = new ArrayList<>();
        for (int pass = 0; pass < 2; pass++)
        {
            if (cancelledProperty().get())
                return null;
            ToolpathGenerator g = new ToolpathGenerator();
            g.init(getContext().getPanel().getSize().getWidth() + 1, getContext().getPanel().getSize().getHeight() + 1,
                    pass * (diameter - settings.getOverlap().getValue()) + settings.getInitialOffset().getValue() + diameter / 2, diameter, elements,
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

        elements.addAll(createPinKeepout());
        getContext().getPanel().getBoards().forEach(board ->
        {
            final RubOutToolpathGenerator generator = new RubOutToolpathGenerator();
            progressProperty().unbind();
            progressProperty().bind(generator.progressProperty());
            generator.init(board.getX(), board.getY(), board.getBoard().getWidth() + 1, board.getBoard().getHeight() + 1,
                    settings.getInitialOffset().getValue(),
                    diameter, settings.getOverlap().getValue(), elements,
                    SettingsFactory.getApplicationSettings().getProcessingThreads().getValue(), cancelledProperty());

            List<Toolpath> t = generator.generate();
            if (cancelledProperty().get())
                return;
            final int mergeTolerance = diameter / 10;
            if (t != null && t.size() > 0)
                toolpaths.addAll(new ToolpathMerger(t, mergeTolerance).merge());
        });

        if (cancelledProperty().get())
            return null;
        return new ChainDetector(toolpaths).detect();
    }

    private List<GerberPrimitive> createPinKeepout()
    {
        Aperture aperture = new CircularAperture(5000);
        return Arrays.stream(getContext().getPanel().getPinLocations()).map(p -> new Flash(p.getX(), p.getY(),
                aperture, GerberPrimitive.Polarity.DARK)).collect(Collectors.toList());
    }
}
