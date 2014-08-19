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

package org.cirqwizard.fx.rubout;

import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBPaneFX;
import org.cirqwizard.fx.machining.Machining;
import org.cirqwizard.fx.machining.RuboutToolpathGenerationService;
import org.cirqwizard.fx.machining.ToolpathGenerationService;
import org.cirqwizard.gcode.TraceGCodeGenerator;
import org.cirqwizard.layers.Layer;
import org.cirqwizard.layers.TraceLayer;
import org.cirqwizard.post.RTPostprocessor;
import org.cirqwizard.settings.RubOutSettings;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.settings.SettingsGroup;

public class Rubout extends Machining
{
    @Override
    protected String getName()
    {
        return "Rub out";
    }

    @Override
    protected boolean isEnabled()
    {
        Context context = getMainApplication().getContext();
        return InsertTool.EXPECTED_TOOL.equals(context.getInsertedTool()) &&
                context.getG54X() != null && context.getG54Y() != null;
    }

    @Override
    public void refresh()
    {
        pcbPane.setToolpathColor(PCBPaneFX.ENABLED_TOOLPATH_COLOR);
        pcbPane.setGerberPrimitives(((TraceLayer)getCurrentLayer()).getElements());

        super.refresh();
    }

    @Override
    protected ToolpathGenerationService getToolpathGenerationService()
    {
        return new RuboutToolpathGenerationService(getMainApplication(), overallProgressBar.progressProperty(),
                estimatedMachiningTimeProperty, getCurrentLayer(), 0, 0);
    }

    @Override
    protected Layer getCurrentLayer()
    {
        return getMainApplication().getContext().getPcbLayout().getTopTracesLayer();
    }

    @Override
    protected String generateGCode()
    {

        RubOutSettings settings = SettingsFactory.getRubOutSettings();
        int arcFeed = (settings.getFeedXY().getValue() * settings.getFeedArcs().getValue() / 100);
        TraceGCodeGenerator generator = new TraceGCodeGenerator(getMainApplication().getContext(), getCurrentLayer().getToolpaths(), false);
        return generator.generate(new RTPostprocessor(), settings.getFeedXY().getValue(), settings.getFeedZ().getValue(), arcFeed,
                settings.getClearance().getValue(), settings.getSafetyHeight().getValue(), settings.getWorkingHeight().getValue(),
                settings.getSpeed().getValue());
    }

    @Override
    public SettingsGroup getSettingsGroup()
    {
        return SettingsFactory.getRubOutSettings();
    }
}
