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

import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBPaneFX;
import org.cirqwizard.fx.machining.ContourMillingToolpathGenerationService;
import org.cirqwizard.fx.machining.Machining;
import org.cirqwizard.fx.machining.ToolpathGenerationService;
import org.cirqwizard.gcode.MillingGCodeGenerator;
import org.cirqwizard.layers.Layer;
import org.cirqwizard.post.RTPostprocessor;
import org.cirqwizard.settings.ContourMillingSettings;
import org.cirqwizard.settings.SettingsFactory;

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
        toolDiameter.setDisable(true);
        toolDiameter.setText(context.getPcbLayout().getContourMillDiameter());
        ContourMillingSettings settings = SettingsFactory.getContourMillingSettings();
        feed.setIntegerValue(settings.getFeedXY().getValue());

        clearance.setIntegerValue(settings.getClearance().getValue());
        safetyHeight.setIntegerValue(settings.getSafetyHeight().getValue());
        zFeed.setDisable(false);
        zFeed.setIntegerValue(settings.getFeedZ().getValue());

        context.setG54Z(settings.getZOffset().getValue());

        pcbPane.setGerberPrimitives(null);
        pcbPane.setGerberColor(PCBPaneFX.CONTOUR_COLOR);
        pcbPane.setToolpathColor(PCBPaneFX.CONTOUR_COLOR);

        toolpathGenerationService.arcFeedProperty().set(feed.getIntegerValue() * settings.getFeedArcs().getValue() / 100);
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
        int arcFeed = (feed.getIntegerValue() * settings.getFeedArcs().getValue() / 100);
        MillingGCodeGenerator generator = new MillingGCodeGenerator(getMainApplication().getContext());
        return generator.generate(new RTPostprocessor(), feed.getIntegerValue(), zFeed.getIntegerValue(), arcFeed,
                clearance.getIntegerValue(), safetyHeight.getIntegerValue(), settings.getWorkingHeight().getValue(),
                settings.getSpeed().getValue());
    }
}
