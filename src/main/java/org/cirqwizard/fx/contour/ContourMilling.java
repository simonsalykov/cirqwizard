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

package org.cirqwizard.fx.contour;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.scene.layout.GridPane;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBPaneFX;
import org.cirqwizard.fx.SettingsDependentScreenController;
import org.cirqwizard.fx.machining.Machining;
import org.cirqwizard.fx.settings.SettingsEditor;
import org.cirqwizard.gcode.MillingGCodeGenerator;
import org.cirqwizard.generation.optimizer.Chain;
import org.cirqwizard.generation.optimizer.ChainDetector;
import org.cirqwizard.generation.optimizer.Optimizer;
import org.cirqwizard.layers.Layer;
import org.cirqwizard.layers.MillingLayer;
import org.cirqwizard.post.RTPostprocessor;
import org.cirqwizard.settings.ApplicationConstants;
import org.cirqwizard.settings.ContourMillingSettings;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.List;

public class ContourMilling extends Machining
{
    @Override
    protected String getName()
    {
        return "Milling";
    }

    @Override
    public void refresh()
    {
        pcbPane.setGerberPrimitives(null);
        pcbPane.setGerberColor(PCBPaneFX.CONTOUR_COLOR);
        pcbPane.setToolpathColor(PCBPaneFX.CONTOUR_COLOR);

        Context context = getMainApplication().getContext();
        ContourMillingSettings settings = SettingsFactory.getContourMillingSettings();
        context.setG54Z(settings.getZOffset().getValue());
        super.refresh();
    }

    @Override
    public void populateSettingsGroup(GridPane pane, SettingsDependentScreenController listener)
    {
        SettingsEditor.renderSettings(pane, SettingsFactory.getContourMillingSettings(), getMainApplication(), listener);
    }

    @Override
    protected Layer getCurrentLayer()
    {
        return getMainApplication().getContext().getPcbLayout().getMillingLayer();
    }

    @Override
    protected void generateToolpaths()
    {
        MillingLayer layer = (MillingLayer) getCurrentLayer();
        layer.generateToolpaths();
        pcbPane.toolpathsProperty().setValue(FXCollections.observableArrayList(layer.getToolpaths()));
        ContourMillingSettings settings = SettingsFactory.getContourMillingSettings();

        List<Chain> chains = new ChainDetector(layer.getToolpaths()).detect();
        chains = new Optimizer(chains, convertToDouble(settings.getFeedXY().getValue()) / 60,
                convertToDouble(settings.getFeedZ().getValue()) / 60,
                convertToDouble(settings.getFeedXY().getDefaultValue()) / 60 * settings.getFeedArcs().getValue() / 100,
                convertToDouble(settings.getClearance().getValue()),
                convertToDouble(settings.getSafetyHeight().getValue()), 100, new SimpleBooleanProperty()).optimize();
        List<Toolpath> toolpaths = new ArrayList<>();
        chains.stream().forEach(c -> toolpaths.addAll(c.getSegments()));
        layer.setToolpaths(toolpaths);
    }

    private double convertToDouble(Integer i)
    {
        return i.doubleValue() / ApplicationConstants.RESOLUTION;
    }

    @Override
    protected String generateGCode()
    {
        ContourMillingSettings settings = SettingsFactory.getContourMillingSettings();
        int arcFeed = (settings.getFeedXY().getValue() * settings.getFeedArcs().getValue() / 100);
        MillingGCodeGenerator generator = new MillingGCodeGenerator(getMainApplication().getContext());
        return generator.generate(new RTPostprocessor(), settings.getFeedXY().getValue(), settings.getFeedZ().getValue(), arcFeed,
                settings.getClearance().getValue(), settings.getSafetyHeight().getValue(), settings.getWorkingHeight().getValue(),
                settings.getSpeed().getValue());
    }
}
