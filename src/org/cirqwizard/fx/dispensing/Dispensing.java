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

import org.cirqwizard.fx.PCBPaneFX;
import org.cirqwizard.fx.machining.DispensingToolpathGenerationService;
import org.cirqwizard.fx.machining.Machining;
import org.cirqwizard.fx.machining.ToolpathGenerationService;
import org.cirqwizard.gcode.PasteGCodeGenerator;
import org.cirqwizard.layers.Layer;
import org.cirqwizard.layers.SolderPasteLayer;
import org.cirqwizard.post.RTPostprocessor;
import org.cirqwizard.settings.DispensingSettings;
import org.cirqwizard.settings.SettingsFactory;

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
        toolDiameter.setDisable(false);
        DispensingSettings settings = SettingsFactory.getDispensingSettings();
        toolDiameter.setIntegerValue(settings.getNeedleDiameter().getValue());
        feed.setIntegerValue(settings.getFeed().getValue());

        clearance.setIntegerValue(settings.getClearance().getValue());
        safetyHeight.setIntegerValue(null);
        safetyHeight.setDisable(true);
        zFeed.setIntegerValue(null);
        zFeed.setDisable(true);

        getMainApplication().getContext().setG54Z(settings.getZOffset().getValue());

        pcbPane.setGerberColor(PCBPaneFX.SOLDER_PAD_COLOR);
        pcbPane.setToolpathColor(PCBPaneFX.PASTE_TOOLPATH_COLOR);
        pcbPane.setGerberPrimitives(((SolderPasteLayer)getCurrentLayer()).getElements());
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
                settings.getPostFeedPause().getValue(), feed.getIntegerValue(), clearance.getIntegerValue(),
                settings.getWorkingHeight().getValue());
    }
}
