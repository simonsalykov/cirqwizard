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

package org.cirqwizard.fx.traces;

import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBPaneFX;
import org.cirqwizard.fx.machining.Machining;
import org.cirqwizard.fx.machining.ToolpathGenerationService;
import org.cirqwizard.fx.machining.TraceMillingToolpathGenerationService;
import org.cirqwizard.gcode.TraceGCodeGenerator;
import org.cirqwizard.layers.TraceLayer;
import org.cirqwizard.post.RTPostprocessor;
import org.cirqwizard.settings.InsulationMillingSettings;
import org.cirqwizard.settings.SettingsFactory;

public abstract class TraceMilling extends Machining
{
    @Override
    protected String getName()
    {
        return "Milling";
    }

    @Override
    protected boolean isEnabled()
    {
        Context context = getMainApplication().getContext();
        return InsertTool.EXPECTED_TOOL.equals(context.getInsertedTool()) &&
                context.getG54X() != null && context.getG54Y() != null && context.getG54Z() != null;
    }

    @Override
    public void refresh()
    {
        toolDiameter.setDisable(false);
        InsulationMillingSettings settings = SettingsFactory.getInsulationMillingSettings();
        toolDiameter.setIntegerValue(settings.getToolDiameter().getValue());
        feed.setIntegerValue(SettingsFactory.getInsulationMillingSettings().getFeedXY().getValue());

        clearance.setIntegerValue(settings.getClearance().getValue());
        safetyHeight.setIntegerValue(settings.getSafetyHeight().getValue());
        zFeed.setDisable(false);
        zFeed.setIntegerValue(settings.getFeedZ().getValue());

        pcbPane.setToolpathColor(PCBPaneFX.ENABLED_TOOLPATH_COLOR);
        pcbPane.setGerberPrimitives(((TraceLayer)getCurrentLayer()).getElements());

        super.refresh();
        toolpathGenerationService.arcFeedProperty().set(feed.getIntegerValue() * settings.getFeedArcs().getValue() / 100);
    }

    @Override
    protected ToolpathGenerationService getToolpathGenerationService()
    {
        return new TraceMillingToolpathGenerationService(getMainApplication(), overallProgressBar.progressProperty(),
                estimatedMachiningTimeProperty, getCurrentLayer(), getCacheId(), getLayerModificationDate());
    }

    protected abstract boolean mirror();
    protected abstract int getCacheId();
    protected abstract long getLayerModificationDate();

    @Override
    protected String generateGCode()
    {
        InsulationMillingSettings settings = SettingsFactory.getInsulationMillingSettings();
        int arcFeed = (feed.getIntegerValue() * settings.getFeedArcs().getValue() / 100);
        TraceGCodeGenerator generator = new TraceGCodeGenerator(getMainApplication().getContext(), getCurrentLayer().getToolpaths(), mirror());
        return generator.generate(new RTPostprocessor(), feed.getIntegerValue(), zFeed.getIntegerValue(), arcFeed,
                clearance.getIntegerValue(), safetyHeight.getIntegerValue(), settings.getWorkingHeight().getValue(),
                settings.getSpeed().getValue());
    }
}
