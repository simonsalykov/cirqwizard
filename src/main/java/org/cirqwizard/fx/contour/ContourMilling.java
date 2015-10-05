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

package org.cirqwizard.fx.contour;

import javafx.scene.layout.GridPane;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBPaneFX;
import org.cirqwizard.fx.SettingsDependentScreenController;
import org.cirqwizard.fx.machining.ContourMillingToolpathGenerationService;
import org.cirqwizard.fx.machining.Machining;
import org.cirqwizard.fx.machining.ToolpathGenerationService;
import org.cirqwizard.fx.settings.SettingsEditor;
import org.cirqwizard.gcode.MillingGCodeGenerator;
import org.cirqwizard.layers.Layer;
import org.cirqwizard.post.RTPostprocessor;
import org.cirqwizard.settings.ContourMillingSettings;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.settings.SettingsGroup;

public class ContourMilling extends Machining
{
    @Override
    protected String getName()
    {
        return "Milling";
    }

    @Override
    public void refresh()
    {
        super.refresh();
        Context context = getMainApplication().getContext();
        ContourMillingSettings settings = SettingsFactory.getContourMillingSettings();
        context.setG54Z(settings.getZOffset().getValue());

        pcbPane.setGerberPrimitives(null);
        pcbPane.setGerberColor(PCBPaneFX.CONTOUR_COLOR);
        pcbPane.setToolpathColor(PCBPaneFX.CONTOUR_COLOR);
    }

    @Override
    public void populateSettingsGroup(GridPane pane, SettingsDependentScreenController listener)
    {
        SettingsEditor.renderSettings(pane, SettingsFactory.getContourMillingSettings(), getMainApplication(), listener);
    }

    @Override
    protected Layer getCurrentLayer()
    {
        return getMainApplication().getContext().getPcbLayout().getMillingLayer();
    }

    @Override
    protected ToolpathGenerationService getToolpathGenerationService()
    {
        return new ContourMillingToolpathGenerationService(getMainApplication(), overallProgressBar.progressProperty(),
                estimatedMachiningTimeProperty);
    }

    @Override
    protected String generateGCode()
    {
        ContourMillingSettings settings = SettingsFactory.getContourMillingSettings();
        int arcFeed = (settings.getFeedXY().getValue() * settings.getFeedArcs().getValue() / 100);
        MillingGCodeGenerator generator = new MillingGCodeGenerator(getMainApplication().getContext());
        return generator.generate(new RTPostprocessor(), settings.getFeedXY().getValue(), settings.getFeedZ().getValue(), arcFeed,
                settings.getClearance().getValue(), settings.getSafetyHeight().getValue(), settings.getWorkingHeight().getValue(),
                settings.getSpeed().getValue());
    }
}
