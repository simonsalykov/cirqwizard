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

import javafx.scene.layout.GridPane;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBPane;
import org.cirqwizard.fx.SettingsDependentScreenController;
import org.cirqwizard.fx.machining.LongProcessingMachining;
import org.cirqwizard.generation.GenerationService;
import org.cirqwizard.generation.gcode.TraceGCodeGenerator;
import org.cirqwizard.generation.optimizer.Chain;
import org.cirqwizard.generation.optimizer.OptimizationService;
import org.cirqwizard.generation.toolpath.ToolpathsCacheKey;
import org.cirqwizard.post.RTPostprocessor;
import org.cirqwizard.settings.InsulationMillingSettings;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.settings.ToolSettings;

import java.util.List;

public abstract class TraceMilling extends LongProcessingMachining
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
        return InsertTool.EXPECTED_TOOL.equals(context.getInsertedTool()) && context.getG54Z() != null;
    }

    @Override
    public void populateSettingsGroup(GridPane pane, SettingsDependentScreenController listener)
    {
        pane.getChildren().clear();
        pane.getChildren().add(new TracesSettingsPopOver(getMainApplication().getContext(), this).getView());
    }

    @Override
    public void refresh()
    {
        pcbPane.setToolpathColor(PCBPane.ENABLED_TOOLPATH_COLOR);
        pcbPane.setGerberPrimitives(getMainApplication().getContext().getPanel().getCombinedElements(getCurrentLayer()));

        super.refresh();
    }

    @Override
    protected GenerationService getGenerationService()
    {
        return new org.cirqwizard.generation.ToolpathGenerationService(getMainApplication().getContext(), getCurrentLayer());
    }

    @Override
    protected OptimizationService getOptimizationService(List<Chain> chains)
    {
        ToolSettings currentTool = getMainApplication().getContext().getCurrentMillingTool();
        InsulationMillingSettings settings = SettingsFactory.getInsulationMillingSettings();
        return new OptimizationService(getMainApplication().getContext(), chains, getMergeTolerance(), currentTool.getFeedXY(),
                currentTool.getFeedZ(), currentTool.getArcs(), settings.getClearance().getValue(), settings.getSafetyHeight().getValue());
    }

    @Override
    protected int getMergeTolerance()
    {
        return getMainApplication().getContext().getCurrentMillingTool().getDiameter() / 4;
    }

    protected abstract boolean mirror();

    @Override
    protected String generateGCode()
    {
        InsulationMillingSettings settings = SettingsFactory.getInsulationMillingSettings();
        ToolSettings currentTool = getMainApplication().getContext().getCurrentMillingTool();
        int arcFeed = (currentTool.getFeedXY() * currentTool.getArcs() / 100);
        TraceGCodeGenerator generator = new TraceGCodeGenerator(getMainApplication().getContext(), pcbPane.toolpathsProperty().getValue(), mirror());
        return generator.generate(new RTPostprocessor(), currentTool.getFeedXY(), currentTool.getFeedZ(), arcFeed,
                settings.getClearance().getValue(), settings.getSafetyHeight().getValue(), settings.getWorkingHeight().getValue(),
                currentTool.getSpeed());
    }
}
