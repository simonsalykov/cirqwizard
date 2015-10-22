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
package org.cirqwizard.generation.optimizer;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import org.cirqwizard.fx.Context;
import org.cirqwizard.generation.ProcessingService;
import org.cirqwizard.settings.ApplicationConstants;
import org.cirqwizard.settings.InsulationMillingSettings;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.settings.ToolSettings;

import java.text.DecimalFormat;
import java.util.List;

public class OptimizationService extends ProcessingService
{
    private List<Chain> chains;
    private int mergeTolerance;

    public OptimizationService(Context context, List<Chain> chains, int mergeTolerance)
    {
        super(context);
        this.chains = chains;
        this.mergeTolerance = mergeTolerance;
    }

    public List<Chain> optimize()
    {
        InsulationMillingSettings settings = SettingsFactory.getInsulationMillingSettings();
        ToolSettings currentTool = getContext().getCurrentMillingTool();

        final Optimizer optimizer = new Optimizer(chains, convertToDouble(currentTool.getFeedXY() / 60), convertToDouble(currentTool.getFeedZ()) / 60,
                convertToDouble(currentTool.getFeedXY()) / 60 * currentTool.getArcs() / 100,
                convertToDouble(settings.getClearance().getValue()), convertToDouble(settings.getSafetyHeight().getValue()), mergeTolerance,
                cancelledProperty());
        setCurrentStage("Optimizing milling time...");
        progressProperty().bind(optimizer.progressProperty());

        final DecimalFormat format = new DecimalFormat("00");
        Platform.runLater(() ->
                additionalInformationProperty().bind(Bindings.createStringBinding(() ->
                {
                    long totalDuration = (long) optimizer.getBestSolutionDuration();
                    String time = format.format(totalDuration / 3600) + ":" + format.format(totalDuration % 3600 / 60) +
                            ":" + format.format(totalDuration % 60);
                    return "Estimated machining time: " + time;
                }, optimizer.bestSolutionDurationProperty()))
        );

        return optimizer.optimize();
    }

    private double convertToDouble(Integer i)
    {
        return i.doubleValue() / ApplicationConstants.RESOLUTION;
    }
}
