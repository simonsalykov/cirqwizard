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

package org.cirqwizard.fx.drilling;

import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBPaneFX;
import org.cirqwizard.fx.machining.DrillingToolpathGenerationService;
import org.cirqwizard.fx.machining.Machining;
import org.cirqwizard.fx.machining.ToolpathGenerationService;
import org.cirqwizard.layers.Layer;
import org.cirqwizard.settings.ApplicationConstants;
import org.cirqwizard.settings.DrillingSettings;
import org.cirqwizard.settings.SettingsFactory;

public class Drilling extends Machining
{
    @Override
    protected String getName()
    {
        return "Drilling";
    }

    @Override
    public void refresh()
    {
        super.refresh();
        toolDiameter.setDisable(true);
        Context context = getMainApplication().getContext();
        toolDiameter.setText(ApplicationConstants.formatToolDiameter(context.getCurrentDrill()));
        DrillingSettings settings = SettingsFactory.getDrillingSettings();
        feed.setIntegerValue(settings.getFeed().getValue());

        clearance.setIntegerValue(settings.getClearance().getValue());
        safetyHeight.setIntegerValue(settings.getSafetyHeight().getValue());
        zFeed.setDisable(true);

        context.setG54Z(settings.getZOffset().getValue());

        pcbPane.setGerberColor(PCBPaneFX.DRILL_POINT_COLOR);
        pcbPane.setToolpathColor(PCBPaneFX.DRILL_POINT_COLOR);
        pcbPane.setGerberPrimitives(null);
    }

    @Override
    protected Layer getCurrentLayer()
    {
        return getMainApplication().getContext().getPcbLayout().getDrillingLayer();
    }

    @Override
    protected ToolpathGenerationService getToolpathGenerationService()
    {
        return new DrillingToolpathGenerationService(getMainApplication(), overallProgressBar.progressProperty(),
                estimatedMachiningTimeProperty);
    }
}
