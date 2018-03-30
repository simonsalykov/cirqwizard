package org.cirqwizard.fx;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.cirqwizard.fx.controls.RealNumberTextField;
import org.cirqwizard.fx.util.ExceptionAlert;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqoid.cnc.controller.serial.SerialInterfaceFactory;
import org.cirqwizard.settings.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class FirstRunWizard extends ScreenController implements Initializable
{
    @FXML private Pane welcomePane;
    @FXML private Pane yAxisDifferencePane;
    @FXML private Pane zOffsetsPane;

    // Z offsets layouts
    @FXML private Label zOffsetPaneTitleLabel;
    @FXML private VBox isolatingMillingVBox;
    @FXML private VBox drillingVBox;
    @FXML private VBox dispenseVBox;

    // control z offsets
    @FXML private RealNumberTextField xTextField;
    @FXML private RealNumberTextField yTextField;
    @FXML private RealNumberTextField zTextField;

    // application settings
    @FXML private VBox serialPortVBox;
    @FXML private ComboBox serialPortComboBox;
    @FXML private RealNumberTextField referencePinXField;
    @FXML private RealNumberTextField referencePinYField;
    @FXML private RealNumberTextField yAxisDifferenceField;

    // homing
    @FXML private Pane homingPane;
    @FXML private Button homeButton;

    // general
    @FXML private Button cancelButton;
    @FXML private Button nextButton;
    @FXML private Button backButton;

    private VBox thirdSubStep;
    private Pane currentPane;
    private Step currentStep;

    @Override
    protected String getFxmlName()
    {
        return "FirstRunWizard.fxml";
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        ManualMovementController.KeyboardHandler keyboardHandler = new ManualMovementController.KeyboardHandler();
        xTextField.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        yTextField.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        zTextField.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);

        serialPortComboBox.setOnAction(e -> {
            nextButton.setDisable(thirdButtonDisabled());
        });

        yAxisDifferenceField.setOnKeyReleased(e -> {
            saveMachineSettings();
            nextButton.setDisable(thirdButtonDisabled());
        });

        referencePinXField.setOnKeyReleased(e -> {
            saveMachineSettings();
            nextButton.setDisable(thirdButtonDisabled());
        });

        referencePinYField.setOnKeyReleased(e -> {
            saveMachineSettings();
            nextButton.setDisable(thirdButtonDisabled());
        });

        cancelButton.setOnMouseClicked(e -> {
            getMainApplication().showMainApplication();
        });

        onStepChange(Step.WELCOME);
    }

    public void onNext()
    {
        // save settings if required
        if(saveSettings())
        {
            onStepChange(Step.values()[currentStep.ordinal() + 1]);
        }
    }

    public void onBack()
    {
        onStepChange(Step.values()[currentStep.ordinal() - 1]);
    }

    private void onStepChange(Step step)
    {
        currentStep = step;

        // disable current step
        if (currentPane != null)
        {
            currentPane.setVisible(false);
            currentPane.setManaged(false);
        }

        if (currentStep != Step.DISPENSE)
            nextButton.setText("Next");

        switch (currentStep)
        {
            case WELCOME:
                backButton.setVisible(false);
                currentPane = welcomePane;
                break;
            case Y_AXIS_DIFFERENCE:
                backButton.setVisible(true);
                currentPane = yAxisDifferencePane;
                fillYAxisValues();
                break;
            case HOMING:
                currentPane = homingPane;
                toHoming();
                break;
            case ISOLATING_MILLING:
                currentPane = zOffsetsPane;
                zOffsetPaneTitleLabel.setText("Isolation milling Z offset");
                toZOffsetsStep();
                thirdStepChangeDescription(isolatingMillingVBox);
                break;
            case DRILLING:
                currentPane = zOffsetsPane;
                zOffsetPaneTitleLabel.setText("Drilling & contour milling Z offsets");
                toZOffsetsStep();
                thirdStepChangeDescription(drillingVBox);
                break;
            case DISPENSE:
                currentPane = zOffsetsPane;
                zOffsetPaneTitleLabel.setText("Dispensing Z offsets");
                toZOffsetsStep();
                thirdStepChangeDescription(dispenseVBox);
                nextButton.setText("Finish");
                break;
            case FINISH:
                getMainApplication().showMainApplication();
                break;
        }

        currentPane.setVisible(true);
        currentPane.setManaged(true);
    }

    private boolean saveSettings()
    {
        if (currentStep == null)
            return true;

        switch (currentStep)
        {
            case Y_AXIS_DIFFERENCE:
                saveMachineSettings();
                return connectToCirqoid();
            case ISOLATING_MILLING:
                saveIsolationMillingOffset();
                return false;
            case DRILLING:
                saveDrillingCountourOffset();
                return false;
            case DISPENSE:
                saveDispenseOffset();
                return false;
        }

        return true;
    }

    public void fillYAxisValues()
    {
        List<String> interfaces = SerialInterfaceFactory.getSerialInterfaces(getMainApplication().getSerialInterface());
        UserPreference<String> port = SettingsFactory.getApplicationSettings().getSerialPort();
        serialPortComboBox.setItems(FXCollections.observableArrayList(interfaces));
        serialPortComboBox.setValue(port.getValue());

        yAxisDifferenceField.setIntegerValue(getYAxisDifference());
        referencePinXField.setIntegerValue(getReferencePinXField());
        referencePinYField.setIntegerValue(getReferencePinYField());

        nextButton.setDisable(thirdButtonDisabled());
    }

    public void toZOffsetsStep()
    {
        thirdStepChangeDescription(isolatingMillingVBox);
    }

    public void toHoming()
    {
        yAxisDifferencePane.setVisible(false);
        homingPane.setVisible(true);
        nextButton.setDisable(true);
    }

    private void thirdStepChangeDescription(VBox step)
    {
        if (thirdSubStep != null)
            thirdSubStep.setVisible(false);

        thirdSubStep = step;
        thirdSubStep.setVisible(true);
    }

    public void saveDispenseOffset()
    {
        double zOffset = Double.parseDouble(zTextField.getRealNumberText());

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmation");
        alert.setHeaderText(String.format("Use %.2f as dispensing Z offset?", zOffset));
        alert.showAndWait().filter(response -> response == ButtonType.YES).ifPresent(response ->
        {
            DispensingSettings dispensingSettings = SettingsFactory.getDispensingSettings();

            UserPreference userPreference = dispensingSettings.getZOffset();
            userPreference.setValue(zTextField.getIntegerValue());
            dispensingSettings.setZOffset(userPreference);

            dispensingSettings.save();

            onStepChange(Step.values()[currentStep.ordinal() + 1]);
        });
    }

    public void saveIsolationMillingOffset()
    {
        double zOffset = Double.parseDouble(zTextField.getRealNumberText());

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmation");
        alert.setHeaderText(String.format("Use %.2f as isolation milling Z offset?", zOffset));
        alert.showAndWait().filter(response -> response == ButtonType.YES).ifPresent(response ->
        {
            ToolLibrary toolLibrary = new ToolLibrary();
            ToolSettings toolSettings = ToolLibrary.getDefaultTool();
            toolSettings.setZOffset(zTextField.getIntegerValue());
            toolLibrary.setToolSettings(new ToolSettings[]{toolSettings});
            toolLibrary.save();

            RubOutSettings settings = SettingsFactory.getRubOutSettings();
            settings.getZOffset().setValue(zTextField.getIntegerValue());
            settings.save();

            onStepChange(Step.values()[currentStep.ordinal() + 1]);
        });
    }

    public void saveDrillingCountourOffset()
    {
        double zOffset = Double.parseDouble(zTextField.getRealNumberText());

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmation");
        alert.setHeaderText(String.format("Use %.2f as drilling and contour milling Z offset?", zOffset));
        alert.showAndWait().filter(response -> response == ButtonType.YES).ifPresent(response ->
        {
            DrillingSettings drillingSettings = SettingsFactory.getDrillingSettings();
            UserPreference drillingZOffset = drillingSettings.getZOffset();
            drillingZOffset.setValue(zTextField.getIntegerValue());

            drillingSettings.setZOffset(drillingZOffset);
            drillingSettings.save();

            ContourMillingSettings contourMillingSettings = SettingsFactory.getContourMillingSettings();
            UserPreference contourMillingZOffset = contourMillingSettings.getZOffset();
            contourMillingZOffset.setValue(zTextField.getIntegerValue());

            contourMillingSettings.setZOffset(contourMillingZOffset);
            contourMillingSettings.save();

            onStepChange(Step.values()[currentStep.ordinal() + 1]);
        });
    }

    public void home()
    {
        try
        {
            getMainApplication().getCNCController().home(yAxisDifferenceField.getIntegerValue());
            nextButton.setDisable(false);
        }
        catch (Exception ex)
        {
            LoggerFactory.logException("Communication with controller failed: ", ex);
            ExceptionAlert alert = new ExceptionAlert("Oops! That's embarrassing!", "Communication error",
                    "Something went wrong while communicating with the controller. " +
                            "The most sensible thing to do now would be to close the program and start over again. Sorry about that.", ex);
            alert.showAndWait();
        }
    }

    private void saveMachineSettings()
    {
        Object serialPort = serialPortComboBox.getValue();
        if (serialPort != null)
        {
            ApplicationSettings applicationSettings = SettingsFactory.getApplicationSettings();
            UserPreference serialPortPreference = applicationSettings.getSerialPort();
            serialPortPreference.setValue(serialPort.toString());
            applicationSettings.setSerialPort(serialPortPreference);
            applicationSettings.save();
        }

        // saving settings
        MachineSettings machineSettings = SettingsFactory.getMachineSettings();

        UserPreference yAxisDifference = machineSettings.getYAxisDifference();
        yAxisDifference.setValue(yAxisDifferenceField.getIntegerValue());

        UserPreference referencePinX = machineSettings.getReferencePinX();
        referencePinX.setValue(referencePinXField.getIntegerValue());

        UserPreference referencePinY = machineSettings.getReferencePinY();
        referencePinY.setValue(referencePinYField.getIntegerValue());

        machineSettings.setYAxisDifference(yAxisDifference);
        machineSettings.setReferencePinX(referencePinX);
        machineSettings.setReferencePinY(referencePinY);
        machineSettings.save();
    }

    private boolean connectToCirqoid()
    {
        try
        {
            Object serialPort = serialPortComboBox.getValue();
            if (getMainApplication().getCNCController() == null)
            {
                getMainApplication().connectSerialPort(serialPort.toString());
                TimeUnit.SECONDS.sleep(1);

                for (int i = 0; i < 5 && getMainApplication().getCNCController() == null; ++i)
                {
                    TimeUnit.SECONDS.sleep(1);
                }

                if (getMainApplication().getCNCController() == null)
                {
                    throw new Exception("Establishing connection with controller has failed.");
                }
            }
            return true;
        }
        catch(Exception ex)
        {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot establish connection to your Cirqoid machine");
        }

        return false;
    }

    private Integer getYAxisDifference()
    {
        // saving settings
        MachineSettings machineSettings = SettingsFactory.getMachineSettings();

        UserPreference yAxisDifference = machineSettings.getYAxisDifference();
        return (Integer) yAxisDifference.getValue();
    }

    private Integer getReferencePinXField()
    {
        // saving settings
        MachineSettings machineSettings = SettingsFactory.getMachineSettings();

        UserPreference referencePinX = machineSettings.getReferencePinX();
        return (Integer) referencePinX.getValue();
    }

    private Integer getReferencePinYField()
    {
        // saving settings
        MachineSettings machineSettings = SettingsFactory.getMachineSettings();

        UserPreference referencePinY = machineSettings.getReferencePinY();
        return (Integer) referencePinY.getValue();
    }

    private boolean thirdButtonDisabled()
    {
        return referencePinXField.getIntegerValue() == null || referencePinXField.getIntegerValue() == 0 ||
               referencePinYField.getIntegerValue() == null || referencePinYField.getIntegerValue() == 0 ||
               yAxisDifferenceField.getIntegerValue() == null || yAxisDifferenceField.getIntegerValue() == 0 ||
               serialPortComboBox.getValue() == null;
    }

    public void goToUpdatedCoordinates()
    {
        getMainApplication().getCNCController().moveTo(xTextField.getIntegerValue(),
                                                       yTextField.getIntegerValue(),
                                                       zTextField.getIntegerValue());
    }

    private enum Step
    {
        WELCOME(0),
        Y_AXIS_DIFFERENCE(1),
        HOMING(2),
        ISOLATING_MILLING(3),
        DRILLING(4),
        DISPENSE(5),
        FINISH(6);

        private final int value;
        Step(final int newValue) { value = newValue; }
        public int getValue() { return value; }
    }
}
