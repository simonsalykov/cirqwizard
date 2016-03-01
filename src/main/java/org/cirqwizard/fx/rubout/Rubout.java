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

import javafx.scene.layout.GridPane;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBPane;
import org.cirqwizard.fx.SettingsDependentScreenController;
import org.cirqwizard.fx.machining.LongProcessingMachining;
import org.cirqwizard.fx.settings.SettingsEditor;
import org.cirqwizard.generation.GenerationService;
import org.cirqwizard.generation.gcode.TraceGCodeGenerator;
import org.cirqwizard.generation.optimizer.Chain;
import org.cirqwizard.generation.optimizer.OptimizationService;
import org.cirqwizard.generation.toolpath.ToolpathsCacheKey;
import org.cirqwizard.post.RTPostprocessor;
import org.cirqwizard.settings.RubOutSettings;
import org.cirqwizard.settings.SettingsFactory;

import java.util.List;

public abstract class Rubout extends LongProcessingMachining
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
        return InsertTool.EXPECTED_TOOL.equals(context.getInsertedTool());
    }

    @Override
    public void refresh()
    {
        pcbPane.setToolpathColor(PCBPane.ENABLED_TOOLPATH_COLOR);
        pcbPane.setGerberPrimitives(getMainApplication().getContext().getPanel().getCombinedElements(getCurrentLayer()));

        super.refresh();
        getMainApplication().getContext().setG54Z(SettingsFactory.getRubOutSettings().getZOffset().getValue());
    }

    protected abstract int getCacheId();
    protected abstract long getLayerModificationDate();
    protected abstract boolean mirror();

    @Override
    protected GenerationService getGenerationService()
    {
        return new org.cirqwizard.generation.RuboutToolpathGenerationService(
                getMainApplication().getContext(), getCurrentLayer());
    }

    @Override
    protected OptimizationService getOptimizationService(List<Chain> chains)
    {
        RubOutSettings settings = SettingsFactory.getRubOutSettings();
        return new OptimizationService(getMainApplication().getContext(), chains, getMergeTolerance(), settings.getFeedXY().getValue(),
                settings.getFeedZ().getValue(), settings.getFeedArcs().getValue(), settings.getClearance().getValue(),
                settings.getSafetyHeight().getValue());
    }

    @Override
    protected ToolpathsCacheKey getCacheKey()
    {
        RubOutSettings settings = SettingsFactory.getRubOutSettings();
        return new ToolpathsCacheKey(getCacheId(), settings.getToolDiameter().getValue(), 0,
                0, false, settings.getInitialOffset().getValue(), settings.getOverlap().getValue());
    }

    @Override
    protected int getMergeTolerance()
    {
        return SettingsFactory.getRubOutSettings().getToolDiameter().getValue() / 10;
    }

    @Override
    protected String generateGCode()
    {
        RubOutSettings settings = SettingsFactory.getRubOutSettings();
        int arcFeed = (settings.getFeedXY().getValue() * settings.getFeedArcs().getValue() / 100);
        TraceGCodeGenerator generator = new TraceGCodeGenerator(getMainApplication().getContext(),
                getMainApplication().getContext().getPanel().getToolspaths(getCurrentLayer()), mirror());
        return generator.generate(new RTPostprocessor(), settings.getFeedXY().getValue(), settings.getFeedZ().getValue(), arcFeed,
                settings.getClearance().getValue(), settings.getSafetyHeight().getValue(), settings.getWorkingHeight().getValue(),
                settings.getSpeed().getValue());
    }

    @Override
    public void populateSettingsGroup(GridPane pane, SettingsDependentScreenController listener)
    {
        SettingsEditor.renderSettings(pane, SettingsFactory.getRubOutSettings(), getMainApplication(), listener);
    }
}
