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

package org.cirqwizard.fx.popover;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.cirqwizard.fx.controls.RealNumberTextField;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.settings.PredefinedLocationSettings;
import org.cirqwizard.settings.SettingsFactory;

import java.text.DecimalFormat;

public class ManualControlPopOver extends PopOverController
{
    private static final DecimalFormat coordinatesFormat = new DecimalFormat("0.00");

    @FXML private RealNumberTextField xTextField;
    @FXML private RealNumberTextField yTextField;
    @FXML private RealNumberTextField zTextField;

    @Override
    protected String getFxmlName()
    {
        return "ManualControl.fxml";
    }

    @FXML
    public void initialize()
    {
        KeyboardHandler keyboardHandler = new KeyboardHandler();
        xTextField.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        yTextField.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        zTextField.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
    }

    public void refresh()
    {
        view.setDisable(getMainApplication().getCNCController() == null);
        xTextField.setIntegerValue(null);
        yTextField.setIntegerValue(null);
        zTextField.setIntegerValue(null);
    }

    private class KeyboardHandler implements EventHandler<KeyEvent>
    {
        @Override
        public void handle(KeyEvent event)
        {
            if (event.getCode() == KeyCode.ESCAPE)
                getPopup().hide();
            if (event.getCode() != KeyCode.UP && event.getCode() != KeyCode.DOWN)
                return;
            RealNumberTextField textField = (RealNumberTextField) event.getSource();
            if (textField.getRealNumberText() == null)
                return;
            try
            {
                RealNumber delta = new RealNumber("0.1");
                if (event.getCode() == KeyCode.DOWN)
                    delta = delta.negate();
                RealNumber currentValue = textField.getRealNumberText() == null ? new RealNumber(0) : new RealNumber(textField.getRealNumberText());
                textField.setText(coordinatesFormat.format(currentValue.add(delta).getValue()));
                textField.fireEvent(new ActionEvent());
            }
            catch (NumberFormatException e)
            {
                // Not a big deal
            }
        }
    }

    public void goToUpdatedCoordinates()
    {
        getMainApplication().getCNCController().moveTo(xTextField.getIntegerValue(), yTextField.getIntegerValue(),
                zTextField.getIntegerValue());
    }

    public void home()
    {
        getMainApplication().getCNCController().home(SettingsFactory.getMachineSettings().getYAxisDifference().getValue());
    }

    public void moveAway()
    {
        PredefinedLocationSettings settings = SettingsFactory.getPredefinedLocationSettings();
        getMainApplication().getCNCController().moveTo(settings.getFarAwayX().getValue(), settings.getFarAwayY().getValue(),
                settings.getFarAwayZ().getValue());
    }

    public void moveToToolChange()
    {
        PredefinedLocationSettings settings = SettingsFactory.getPredefinedLocationSettings();
        getMainApplication().getCNCController().moveTo(settings.getToolChangeX().getValue(), settings.getToolChangeY().getValue(),
                settings.getToolChangeZ().getValue());
    }

}
