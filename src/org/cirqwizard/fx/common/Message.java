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

package org.cirqwizard.fx.common;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.cirqwizard.fx.ScreenController;
import org.cirqwizard.settings.SettingsFactory;


public class Message extends ScreenController
{
    @FXML protected Label header;
    @FXML protected Label text;
    @FXML protected Button continueButton;
    @FXML protected Button moveAwayButton;

    @Override
    protected String getFxmlName()
    {
        return "/org/cirqwizard/fx/common/Message.fxml";
    }

    @Override
    public void refresh()
    {
        moveAwayButton.setVisible(false);
        moveAwayButton.setDisable(getMainApplication().getCNCController() == null);
        // TODO
//        continueButton.setVisible(getMainApplication().getState() != State.TERMINAL);
    }

    public void moveAway()
    {
        getMainApplication().getCNCController().moveHeadAway(SettingsFactory.getMachineSettings().getFarAwayY().getValue());
    }


}
