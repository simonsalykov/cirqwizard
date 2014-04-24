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
import org.cirqwizard.fx.SceneController;
import javafx.fxml.FXML;
import javafx.scene.Parent;


public class SyringeBleedingController extends SceneController
{
    @FXML private Parent view;
    @FXML private Button pushButton;

    @Override
    public Parent getView()
    {
        return view;
    }

    @Override
    public void refresh()
    {
        pushButton.setDisable(getMainApplication().getCNCController() == null);
    }

    public void dispense()
    {
        getMainApplication().getCNCController().dispensePaste(getMainApplication().getSettings().getDispensingBleedingDuration());
    }
}
