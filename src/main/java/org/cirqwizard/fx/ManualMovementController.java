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
import org.cirqwizard.fx.misc.ManualDataInput;
import org.cirqwizard.settings.SettingsFactory;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;


public class ManualMovementController extends ScreenController implements Initializable
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

    public static class KeyboardHandler implements EventHandler<KeyEvent>
    {
        @Override
        public void handle(KeyEvent event)
        {
            if (event.getCode() != KeyCode.UP && event.getCode() != KeyCode.DOWN)
                return;
            RealNumberTextField textField = (RealNumberTextField) event.getSource();
            if (textField.getIntegerValue() == null)
                return;
            int delta = 100;
            if (event.getCode() == KeyCode.DOWN)
                delta = -delta;
            textField.setIntegerValue(textField.getIntegerValue() + delta);
            textField.fireEvent(new ActionEvent());
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
        getMainApplication().setCurrentScreen(getMainApplication().getScreen(ManualDataInput.class));
    }

}




