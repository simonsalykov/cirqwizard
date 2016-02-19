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
import org.cirqwizard.layers.DrillingLayer;
import org.cirqwizard.layers.Layer;
import org.cirqwizard.post.RTPostprocessor;
import org.cirqwizard.settings.DrillingSettings;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.generation.toolpath.Toolpath;

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
        super.refresh();
        Context context = getMainApplication().getContext();
        DrillingSettings settings = SettingsFactory.getDrillingSettings();

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
    public void populateSettingsGroup(GridPane pane, SettingsDependentScreenController listener)
    {
        SettingsEditor.renderSettings(pane, SettingsFactory.getDrillingSettings(), getMainApplication(), listener);
    }

    @Override
    protected void generateToolpaths()
    {
        DrillingLayer layer = (DrillingLayer) getCurrentLayer();
        List<Toolpath> drillPoints = layer.getToolpaths().stream().
                filter((p) -> Math.abs(getMainApplication().getContext().getCurrentDrill() - p.getToolDiameter()) < 50).
                collect(Collectors.toList());
        pcbPane.toolpathsProperty().setValue(FXCollections.observableArrayList(drillPoints));
    }

    @Override
    protected String generateGCode()
    {
        DrillingSettings settings = SettingsFactory.getDrillingSettings();
        DrillGCodeGenerator generator = new DrillGCodeGenerator(getMainApplication().getContext());
        return generator.generate(new RTPostprocessor(), settings.getFeed().getValue(), settings.getClearance().getValue(),
                settings.getSafetyHeight().getValue(), settings.getWorkingHeight().getValue(),
                settings.getSpeed().getValue());
    }
}
