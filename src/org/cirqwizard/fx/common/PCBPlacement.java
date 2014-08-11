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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.VBox;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBSize;
import org.cirqwizard.fx.ScreenController;


public abstract class PCBPlacement extends ScreenController
{
    @FXML protected Button continueButton;
    @FXML protected VBox radioButtonsBox;
    @FXML protected RadioButton smallPCB;
    @FXML protected RadioButton largePCB;

    @FXML protected VBox errorBox;
    @FXML protected CheckBox ignoreErrorCheckbox;
    @FXML protected Label text;

    @Override
    protected String getFxmlName()
    {
        return "/org/cirqwizard/fx/common/PCBPlacement.fxml";
    }

    @Override
    protected String getName()
    {
        return "Placement";
    }

    @Override
    public void refresh()
    {
        Context context = getMainApplication().getContext();
        context.setPcbPlacement(null);
        PCBSize pcbSize = context.getPcbSize();
        if (pcbSize == PCBSize.Small)
            smallPCB.setSelected(true);
        else if (pcbSize == PCBSize.Large)
            largePCB.setSelected(true);

        errorBox.setVisible(false);
        ignoreErrorCheckbox.setSelected(false);

        updateComponents();
    }

    protected abstract Context.PcbPlacement getExpectedPlacement();

    @Override
    protected boolean isMandatory()
    {
        return !getExpectedPlacement().equals(getMainApplication().getContext().getPcbPlacement());
    }

    @Override
    public void next()
    {
        getMainApplication().getContext().setPcbPlacement(getExpectedPlacement());
        super.next();
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
}
