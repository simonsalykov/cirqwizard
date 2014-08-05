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
import javafx.scene.control.Label;
import org.cirqwizard.settings.SettingsFactory;


public class MessageController extends SceneController
{
    @FXML private Parent view;
    @FXML private Label header;
    @FXML private Label text;
    @FXML private Button continueButton;
    @FXML private Button moveAwayButton;

    @Override
    public Parent getView()
    {
        return view;
    }

    @Override
    public void refresh()
    {
        moveAwayButton.setVisible(false);
        Context context = getMainApplication().getContext();
        switch (getMainApplication().getState())
        {
            case INSERTING_CUTTER_FOR_TOP_TRACES:
            case INSERTING_CUTTER_FOR_BOTTOM_TRACES:
                header.setText("Insert trace milling cutter into spindle");
                text.setText("Make sure the milling cutter is fully inserted.");
            break;
            case INSERTING_DRILL:
                header.setText("Insert drill: " + context.getPcbLayout().getDrillDiameters().get(context.getCurrentDrill()) + "mm");
                text.setText("Insert drill");
            break;
            case INSERTING_CUTTER_FOR_CONTOUR:
                header.setText("Insert contour cutter: " + context.getPcbLayout().getContourMillDiameter() + "mm");
                text.setText("Insert contour cutter");
            break;
            case INSERTING_SYRINGE:
                header.setText("Insert syringe with solder paste");
                text.setText("Connect it to the air outlet at the base of the machine. " +
                        "Connect a pump to the valve on the front cover of the machine and pump the air until manometer reads 2.0 bar. " +
                        "Do not disconnect the pump yet - you will need it again.");
            break;
            case INSERTING_PP_HEAD:
                header.setText("Insert pick & place head");
                text.setText("Connect it to the vacuum outlet at the base of the machine and to the socket. Choose a needle " +
                        "that will work for your components.");
            break;
            case TERMINAL:
                header.setText("Congratulations");
                text.setText("The PCB is done!");
                moveAwayButton.setVisible(true);
            break;
        }
        moveAwayButton.setDisable(getMainApplication().getCNCController() == null);
        continueButton.setVisible(getMainApplication().getState() != State.TERMINAL);
    }

    public void moveAway()
    {
        getMainApplication().getCNCController().moveHeadAway(SettingsFactory.getMachineSettings().getFarAwayY().getValue());
    }


}
