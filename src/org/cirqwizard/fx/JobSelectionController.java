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

import org.cirqwizard.settings.InsulationMillingSettings;
import org.cirqwizard.settings.Settings;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import org.cirqwizard.settings.SettingsFactory;


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
        Settings settings = getMainApplication().getSettings();
        InsulationMillingSettings insulationMillingSettings = SettingsFactory.getInsulationMillingSettings();

        if (settings.getMachineYDiff() == null)
            return "You need to specify a value in settings for parameter Y axis difference (in Machine  pane)";
        if (settings.getMachineReferencePinX() == null)
            return "You need to specify a value in settings for parameter Reference pin X (in Machine  pane)";
        if (settings.getMachineReferencePinY() == null)
            return "You need to specify a value in settings for parameter Reference pin Y (in Machine  pane)";
        if (settings.getMachineSmallPCBWidth() == null)
            return "You need to specify a value in settings for parameter Small PCB width (in Machine  pane)";
        if (settings.getMachineLargePCBWidth() == null)
            return "You need to specify a value in settings for parameter Large PCB width (in Machine  pane)";
        if (settings.getFarAwayY() == null)
            return "You need to specify a value in settings for parameter Far away Y (in Machine  pane)";

        if (topTraces.isSelected() || bottomTraces.isSelected())
        {
            if (insulationMillingSettings.getToolDiameter().getValue() == null)
                return "You need to specify a value in settings for parameter Tool diameter (in Insulation milling pane)";
            if (insulationMillingSettings.getFeedXY().getValue() == null)
                return "You need to specify a value in settings for parameter Feed XY (in Insulation milling pane)";
            if (insulationMillingSettings.getFeedZ().getValue() == null)
                return "You need to specify a value in settings for parameter Feed Z (in Insulation milling pane)";
            if (insulationMillingSettings.getSpeed().getValue() == null)
                return "You need to specify a value in settings for parameter Speed (in Insulation milling pane)";
            if (insulationMillingSettings.getClearance().getValue() == null)
                return "You need to specify a value in settings for parameter Clearance (in Insulation milling pane)";
            if (insulationMillingSettings.getSafetyHeight().getValue() == null)
                return "You need to specify a value in settings for parameter Safety height (in Insulation milling pane)";
            if (insulationMillingSettings.getZOffset().getValue() == null)
                return "You need to specify a value in settings for parameter Z offset (in Insulation milling pane)";
            if (insulationMillingSettings.getWorkingHeight().getValue() == null)
                return "You need to specify a value in settings for parameter Working height (in Insulation milling pane)";
        }

        if (drilling.isSelected())
        {
            if (settings.getDefaultDrillingFeed() == null)
                return "You need to specify a value in settings for parameter Feed (in Drilling pane)";
            if (settings.getDefaultDrillingSpeed().trim().isEmpty())
                return "You need to specify a value in settings for parameter Speed (in Drilling pane)";
            if (settings.getDefaultDrillingClearance() == null)
                return "You need to specify a value in settings for parameter Clearance (in Drilling pane)";
            if (settings.getDefaultDrillingSafetyHeight() == null)
                return "You need to specify a value in settings for parameter Safety height (in Drilling pane)";
            if (settings.getDefaultDrillingZOffset() == null)
                return "You need to specify a value in settings for parameter Z offset (in Drilling pane)";
            if (settings.getDefaultDrillingWorkingHeight() == null)
                return "You need to specify a value in settings for parameter Working height (in Drilling pane)";
        }

        if (contour.isSelected())
        {
            if (settings.getDefaultContourFeedXY() == null)
                return "You need to specify a value in settings for parameter Feed XY (in Contour milling pane)";
            if (settings.getDefaultContourFeedZ() == null)
                return "You need to specify a value in settings for parameter Feed Z (in Contour milling pane)";
            if (settings.getDefaultContourSpeed().trim().isEmpty())
                return "You need to specify a value in settings for parameter Speed (in Contour milling pane)";
            if (settings.getDefaultContourClearance() == null)
                return "You need to specify a value in settings for parameter Clearance (in Contour milling pane)";
            if (settings.getDefaultContourSafetyHeight() == null)
                return "You need to specify a value in settings for parameter Safety height (in Contour milling pane)";
            if (settings.getDefaultContourZOffset() == null)
                return "You need to specify a value in settings for parameter Z offset (in Contour milling pane)";
            if (settings.getDefaultContourWorkingHeight() == null)
                return "You need to specify a value in settings for parameter Working height (in Contour milling pane)";
        }

        if (paste.isSelected())
        {
            if (settings.getDefaultDispensingNeedleDiameter() == null)
                return "You need to specify a value in settings for parameter Needle diameter (in Dispensing pane)";
            if (settings.getDefaultDispensingPrefeedPause() == null)
                return "You need to specify a value in settings for parameter Prefeed pause (in Dispensing pane)";
            if (settings.getDefaultDispensingFeed() == null)
                return "You need to specify a value in settings for parameter Feed (in Dispensing pane)";
            if (settings.getDefaultDispensingClearance() == null)
                return "You need to specify a value in settings for parameter Clearance (in Dispensing pane)";
            if (settings.getDefaultDispensingZOffset() == null)
                return "You need to specify a value in settings for parameter Z offset (in Dispensing pane)";
            if (settings.getDefaultDispensingWorkingHeight() == null)
                return "You need to specify a value in settings for parameter Working height (in Dispensing pane)";
            if (settings.getDispensingBleedingDuration() == null)
                return "You need to specify a value in settings for parameter Bleeding duration (in Dispensing pane)";
            if (settings.getDispensingPostfeedPause() == null)
                return "You need to specify a value in settings for parameter Postfeed pause (in Dispensing pane)";
        }

        if (placing.isSelected())
        {
            if (settings.getPPPickupHeight() == null)
                return "You need to specify a value in settings for parameter Pickup height (in Pick & Place pane)";
            if (settings.getPPMoveHeight() == null)
                return "You need to specify a value in settings for parameter Move height (in Pick & Place pane)";
        }

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
