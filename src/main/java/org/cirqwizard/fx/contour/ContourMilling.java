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
import org.cirqwizard.fx.PCBPane;
import org.cirqwizard.fx.SettingsDependentScreenController;
import org.cirqwizard.fx.machining.Machining;
import org.cirqwizard.fx.settings.SettingsEditor;
import org.cirqwizard.generation.MillingToolpathGenerator;
import org.cirqwizard.generation.gcode.MillingGCodeGenerator;
import org.cirqwizard.generation.optimizer.Chain;
import org.cirqwizard.generation.optimizer.ChainDetector;
import org.cirqwizard.generation.optimizer.Optimizer;
import org.cirqwizard.generation.optimizer.TimeEstimator;
import org.cirqwizard.generation.toolpath.Toolpath;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.layers.Board;
import org.cirqwizard.post.RTPostprocessor;
import org.cirqwizard.settings.ApplicationConstants;
import org.cirqwizard.settings.ContourMillingSettings;
import org.cirqwizard.settings.SettingsFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
        pcbPane.setGerberColor(PCBPane.CONTOUR_COLOR);
        pcbPane.setToolpathColor(PCBPane.CONTOUR_COLOR);

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
    protected Board.LayerType getCurrentLayer()
    {
        return Board.LayerType.MILLING;
    }

    @Override
    protected void generateToolpaths()
    {
        ContourMillingSettings settings = SettingsFactory.getContourMillingSettings();
        double feed = convertToDouble(settings.getFeedXY().getValue()) / 60;
        double zFeed = convertToDouble(settings.getFeedZ().getValue()) / 60;
        double arcFeed = convertToDouble(settings.getFeedXY().getDefaultValue()) / 60 * settings.getFeedArcs().getValue() / 100;
        double clearance = convertToDouble(settings.getClearance().getValue());
        double safetyHeight = convertToDouble(settings.getSafetyHeight().getValue());

        List<Toolpath> toolpaths = new MillingToolpathGenerator(
                (List<GerberPrimitive>) getMainApplication().getContext().getPanel().getCombinedElements(Board.LayerType.MILLING)).
                generate();
        double originalDuration = TimeEstimator.calculateTotalDuration(toolpaths, feed, zFeed, arcFeed, clearance, safetyHeight,
                false, ApplicationConstants.ROUNDING);
        List<Chain> chains = new ChainDetector(toolpaths).detect();
        chains = new Optimizer(chains, feed, zFeed, arcFeed, clearance, safetyHeight, 100, new SimpleBooleanProperty()).optimize();
        List<Toolpath> optimizedToolpaths = chains.stream().map(Chain::getSegments).flatMap(Collection::stream).collect(Collectors.toList());
        double optimizedDuration = TimeEstimator.calculateTotalDuration(optimizedToolpaths, feed, zFeed, arcFeed, clearance, safetyHeight,
                false, ApplicationConstants.ROUNDING);
        if (optimizedDuration < originalDuration)
            toolpaths = optimizedToolpaths;
        getMainApplication().getContext().getPanel().setToolpaths(Board.LayerType.MILLING, toolpaths);
        pcbPane.toolpathsProperty().setValue(FXCollections.observableArrayList(toolpaths));
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

        StringBuilder sb = new StringBuilder();
        double stepNumber = 1;

        int workingStepHeight = settings.getWorkingStepHeight().getValue();
        if (workingStepHeight > 0)
            stepNumber = Math.ceil(Math.abs((double)settings.getWorkingHeight().getValue() / workingStepHeight));

        int roundedStepHeight = (int)(settings.getWorkingHeight().getValue() / stepNumber);

        int currentHeight = settings.getWorkingHeight().getValue();
        currentHeight -=  (stepNumber - 1) * roundedStepHeight;

        for (int i = 0; i < stepNumber; ++i)
        {
            String generatedGCode = generator.generate(new RTPostprocessor(), settings.getFeedXY().getValue(), settings.getFeedZ().getValue(), arcFeed,
                    settings.getClearance().getValue(), settings.getSafetyHeight().getValue(), currentHeight, settings.getSpeed().getValue());

            sb.append(generatedGCode);

            currentHeight += roundedStepHeight;
        }

        return sb.toString();
    }
}
