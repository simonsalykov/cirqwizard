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

package org.cirqwizard.fx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.cirqwizard.fx.controls.RealNumberTextField;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.settings.SettingsFactory;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;


public class ManualMovementController extends SceneController implements Initializable
{
    @FXML private Parent view;
    @FXML private Button homeButton;
    @FXML public RealNumberTextField xPositionTextField;
    @FXML public RealNumberTextField yPositionTextField;
    @FXML public RealNumberTextField zPositionTextField;

    private static final DecimalFormat coordinatesFormat = new DecimalFormat("0.00");

    @Override
    public Parent getView()
    {
        return view;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        KeyboardHandler keyboardHandler = new KeyboardHandler();
        xPositionTextField.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        yPositionTextField.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        zPositionTextField.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
    }

    @Override
    public void refresh()
    {
        boolean noMachineConnected = getMainApplication().getCNCController() == null;
        homeButton.setDisable(noMachineConnected);
        xPositionTextField.setDisable(noMachineConnected);
        yPositionTextField.setDisable(noMachineConnected);
        zPositionTextField.setDisable(noMachineConnected);
    }

    private class KeyboardHandler implements EventHandler<KeyEvent>
    {
        @Override
        public void handle(KeyEvent event)
        {
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
        getMainApplication().getCNCController().moveTo(xPositionTextField.getIntegerValue(), yPositionTextField.getIntegerValue(),
                zPositionTextField.getIntegerValue());
    }

    public void home()
    {
        getMainApplication().getCNCController().home(SettingsFactory.getMachineSettings().getYAxisDifference().getValue());
    }

    public void goToMDI()
    {
        getMainApplication().setState(State.MANUAL_DATA_INPUT);
    }

}




