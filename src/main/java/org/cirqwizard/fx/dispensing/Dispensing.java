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

package org.cirqwizard.fx.dispensing;

import javafx.collections.FXCollections;
import javafx.scene.layout.GridPane;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBPane;
import org.cirqwizard.fx.SettingsDependentScreenController;
import org.cirqwizard.fx.machining.Machining;
import org.cirqwizard.fx.settings.SettingsEditor;
import org.cirqwizard.generation.DispensingToolpathGenerator;
import org.cirqwizard.generation.gcode.PasteGCodeGenerator;
import org.cirqwizard.generation.toolpath.Toolpath;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.layers.Board;
import org.cirqwizard.post.RTPostprocessor;
import org.cirqwizard.settings.DispensingSettings;
import org.cirqwizard.settings.SettingsFactory;

import java.util.List;

public class Dispensing extends Machining
{
    @Override
    protected String getName()
    {
        return "Dispensing";
    }

    @Override
    public void refresh()
    {
        super.refresh();
        DispensingSettings settings = SettingsFactory.getDispensingSettings();
        getMainApplication().getContext().setG54Z(settings.getZOffset().getValue());

        pcbPane.setGerberColor(PCBPane.SOLDER_PAD_COLOR);
        pcbPane.setToolpathColor(PCBPane.PASTE_TOOLPATH_COLOR);
        pcbPane.setGerberPrimitives(getMainApplication().getContext().getPanel().getCombinedElements(Board.LayerType.SOLDER_PASTE));
    }

    @Override
    public void populateSettingsGroup(GridPane pane, SettingsDependentScreenController listener)
    {
        SettingsEditor.renderSettings(pane, SettingsFactory.getDispensingSettings(), getMainApplication(), listener);
    }

    @Override
    protected Board.LayerType getCurrentLayer()
    {
        return Board.LayerType.SOLDER_PASTE;
    }

    @Override
    protected void generateToolpaths()
    {
        List<Toolpath> toolpaths = new DispensingToolpathGenerator((List<GerberPrimitive>)
                getMainApplication().getContext().getPanel().getCombinedElements(Board.LayerType.SOLDER_PASTE)).
                generate(SettingsFactory.getDispensingSettings().getNeedleDiameter().getValue());
        pcbPane.toolpathsProperty().setValue(FXCollections.observableArrayList(toolpaths));
    }

    @Override
    protected String generateGCode()
    {
        DispensingSettings settings = SettingsFactory.getDispensingSettings();
        Context context = getMainApplication().getContext();
        PasteGCodeGenerator generator = new PasteGCodeGenerator(context.getG54X(), context.getG54Y(), context.getG54Z(),
                pcbPane.toolpathsProperty().getValue());
        return generator.generate(new RTPostprocessor(), settings.getPreFeedPause().getValue(),
                settings.getPostFeedPause().getValue(), settings.getFeed().getValue(), settings.getClearance().getValue(),
                settings.getWorkingHeight().getValue());
    }
}
