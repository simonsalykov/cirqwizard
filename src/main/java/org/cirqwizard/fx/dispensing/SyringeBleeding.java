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

package org.cirqwizard.fx.dispensing;

import javafx.scene.control.Button;
import org.cirqwizard.fx.ScreenController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import org.cirqwizard.settings.SettingsFactory;


public class SyringeBleeding extends ScreenController
{
    @FXML private Button pushButton;

    @Override
    protected String getFxmlName()
    {
        return "SyringeBleeding.fxml";
    }

    @Override
    protected String getName()
    {
        return "Bleeding";
    }

    @Override
    public void refresh()
    {
        pushButton.setDisable(getMainApplication().getCNCController() == null);
    }

    public void dispense()
    {
        getMainApplication().getCNCController().dispensePaste(SettingsFactory.getDispensingSettings().getBleedingDuration().getValue());
    }
}
