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
package org.cirqwizard.generation;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import org.cirqwizard.fx.Context;

public abstract class ProcessingService
{
    private Context context;
    private SimpleDoubleProperty progress = new SimpleDoubleProperty();
    private SimpleStringProperty currentStageProperty = new SimpleStringProperty();
    private SimpleStringProperty additionalInformation = new SimpleStringProperty();
    private BooleanProperty cancelled = new SimpleBooleanProperty(false);

    public ProcessingService(Context context)
    {
        this.context = context;
    }

    public Context getContext()
    {
        return context;
    }

    public String getCurrentStage()
    {
        return currentStageProperty.get();
    }

    public SimpleStringProperty currentStageProperty()
    {
        return currentStageProperty;
    }

    public String getAdditionalInformation()
    {
        return additionalInformation.get();
    }

    public SimpleStringProperty additionalInformationProperty()
    {
        return additionalInformation;
    }

    public double getProgress()
    {
        return progress.get();
    }

    public SimpleDoubleProperty progressProperty()
    {
        return progress;
    }

    public boolean isCancelled()
    {
        return cancelled.get();
    }

    public BooleanProperty cancelledProperty()
    {
        return cancelled;
    }

    public void setCancelled(boolean cancelled)
    {
        this.cancelled.set(cancelled);
    }

    public void setCurrentStage(String stage)
    {
        Platform.runLater(() -> currentStageProperty().setValue(stage));
    }

    public void setAdditionalInformation(String info)
    {
        Platform.runLater(() -> additionalInformation.setValue(info));
    }

    public void setProgress(double progress)
    {
        Platform.runLater(() -> progressProperty().setValue(progress));
    }

}
