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

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.cirqwizard.fx.settings.SettingsEditor;
import org.cirqwizard.settings.SettingsFactory;


public class Homing extends ScreenController
{
    @FXML private Button homeButton;
    @FXML private VBox axisDifferenceWarningBox;

    @Override
    protected String getFxmlName()
    {
        return "Homing.fxml";
    }

    @Override
    protected String getName()
    {
        return "Homing";
    }

    @Override
    public void refresh()
    {
        homeButton.setDisable(getMainApplication().getCNCController() == null);
        axisDifferenceWarningBox.setVisible(SettingsFactory.getMachineSettings().getYAxisDifference().getValue() == null);
    }

    public void home()
    {
        getMainApplication().getCNCController().home(SettingsFactory.getMachineSettings().getYAxisDifference().getValue());
    }

    public void goToSettings()
    {
        getMainApplication().setCurrentScreen(getMainApplication().getScreen(SettingsEditor.class));
    }


}
