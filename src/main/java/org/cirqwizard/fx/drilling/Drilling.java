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

import javafx.collections.FXCollections;
import javafx.scene.layout.GridPane;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBPaneFX;
import org.cirqwizard.fx.SettingsDependentScreenController;
import org.cirqwizard.fx.machining.Machining;
import org.cirqwizard.fx.settings.SettingsEditor;
import org.cirqwizard.generation.gcode.DrillGCodeGenerator;
import org.cirqwizard.generation.toolpath.DrillPoint;
import org.cirqwizard.generation.toolpath.Toolpath;
import org.cirqwizard.layers.Board;
import org.cirqwizard.layers.LayerElement;
import org.cirqwizard.post.RTPostprocessor;
import org.cirqwizard.settings.DrillingSettings;
import org.cirqwizard.settings.SettingsFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
        Context context = getMainApplication().getContext();
        DrillingSettings settings = SettingsFactory.getDrillingSettings();

        context.setG54Z(settings.getZOffset().getValue());

        pcbPane.setGerberColor(PCBPaneFX.DRILL_POINT_COLOR);
        pcbPane.setToolpathColor(PCBPaneFX.DRILL_POINT_COLOR);
        super.refresh();
    }

    @Override
    protected Board.LayerType getCurrentLayer()
    {
        return Board.LayerType.DRILLING;
    }

    @Override
    public void populateSettingsGroup(GridPane pane, SettingsDependentScreenController listener)
    {
        SettingsEditor.renderSettings(pane, SettingsFactory.getDrillingSettings(), getMainApplication(), listener);
    }

    @Override
    protected void generateToolpaths()
    {
        List<? extends LayerElement> drillPoints = getMainApplication().getContext().getPanel().getCombinedElements(getCurrentLayer()).stream().
                filter(e -> ((DrillPoint) e).getToolDiameter() == getMainApplication().getContext().getCurrentDrill()).
                collect(Collectors.toList());
        pcbPane.toolpathsProperty().setValue(FXCollections.observableArrayList((Collection<? extends Toolpath>) drillPoints));
    }

    @Override
    protected String generateGCode()
    {
        DrillingSettings settings = SettingsFactory.getDrillingSettings();
        List<? extends Toolpath> toolpaths = pcbPane.toolpathsProperty().getValue();
        Context context = getMainApplication().getContext();
        DrillGCodeGenerator generator = new DrillGCodeGenerator(context.getG54X(), context.getG54Y(), context.getG54Z(),
                (List<DrillPoint>) toolpaths);
        return generator.generate(new RTPostprocessor(), settings.getFeed().getValue(), settings.getClearance().getValue(),
                settings.getSafetyHeight().getValue(), settings.getWorkingHeight().getValue(),
                settings.getSpeed().getValue());
    }
}
