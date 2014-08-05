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

import org.cirqwizard.settings.*;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;


public class JobSelectionController extends SceneController
{
    @FXML private Parent view;
    @FXML private CheckBox topTraces;
    @FXML private CheckBox bottomTraces;
    @FXML private CheckBox drilling;
    @FXML private CheckBox contour;
    @FXML private CheckBox paste;
    @FXML private CheckBox placing;
    @FXML private Button continueButton;
    @FXML private Label errorMessage;
    @FXML private Hyperlink linkToSettings;
    @Override
    public Parent getView()
    {
        return view;
    }

    private String getEmptySettings()
    {
        if (!SettingsFactory.getMachineSettings().validate())
            return "You need to fill in all mandatory fields in Machine settings";

        if ((topTraces.isSelected() || bottomTraces.isSelected()) && !SettingsFactory.getInsulationMillingSettings().validate())
            return "You need to fill in all mandatory fields in Insulation milling settings";

        if (drilling.isSelected() && !SettingsFactory.getDrillingSettings().validate())
            return "You need to fill in all mandatory fields in Drilling settings";

        if (contour.isSelected() && !SettingsFactory.getContourMillingSettings().validate())
            return "You need to fill in all mandatory fields in Contour milling settings";

        if (paste.isSelected() && !SettingsFactory.getDispensingSettings().validate())
            return "You need to fill in all mandatory fields in Dispensing settings";

        if (placing.isSelected() && !SettingsFactory.getPpSettings().validate())
            return "You need to fill in all mandatory fields in Pick & Place settings";

        return null;
    }

    private void checkSettings()
    {
        String checkResult = getEmptySettings();
        continueButton.setDisable(checkResult != null);
        errorMessage.setText(checkResult);
        errorMessage.setVisible(checkResult != null);
        linkToSettings.setVisible(checkResult != null);
    }

    public void showSettings()
    {
        getMainApplication().setState(State.CHECK_SETTINGS);
    }

    public void refresh()
    {
        Context context = getMainApplication().getContext();
        topTraces.setSelected(context.isTopTracesSelected());
        topTraces.setDisable(context.getTopTracesLayer() == null);
        bottomTraces.setSelected(context.isBottomTracesSelected());
        bottomTraces.setDisable(context.getBottomTracesLayer() == null);
        drilling.setSelected(context.isDrillingSelected());
        drilling.setDisable(context.getDrillingLayer() == null);
        contour.setSelected(context.isContourSelected());
        contour.setDisable(context.getMillingLayer() == null);
        paste.setSelected(context.isPasteSelected());
        paste.setDisable(context.getSolderPasteLayer() == null);
        placing.setSelected(context.isPlacingSelected());
        placing.setDisable(context.getComponentsLayer() == null);
        checkSettings();
    }

    public void updateContext()
    {
        Context context = getMainApplication().getContext();
        context.setTopTracesSelected(topTraces.isSelected());
        context.setBottomTracesSelected(bottomTraces.isSelected());
        context.setDrillingSelected(drilling.isSelected());
        context.setContourSelected(contour.isSelected());
        context.setPasteSelected(paste.isSelected());
        context.setPlacingSelected(placing.isSelected());
        checkSettings();
    }
}
