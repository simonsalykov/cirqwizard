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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.VBox;
import org.cirqwizard.settings.SettingsFactory;


public class PCBPlacementController extends SceneController
{
    @FXML private Parent view;
    @FXML private Button continueButton;
    @FXML private VBox radioButtonsBox;
    @FXML private RadioButton smallPCB;
    @FXML private RadioButton largePCB;
    @FXML private Button moveAwayButton;

    @FXML private Label header;

    @FXML private VBox errorBox;
    @FXML private CheckBox ignoreErrorCheckbox;
    @FXML private Label topTracesText;
    @FXML private Label bottomTracesText;
    @FXML private Label drillingText;

    @Override
    public Parent getView()
    {
        return view;
    }

    @Override
    public void refresh()
    {
        Context context = getMainApplication().getContext();
        PCBSize pcbSize = context.getPcbSize();
        if (pcbSize == PCBSize.Small)
            smallPCB.setSelected(true);
        else if (pcbSize == PCBSize.Large)
            largePCB.setSelected(true);

        header.setVisible(false);
        topTracesText.setVisible(false);
        errorBox.setVisible(false);
        bottomTracesText.setVisible(false);
        drillingText.setVisible(false);
        State state = getMainApplication().getState();
        switch (state)
        {
            case PCB_PLACEMENT_FOR_TOP_TRACES:
            case PCB_PLACEMENT_FOR_DISPENSING:
            case PCB_PLACEMENT_FOR_PLACING:
                header.setVisible(true);
                topTracesText.setVisible(true);
            break;
            case PCB_PLACEMENT_FOR_BOTTOM_TRACES:
                bottomTracesText.setVisible(true);
                header.setVisible(true);
                break;
            case PCB_PLACEMENT_FOR_DRILLING:
            case PCB_PLACEMENT_FOR_CONTOUR:
                header.setVisible(true);
                drillingText.setVisible(true);
            break;
        }

        if (state == State.PCB_PLACEMENT_FOR_TOP_TRACES ||
            (state == State.PCB_PLACEMENT_FOR_BOTTOM_TRACES && !context.isTopTracesSelected()) ||
            (state == State.PCB_PLACEMENT_FOR_DRILLING && !(context.isTopTracesSelected() || context.isBottomTracesSelected())) ||
            (state == State.PCB_PLACEMENT_FOR_CONTOUR && !(context.isTopTracesSelected() || context.isBottomTracesSelected() || context.isDrillingSelected())) ||
            (state == State.PCB_PLACEMENT_FOR_DISPENSING && !(context.isTopTracesSelected() || context.isBottomTracesSelected() || context.isDrillingSelected() ||
                    context.isContourSelected())) ||
            (state == State.PCB_PLACEMENT_FOR_PLACING && !(context.isTopTracesSelected() || context.isBottomTracesSelected() || context.isDrillingSelected() ||
                    context.isContourSelected() || context.isPasteSelected())))
        {
            radioButtonsBox.setVisible(true);
        }
        else
            radioButtonsBox.setVisible(false);

        moveAwayButton.setDisable(getMainApplication().getCNCController() == null);
        ignoreErrorCheckbox.setSelected(false);

        updateComponents();
    }

    private boolean checkSelectedPcbSize()
    {
        Context context = getMainApplication().getContext();
        return context.getPcbSize().checkFit(context.getBoardWidth(), context.getBoardHeight());
    }

    public void updateComponents()
    {
        Context context = getMainApplication().getContext();
        errorBox.setVisible(false);
        if (smallPCB.isSelected())
            context.setPcbSize(PCBSize.Small);
        else if (largePCB.isSelected())
            context.setPcbSize(PCBSize.Large);
        continueButton.setDisable(radioButtonsBox.isVisible() && !smallPCB.isSelected() && !largePCB.isSelected());
        if (context.getPcbSize() != null && !checkSelectedPcbSize())
        {
            errorBox.setVisible(true);
            continueButton.setDisable(!ignoreErrorCheckbox.isSelected());
        }
    }

    public void moveAway()
    {
        getMainApplication().getCNCController().moveHeadAway(SettingsFactory.getMachineSettings().getFarAwayY().getValue());
    }
}
