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
package org.cirqwizard.fx.traces;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.SettingsDependentScreenController;
import org.cirqwizard.fx.controls.RealNumberTextField;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.InsulationMillingSettings;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.settings.ToolLibrary;
import org.cirqwizard.settings.ToolSettings;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TracesSettingsPopOver implements Initializable
{
    @FXML private Node view;

    @FXML private ComboBox<ToolSettings> toolComboBox;

    @FXML private RealNumberTextField diameterTextField;
    @FXML private TextField speedTextField;

    @FXML private RealNumberTextField xyFeedTextField;
    @FXML private RealNumberTextField zFeedTextField;
    @FXML private TextField arcsFeedTextField;

    @FXML private RealNumberTextField clearanceTextField;
    @FXML private RealNumberTextField safetyHeightTextField;
    @FXML private RealNumberTextField workingHeightTextField;

    @FXML private TextField additionalPassesCountTextField;
    @FXML private TextField additonalPassesOverlapTextField;
    @FXML private CheckBox additionalPassesPadsOnlyCheckBox;

    private Context context;
    private SettingsDependentScreenController listener;
    private boolean supressInvalidation = false;

    public TracesSettingsPopOver(Context context, SettingsDependentScreenController listener)
    {
        this.context = context;
        this.listener = listener;

        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TracesSettingsPopOver.fxml"));
            loader.setController(this);
            loader.load();
            refresh();
        }
        catch (IOException e)
        {
            LoggerFactory.logException("Could not load FXML", e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        toolComboBox.getSelectionModel().selectedItemProperty().addListener((v, oldV, newV) ->
        {
            refreshFields();
            if (!supressInvalidation)
                listener.settingsInvalidated();
        });
        diameterTextField.realNumberIntegerProperty().addListener((v, oldV, newV) ->
        {
            toolComboBox.getItems().get(toolComboBox.getSelectionModel().getSelectedIndex()).setDiameter(newV);
            saveLibrary();
        });
        addInvalidationListenerToTextField(diameterTextField);
        speedTextField.textProperty().addListener((v, oldV, newV) ->
        {
            try
            {
                toolComboBox.getItems().get(toolComboBox.getSelectionModel().getSelectedIndex()).setSpeed(Integer.valueOf(newV));
            }
            catch (NumberFormatException e) {}
            saveLibrary();
        });
        xyFeedTextField.realNumberIntegerProperty().addListener((v, oldV, newV) ->
        {
            toolComboBox.getItems().get(toolComboBox.getSelectionModel().getSelectedIndex()).setFeedXY(newV);
            saveLibrary();
        });
        zFeedTextField.realNumberIntegerProperty().addListener((v, oldV, newV) ->
        {
            toolComboBox.getItems().get(toolComboBox.getSelectionModel().getSelectedIndex()).setFeedZ(newV);
            saveLibrary();
        });
        arcsFeedTextField.textProperty().addListener((v, oldV, newV) ->
        {
            try
            {
                toolComboBox.getItems().get(toolComboBox.getSelectionModel().getSelectedIndex()).setArcs(Integer.valueOf(newV));
            }
            catch (NumberFormatException e) {}
            saveLibrary();
        });
        clearanceTextField.realNumberIntegerProperty().addListener((v, oldV, newV) ->
        {
            InsulationMillingSettings settings = SettingsFactory.getInsulationMillingSettings();
            settings.getClearance().setValue(newV);
            settings.save();
        });
        safetyHeightTextField.realNumberIntegerProperty().addListener((v, oldV, newV) ->
        {
            InsulationMillingSettings settings = SettingsFactory.getInsulationMillingSettings();
            settings.getSafetyHeight().setValue(newV);
            settings.save();
        });
        workingHeightTextField.realNumberIntegerProperty().addListener((v, oldV, newV) ->
        {
            InsulationMillingSettings settings = SettingsFactory.getInsulationMillingSettings();
            settings.getWorkingHeight().setValue(newV);
            settings.save();
        });
        additionalPassesCountTextField.textProperty().addListener((v, oldV, newV) ->
        {
            try
            {
                toolComboBox.getItems().get(toolComboBox.getSelectionModel().getSelectedIndex()).setAdditionalPasses(Integer.valueOf(newV));
            }
            catch (NumberFormatException e) {}
            saveLibrary();
        });
        addInvalidationListenerToTextField(additionalPassesCountTextField);
        additonalPassesOverlapTextField.textProperty().addListener((v, oldV, newV) ->
        {
            try
            {
                toolComboBox.getItems().get(toolComboBox.getSelectionModel().getSelectedIndex()).setAdditionalPassesOverlap(Integer.valueOf(newV));
            }
            catch (NumberFormatException e) {}
            saveLibrary();
        });
        addInvalidationListenerToTextField(additonalPassesOverlapTextField);
        additionalPassesPadsOnlyCheckBox.selectedProperty().addListener((v, oldV, newV) ->
        {
            toolComboBox.getItems().get(toolComboBox.getSelectionModel().getSelectedIndex()).setAdditionalPassesPadsOnly(newV);
            saveLibrary();
            listener.settingsInvalidated();
        });
    }

    public Node getView()
    {
        return view;
    }

    public void refresh()
    {
        try
        {
            supressInvalidation = true;
            toolComboBox.setItems(FXCollections.observableArrayList(ToolLibrary.load().getToolSettings()));
            toolComboBox.getSelectionModel().select(context.getCurrentMillingToolIndex());
        }
        catch (Exception e)
        {
            LoggerFactory.logException("Could not load tool library", e);
        }
        finally
        {
            supressInvalidation = false;
        }
    }

    private void saveLibrary()
    {
        if (supressInvalidation)
            return;

        ToolLibrary library = new ToolLibrary();
        library.setToolSettings(toolComboBox.getItems().toArray(new ToolSettings[0]));
        library.save();
    }

    private void refreshFields()
    {
        ToolSettings currentTool = toolComboBox.getValue();
        InsulationMillingSettings settings = SettingsFactory.getInsulationMillingSettings();
        context.setCurrentMillingTool(currentTool);
        context.setCurrentMillingToolIndex(toolComboBox.getSelectionModel().getSelectedIndex());

        diameterTextField.setIntegerValue(currentTool.getDiameter());
        speedTextField.setText(String.valueOf(currentTool.getSpeed()));
        xyFeedTextField.setIntegerValue(currentTool.getFeedXY());
        zFeedTextField.setIntegerValue(currentTool.getFeedZ());
        arcsFeedTextField.setText(String.valueOf(currentTool.getArcs()));
        clearanceTextField.setIntegerValue(settings.getClearance().getValue());
        safetyHeightTextField.setIntegerValue(settings.getSafetyHeight().getValue());
        workingHeightTextField.setIntegerValue(settings.getWorkingHeight().getValue());
        additionalPassesCountTextField.setText(String.valueOf(currentTool.getAdditionalPasses()));
        additonalPassesOverlapTextField.setText(String.valueOf(currentTool.getAdditionalPassesOverlap()));
        additionalPassesPadsOnlyCheckBox.setSelected(currentTool.isAdditionalPassesPadsOnly());
    }

    private void addInvalidationListenerToTextField(TextField textField)
    {
        textField.setOnAction((event) -> listener.settingsInvalidated());
        textField.focusedProperty().addListener((v, oldV, newV) ->
        {
            if (!newV)
                listener.settingsInvalidated();
        });
    }

}
