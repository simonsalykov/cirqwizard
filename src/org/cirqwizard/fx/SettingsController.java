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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.cirqwizard.excellon.ExcellonParser;
import org.cirqwizard.fx.controls.RealNumberTextField;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.serial.SerialInterfaceFactory;
import org.cirqwizard.settings.Settings;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;


public class SettingsController extends SceneController implements Initializable
{
    @FXML private Parent view;
    @FXML private RealNumberTextField yAxisDiff;
    @FXML private RealNumberTextField referencePinX;
    @FXML private RealNumberTextField referencePinY;
    @FXML private RealNumberTextField smallPCBWidth;
    @FXML private RealNumberTextField largePCBWidth;
    @FXML private RealNumberTextField farAwayY;
    @FXML private ComboBox<String> serialPortComboBox;
    @FXML private ComboBox<String> logLevelComboBox;
    @FXML private TextField processingThreads;

    @FXML private RealNumberTextField tracesToolDiameter;
    @FXML private RealNumberTextField tracesFeedXY;
    @FXML private RealNumberTextField tracesFeedZ;
    @FXML private RealNumberTextField tracesSpeed;
    @FXML private RealNumberTextField tracesClearance;
    @FXML private RealNumberTextField tracesSafetyHeight;
    @FXML private RealNumberTextField tracesZOffset;
    @FXML private RealNumberTextField tracesWorkingHeight;
    @FXML private TextField tracesAdditionalPasses;
    @FXML private TextField tracesAdditionalPassesOverlap;
    @FXML private CheckBox tracesAdditionalPassesPadsOnly;

    @FXML private RealNumberTextField drillingFeed;
    @FXML private RealNumberTextField drillingSpeed;
    @FXML private RealNumberTextField drillingClearance;
    @FXML private RealNumberTextField drillingSafetyHeight;
    @FXML private RealNumberTextField drillingZOffset;
    @FXML private RealNumberTextField drillingWorkingHeight;

    @FXML private RealNumberTextField contourFeedXY;
    @FXML private RealNumberTextField contourFeedZ;
    @FXML private RealNumberTextField contourSpeed;
    @FXML private RealNumberTextField contourClearance;
    @FXML private RealNumberTextField contourSafetyHeight;
    @FXML private RealNumberTextField contourZOffset;
    @FXML private RealNumberTextField contourWorkingHeight;

    @FXML private RealNumberTextField dispensingNeedleDiameter;
    @FXML private RealNumberTextField dispensingPrefeedPause;
    @FXML private RealNumberTextField dispensingFeed;
    @FXML private RealNumberTextField dispensingClearance;
    @FXML private RealNumberTextField dispensingZOffset;
    @FXML private RealNumberTextField dispensingWorkingHeight;
    @FXML private RealNumberTextField dispensingBleedingDuration;
    @FXML private RealNumberTextField dispensingPostfeedPause;

    @FXML private RealNumberTextField ppPickupHeight;
    @FXML private RealNumberTextField ppMoveHeight;
    @FXML private RealNumberTextField ppRotationFeed;

    @FXML private TextField excellonIntegerPlaces;
    @FXML private TextField excellonDecimalPlaces;
    @FXML private ComboBox<String> excellonUnits;
    @FXML private  TextField importPPRegex;

    private ChangeListener<String> serialPortNameListener = new ChangeListener<String>()
    {
        @Override
        public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
        {
            getMainApplication().getSettings().setSerialPort(s2);
            if (s2 != null && !s2.trim().isEmpty())
                getMainApplication().connectSerialPort(s2);
        }
    };


    @Override
    public Parent getView()
    {
        return view;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        yAxisDiff.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setMachineYDiff(integer2);
            }
        });
        referencePinX.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setMachineReferencePinX(integer2);
            }
        });
        referencePinY.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setMachineReferencePinY(integer2);
            }
        });
        smallPCBWidth.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setMachineSmallPCBWidth(integer2);
            }
        });
        largePCBWidth.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setMachineLargePCBWidth(integer2);
            }
        });
        farAwayY.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setFarAwayY(integer2);
            }
        });
        logLevelComboBox.valueProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                LoggerFactory.getApplicationLogger().setLevel(Level.parse(s2));
                getMainApplication().getSettings().setLogLevel(s2);
            }
        });
        excellonIntegerPlaces.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                try
                {
                    getMainApplication().getSettings().setImportExcellonIntegerPlaces(Integer.valueOf(s2));
                }
                catch (NumberFormatException e)
                {
                }
            }
        });
        excellonDecimalPlaces.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                try
                {
                    getMainApplication().getSettings().setImportExcellonDecimalPlaces(Integer.valueOf(s2));
                }
                catch (NumberFormatException e)
                {
                }
            }
        });
        excellonUnits.getItems().clear();
        excellonUnits.getItems().addAll("Inches", "Millimeters");
        excellonUnits.valueProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                int conversionRatio = ExcellonParser.INCHES_MM_RATIO;
                if (excellonUnits.getSelectionModel().getSelectedIndex() == 1)
                    conversionRatio = ExcellonParser.MM_MM_RATIO;
                getMainApplication().getSettings().setImportExcellonUnitConversionRatio(conversionRatio);
            }
        });
        importPPRegex.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setImportPPRegex(s2);
            }
        });
        processingThreads.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                if (s2 != null)
                {
                    try
                    {
                        getMainApplication().getSettings().setProcessingThreads(Integer.valueOf(s2));
                    }
                    catch (NumberFormatException e) {}
                }
            }
        });

        tracesToolDiameter.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultTraceToolDiameter(integer2);
            }
        });
        tracesFeedXY.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultTracesFeedXY(integer2);
            }
        });
        tracesFeedZ.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultTracesFeedZ(integer2);
            }
        });
        tracesClearance.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultTracesClearance(integer2);
            }
        });
        tracesSafetyHeight.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultTracesSafetyHeight(integer2);
            }
        });
        tracesZOffset.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultTracesZOFfset(integer2);
            }
        });
        tracesWorkingHeight.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultTracesWorkingHeight(integer2);
            }
        });
        tracesAdditionalPasses.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                try
                {
                    getMainApplication().getSettings().setTracesAdditionalPasses(Integer.valueOf(s2));
                }
                catch (NumberFormatException e) {}
            }
        });
        tracesAdditionalPassesOverlap.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                try
                {
                    getMainApplication().getSettings().setTracesAddtiionalPassesOverlap(Integer.valueOf(s2));
                }
                catch (NumberFormatException e) {}
            }
        });
        tracesAdditionalPassesPadsOnly.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2)
            {
                getMainApplication().getSettings().setTracesAdditionalPassesPadsOnly(aBoolean2);
            }
        });

        drillingFeed.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultDrillingFeed(integer2);
            }
        });
        drillingSpeed.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultDrillingSpeed(s2);
            }
        });
        drillingClearance.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultDrillingClearance(integer2);
            }
        });
        drillingSafetyHeight.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultDrillingSafetyHeight(integer2);
            }
        });
        drillingZOffset.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultDrillingZOffset(integer2);
            }
        });
        drillingWorkingHeight.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultDrillingWorkingHeight(integer2);
            }
        });

        contourFeedXY.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultContourFeedXY(integer2);
            }
        });
        contourFeedZ.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultContourFeedZ(integer2);
            }
        });
        contourSpeed.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultContourSpeed(s2);
            }
        });
        contourClearance.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultContourClearance(integer2);
            }
        });
        contourSafetyHeight.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultContourSafetyHeight(integer2);
            }
        });
        contourZOffset.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultContourZOffset(integer2);
            }
        });
        contourWorkingHeight.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultContourWorkingHeight(integer2);
            }
        });

        dispensingNeedleDiameter.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultDispensingNeedleDiameter(integer2);
            }
        });
        dispensingPrefeedPause.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultDispensingPrefeedPause(integer2);
            }
        });
        dispensingFeed.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultDispensingFeed(integer2);
            }
        });
        dispensingClearance.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultDispensingClearance(integer2);
            }
        });
        dispensingZOffset.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultDispensingZOffset(integer2);
            }
        });
        dispensingWorkingHeight.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDefaultDispensingWorkingHeight(integer2);
            }
        });
        dispensingBleedingDuration.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDispensingBleedingDuration(integer2);
            }
        });
        dispensingPostfeedPause.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setDispensingPostfeedPause(integer2);
            }
        });

        ppPickupHeight.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setPPPickupHeight(integer2);
            }
        });
        ppMoveHeight.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setPPMoveHeight(integer2);
            }
        });
        ppRotationFeed.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getSettings().setPPRotationFeed(integer2);
            }
        });
    }

    @Override
    public void refresh()
    {
        serialPortComboBox.valueProperty().removeListener(serialPortNameListener);
        ArrayList<String> serialInterfaceList = SerialInterfaceFactory.getSerialInterfaces(getMainApplication().getSerialInterface());
        serialPortComboBox.getItems().clear();
        serialPortComboBox.getItems().add("");
        serialPortComboBox.getItems().addAll(serialInterfaceList);
        Settings settings = getMainApplication().getSettings();
        if (serialInterfaceList.contains(settings.getSerialPort()))
            serialPortComboBox.setValue(settings.getSerialPort());
        serialPortComboBox.valueProperty().addListener(serialPortNameListener);
        excellonIntegerPlaces.setText(String.valueOf(settings.getImportExcellonIntegerPlaces()));
        excellonDecimalPlaces.setText(String.valueOf(settings.getImportExcellonDecimalPlaces()));
        excellonUnits.getSelectionModel().select(
                settings.getImportExcellonUnitConversionRatio() == ExcellonParser.MM_MM_RATIO ? 1 : 0);
        processingThreads.setText(String.valueOf(settings.getProcessingThreads()));

        logLevelComboBox.setValue(settings.getLogLevel());

        yAxisDiff.setIntegerValue(settings.getMachineYDiff());
        referencePinX.setIntegerValue(settings.getMachineReferencePinX());
        referencePinY.setIntegerValue(settings.getMachineReferencePinY());
        smallPCBWidth.setIntegerValue(settings.getMachineSmallPCBWidth());
        largePCBWidth.setIntegerValue(settings.getMachineLargePCBWidth());
        farAwayY.setIntegerValue(settings.getFarAwayY());

        tracesToolDiameter.setIntegerValue(settings.getDefaultTraceToolDiameter());
        tracesFeedXY.setIntegerValue(settings.getDefaultTracesFeedXY());
        tracesFeedZ.setIntegerValue(settings.getDefaultTracesFeedZ());
        tracesSpeed.setText(settings.getDefaultTracesSpeed());
        tracesClearance.setIntegerValue(settings.getDefaultTracesClearance());
        tracesSafetyHeight.setIntegerValue(settings.getDefaultTracesSafetyHeight());
        tracesZOffset.setIntegerValue(settings.getDefaultTracesZOffset());
        tracesWorkingHeight.setIntegerValue(settings.getDefaultTracesWorkingHeight());
        tracesAdditionalPasses.setText(String.valueOf(settings.getTracesAdditionalPasses()));
        tracesAdditionalPassesOverlap.setText(String.valueOf(settings.getTracesAdditionalPassesOverlap()));
        tracesAdditionalPassesPadsOnly.setSelected(settings.isTracesAdditionalPassesPadsOnly());

        drillingFeed.setIntegerValue(settings.getDefaultDrillingFeed());
        drillingSpeed.setText(settings.getDefaultDrillingSpeed());
        drillingClearance.setIntegerValue(settings.getDefaultDrillingClearance());
        drillingSafetyHeight.setIntegerValue(settings.getDefaultDrillingSafetyHeight());
        drillingZOffset.setIntegerValue(settings.getDefaultDrillingZOffset());
        drillingWorkingHeight.setIntegerValue(settings.getDefaultDrillingWorkingHeight());

        contourFeedXY.setIntegerValue(settings.getDefaultContourFeedXY());
        contourFeedZ.setIntegerValue(settings.getDefaultContourFeedZ());
        contourSpeed.setText(settings.getDefaultContourSpeed());
        contourClearance.setIntegerValue(settings.getDefaultContourClearance());
        contourSafetyHeight.setIntegerValue(settings.getDefaultContourSafetyHeight());
        contourZOffset.setIntegerValue(settings.getDefaultContourZOffset());
        contourWorkingHeight.setIntegerValue(settings.getDefaultContourWorkingHeight());

        dispensingNeedleDiameter.setIntegerValue(settings.getDefaultDispensingNeedleDiameter());
        dispensingPrefeedPause.setIntegerValue(settings.getDefaultDispensingPrefeedPause());
        dispensingFeed.setIntegerValue(settings.getDefaultDispensingFeed());
        dispensingClearance.setIntegerValue(settings.getDefaultDispensingClearance());
        dispensingZOffset.setIntegerValue(settings.getDefaultDispensingZOffset());
        dispensingWorkingHeight.setIntegerValue(settings.getDefaultDispensingWorkingHeight());
        dispensingBleedingDuration.setIntegerValue(settings.getDispensingBleedingDuration());
        dispensingPostfeedPause.setIntegerValue(settings.getDispensingPostfeedPause());

        ppPickupHeight.setIntegerValue(settings.getPPPickupHeight());
        ppMoveHeight.setIntegerValue(settings.getPPMoveHeight());
        ppRotationFeed.setIntegerValue(settings.getPPRotationFeed());

        importPPRegex.setText(settings.getImportPPRegex());
    }

}
