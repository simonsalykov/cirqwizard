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
import org.cirqwizard.fx.controls.RealNumberTextField;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.InsulationMillingSettings;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.settings.ToolLibrary;
import org.cirqwizard.settings.ToolSettings;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by simon on 05.10.15.
 */
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
    @FXML private CheckBox additionalPassesPadsOnlyComboBox;

    private Context context;

    public TracesSettingsPopOver(Context context)
    {
        this.context = context;

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
        toolComboBox.getSelectionModel().selectedItemProperty().addListener((v, oldV, newV) -> refreshFields());
        diameterTextField.realNumberIntegerProperty().addListener((v, oldV, newV) ->
        {
            if (toolComboBox.getSelectionModel().getSelectedIndex() >= 0)
            {
                toolComboBox.getItems().get(toolComboBox.getSelectionModel().getSelectedIndex()).setDiameter(newV);
                saveLibrary();
            }
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
            toolComboBox.setItems(FXCollections.observableArrayList(ToolLibrary.load().getToolSettings()));
            toolComboBox.getSelectionModel().select(context.getCurrentMillingToolIndex());
        }
        catch (Exception e)
        {
            LoggerFactory.logException("Could not load tool library", e);
        }
    }

    private void saveLibrary()
    {
        ToolLibrary library = new ToolLibrary();
        library.setToolSettings(toolComboBox.getItems().toArray(new ToolSettings[0]));
        library.save();
    }

    private void refreshFields()
    {
        ToolSettings currentTool = toolComboBox.getValue();
        InsulationMillingSettings settings = SettingsFactory.getInsulationMillingSettings();
        context.setCurrentMillingTool(currentTool);

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
        additionalPassesPadsOnlyComboBox.setSelected(currentTool.isAdditionalPassesPadsOnly());
    }
}
