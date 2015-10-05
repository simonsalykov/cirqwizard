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

import javafx.scene.layout.GridPane;
import org.cirqwizard.fx.PCBPaneFX;
import org.cirqwizard.fx.SettingsDependentScreenController;
import org.cirqwizard.fx.machining.DispensingToolpathGenerationService;
import org.cirqwizard.fx.machining.Machining;
import org.cirqwizard.fx.machining.ToolpathGenerationService;
import org.cirqwizard.fx.settings.SettingsEditor;
import org.cirqwizard.gcode.PasteGCodeGenerator;
import org.cirqwizard.layers.Layer;
import org.cirqwizard.layers.SolderPasteLayer;
import org.cirqwizard.post.RTPostprocessor;
import org.cirqwizard.settings.DispensingSettings;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.settings.SettingsGroup;

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

        pcbPane.setGerberColor(PCBPaneFX.SOLDER_PAD_COLOR);
        pcbPane.setToolpathColor(PCBPaneFX.PASTE_TOOLPATH_COLOR);
        pcbPane.setGerberPrimitives(((SolderPasteLayer)getCurrentLayer()).getElements());
    }

    @Override
    public void populateSettingsGroup(GridPane pane, SettingsDependentScreenController listener)
    {
        SettingsEditor.renderSettings(pane, SettingsFactory.getDispensingSettings(), getMainApplication(), listener);
    }

    @Override
    protected Layer getCurrentLayer()
    {
        return getMainApplication().getContext().getPcbLayout().getSolderPasteLayer();
    }

    @Override
    protected ToolpathGenerationService getToolpathGenerationService()
    {
        return new DispensingToolpathGenerationService(getMainApplication(), overallProgressBar.progressProperty(),
                estimatedMachiningTimeProperty);
    }

    @Override
    protected String generateGCode()
    {
        DispensingSettings settings = SettingsFactory.getDispensingSettings();
        PasteGCodeGenerator generator = new PasteGCodeGenerator(getMainApplication().getContext());
        return generator.generate(new RTPostprocessor(), settings.getPreFeedPause().getValue(),
                settings.getPostFeedPause().getValue(), settings.getFeed().getValue(), settings.getClearance().getValue(),
                settings.getWorkingHeight().getValue());
    }
}
