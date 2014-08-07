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

package org.cirqwizard.fx.machining;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.cirqwizard.fx.MainApplication;
import org.cirqwizard.layers.DrillingLayer;
import org.cirqwizard.layers.Layer;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.toolpath.DrillPoint;
import org.cirqwizard.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DrillingToolpathGenerationService extends ToolpathGenerationService
{
    public DrillingToolpathGenerationService(MainApplication mainApplication, DoubleProperty overallProgressProperty, StringProperty estimatedMachiningTimeProperty)
    {
        super(mainApplication, overallProgressProperty, estimatedMachiningTimeProperty);
    }

    @Override
    protected Task<ObservableList<Toolpath>> createTask()
    {
        return new Task<ObservableList<Toolpath>>()
        {
            @Override
            protected ObservableList<Toolpath> call() throws Exception
            {
                try
                {
                    lastToolDiameter = toolDiameter.get();
                    overallProgressProperty.unbind();
                    generationStageProperty.unbind();
                    estimatedMachiningTimeProperty.unbind();

                    DrillingLayer layer = context.getPcbLayout().getDrillingLayer();
                    List<Toolpath> drillPoints = layer.getToolpaths().stream().parallel().
                            filter((p) -> Math.abs(toolDiameter.getValue() - p.getToolDiameter()) < 50).
                            collect(Collectors.toList());

                    return FXCollections.observableArrayList(drillPoints);
                }
                catch (NumberFormatException e)
                {
                    LoggerFactory.getApplicationLogger().log(Level.WARNING, "Could not parse tool diameter", e);
                    throw e;
                }
                catch (Exception e)
                {
                    LoggerFactory.logException("Error generating toolpaths", e);
                    throw e;
                }
            }
        };
    }
}
