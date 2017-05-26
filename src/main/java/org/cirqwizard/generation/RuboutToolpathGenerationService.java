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
        elements.addAll(createPinKeepout());
        VectorRuboutGenerator generator =  new VectorRuboutGenerator(getContext().getPanel().getSize().getWidth(),
                getContext().getPanel().getSize().getHeight(), elements, diameter, cancelledProperty(),
                settings.getInitialOffset().getValue());
        return generator.generate();
    }

    private List<GerberPrimitive> createPinKeepout()
    {
        Aperture aperture = new CircularAperture(5000);
        return Arrays.stream(getContext().getPanel().getPinLocations()).map(p -> new Flash(p.getX(), p.getY(),
                aperture, GerberPrimitive.Polarity.DARK)).collect(Collectors.toList());
    }
}
