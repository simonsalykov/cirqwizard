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

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.cirqwizard.fx.controls.RealNumberTextField;
import org.cirqwizard.settings.SettingsFactory;

public class OffsetsPopOver extends PopOverController
{
    @FXML private RealNumberTextField xTextField;
    @FXML private RealNumberTextField yTextField;
    @FXML private RealNumberTextField zTextField;

    @Override
    protected String getFxmlName()
    {
        return "Offsets.fxml";
    }

    @FXML
    public void initialize()
    {
        xTextField.realNumberIntegerProperty().addListener((v, oldV, newV) -> setX(newV));
        yTextField.realNumberIntegerProperty().addListener((v, oldV, newV) -> setY(newV));
        zTextField.realNumberIntegerProperty().addListener((v, oldV, newV) -> setZ(newV));
        EventHandler<KeyEvent> keyEventEventHandler = event ->
        {
            if (event.getCode() == KeyCode.ESCAPE)
                popup.hide();
        };
        xTextField.setOnKeyPressed(keyEventEventHandler);
        yTextField.setOnKeyPressed(keyEventEventHandler);
        zTextField.setOnKeyPressed(keyEventEventHandler);
    }

    private void setX(Integer x)
    {
        getMainApplication().getContext().setG54X(x);
        SettingsFactory.getApplicationValues().getG54X().setValue(x);
        SettingsFactory.getApplicationValues().save();
    }

    private void setY(Integer y)
    {
        getMainApplication().getContext().setG54Y(y);
        SettingsFactory.getApplicationValues().getG54Y().setValue(y);
        SettingsFactory.getApplicationValues().save();
    }

    private void setZ(Integer z)
    {
        getMainApplication().getContext().setG54Z(z);
    }

    public void refresh()
    {
        xTextField.setIntegerValue(getMainApplication().getContext().getG54X());
        yTextField.setIntegerValue(getMainApplication().getContext().getG54Y());
        zTextField.setIntegerValue(getMainApplication().getContext().getG54Z());
    }

}
