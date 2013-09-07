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
import javafx.scene.Parent;
import javafx.scene.control.Button;


public class HomingController extends SceneController
{
    @FXML private Parent view;
    @FXML private Button homeButton;

    @Override
    public Parent getView()
    {
        return view;
    }

    @Override
    public void refresh()
    {
        homeButton.setDisable(getMainApplication().getCNCController() == null);
    }

    public void home()
    {
        getMainApplication().getCNCController().home(getMainApplication().getSettings().getMachineYDiff());
    }


}
